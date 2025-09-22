package com.lbg.ecp.service;

import com.lbg.ecp.api.GithubApi;
import com.lbg.ecp.entities.api.Branch;
import com.lbg.ecp.entities.api.GitCommit;
import com.lbg.ecp.entities.api.PullRequest;
import com.lbg.ecp.entities.tables.*;
import com.lbg.ecp.repository.BranchRepo;
import com.lbg.ecp.repository.CommitRepo;
import com.lbg.ecp.repository.PullRequestRepo;
import com.lbg.ecp.repository.RepositoryRepo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DataLoadService {

    private final GithubApi githubApi;
    private final BranchRepo branchRepo;
    private final CommitRepo commitRepo;
    private final RepositoryRepo repositoryRepo;
    private final PullRequestRepo pullRequestRepo;
    private final HealthCheckService healthCheckService;
    Logger log = LogManager.getLogger();

    @Autowired
    public DataLoadService(
            GithubApi githubApi,
            BranchRepo branchRepo,
            CommitRepo commitRepo,
            RepositoryRepo repositoryRepo,
            PullRequestRepo pullRequestRepo,
            HealthCheckService healthCheckService) {
        this.branchRepo = branchRepo;
        this.commitRepo = commitRepo;
        this.githubApi = githubApi;
        this.repositoryRepo = repositoryRepo;
        this.pullRequestRepo = pullRequestRepo;
        this.healthCheckService = healthCheckService;
    }

    public Repository updateRepositoryBaseDetails(com.lbg.ecp.entities.api.Repository repository) {
        // check if already present in the database, if present then update the entity with any changed details, if not create a new repository entity.
        return repositoryRepo
                .findRepositoryByFullName(repository.getFullName())
                .map(
                        existingRepository -> {
                            existingRepository.setName(repository.getName());
                            existingRepository.setOwner(repository.getOwner().getLogin());
                            existingRepository.setFullName(repository.getFullName());
                            repositoryRepo.save(existingRepository);
                            return existingRepository;
                        }
                )
                .orElseGet(
                        () ->
                                repositoryRepo.save(Repository.builder()
                                        .name(repository.getFullName())
                                        .owner(repository.getOwner().getLogin())
                                        .fullName(repository.getFullName())
                                        .build()));
    }

    private Health updateHealth(Health health, Health newHealth) {
        return Health.builder()
                .healthQuality(newHealth.getHealthQuality())
                .comments(newHealth.getComments().stream().map(
                        comment -> Comment.builder()
                                .health(health)
                                .isNegative(comment.getIsNegative())
                                .commentType(comment.getCommentType())
                                .details(comment.getDetails())
                                .solution(comment.getSolution())
                                .build()
                ).toList())
                .build();
    }

    public com.lbg.ecp.entities.tables.PullRequest updatePullRequest(Repository repository, PullRequest pullRequest) {
        return pullRequestRepo.findPullRequestByUrl(pullRequest.getUrl()).map(
                existingPullRequest -> {

                    //UPDATE BASE INFO
                    existingPullRequest.setTitle(pullRequest.getTitle());
                    existingPullRequest.setNumber(pullRequest.getNumber());

                    //UPDATE HEALTH
                    Health updatedHealth = healthCheckService.calculateHealth(existingPullRequest);
                    existingPullRequest.setHealth(updatedHealth);

                    //UPDATE LABELS
                    existingPullRequest.getPullRequestLabels().clear();
                    existingPullRequest.getPullRequestLabels().addAll(
                            pullRequest.getLabels().stream().map(
                                    label -> PullRequestLabel.builder()
                                            .name(label.getName())
                                            .description(label.getDescription())
                                            .color(label.getColor())
                                            .pullRequest(existingPullRequest)
                                            .build()
                            ).toList()
                    );

                    //Save the updated pull request (and its related health, comment, and label entities)
                    pullRequestRepo.save(existingPullRequest);
                    return existingPullRequest;
                }).orElseGet( () -> {

                    //Create new Pull Request.
                    com.lbg.ecp.entities.tables.PullRequest newPullRequest = com.lbg.ecp.entities.tables.PullRequest.builder()
                            .url(pullRequest.getUrl())
                            .title(pullRequest.getTitle())
                            .createdAt(new Timestamp(System.currentTimeMillis()))
                            .number(pullRequest.getNumber())
                            .repository(repository)
                            .build();

                    //Create new health and add to repository.
                    Health updatedHealth = healthCheckService.calculateHealth(newPullRequest);
                    newPullRequest.setHealth(updateHealth(newPullRequest.getHealth(), updatedHealth));

                    //Save repository
                    pullRequestRepo.save(newPullRequest);
                    return newPullRequest;
                }
        );
    }

    public com.lbg.ecp.entities.tables.Branch updateBranch(Repository repository, Branch branch) {

        Branch.Commit latestGitCommitFromBranch =
                githubApi
                        .getBranch(repository.getOwner(), repository.getName(), branch.getName())
                        .getCommit();

        GitCommit commitDetails =
                githubApi.getCommitDetails(
                        repository.getOwner(),
                        repository.getName(),
                        latestGitCommitFromBranch.getSha());

        return branchRepo
                .findBranchByRepositoryAndBranchName(repository, branch.getName())
                .map(
                        existingBranch -> {

                            existingBranch.setBranchName(branch.getName());

                            //TODO: RESUME HERE
                            Commit commit = existingBranch.getLatestCommit();
                            if (commit != null) {
                                commit.setCommitSha(latestGitCommitFromBranch.getSha());
                                commit.setCommitDate(
                                        Timestamp.valueOf(
                                                LocalDateTime.parse(
                                                        commitDetails.getCommit().getAuthor().getDate(),
                                                        DateTimeFormatter.ISO_DATE_TIME)));
                                existingBranch.setLatestCommit(commit);
                                Health health =
                                        healthCheckService.calculateHealth(
                                                existingBranch.getBranchName(), commitDetails);
                                health.getComments().forEach(
                                        comment -> comment.setHealth(existingBranch.getHealth())
                                );
                                existingBranch.getHealth().setComments(health.getComments());
                                existingBranch
                                        .getHealth()
                                        .setHealthQuality(health.getHealthQuality());
                                branchRepo.save(existingBranch);
                            }
                            return existingBranch;
                        })
                .orElseGet(
                        () -> {
                            var newBranch =
                                    new com.lbg.ecp.entities.tables.Branch()
                                            .setRepository(repository)
                                            .setBranchName(branch.getName())
                                            .setHealth(new Health().setHealthQuality(1.0));
                            branchRepo.save(newBranch);

                            var newCommit =
                                    new Commit(
                                            latestGitCommitFromBranch.getSha(),
                                            newBranch,
                                            Timestamp.valueOf(
                                                    LocalDateTime.parse(
                                                            commitDetails.getCommit().getAuthor().getDate(),
                                                            DateTimeFormatter.ISO_DATE_TIME)));
                            commitRepo.save(newCommit);

                            Health newHealth =
                                    healthCheckService.calculateHealth(
                                            newBranch.getBranchName(), commitDetails);
                            newHealth.getComments().forEach(
                                    comment -> comment.setHealth(newHealth)
                            );
                            newBranch.setHealth(newHealth);
                            branchRepo.save(newBranch);
                            return newBranch;
                        });
    }
}
