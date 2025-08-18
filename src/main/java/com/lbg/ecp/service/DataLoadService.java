package com.lbg.ecp.service;

import com.lbg.ecp.api.GithubApi;
import com.lbg.ecp.entities.api.Branch;
import com.lbg.ecp.entities.api.GitCommit;
import com.lbg.ecp.entities.api.PullRequest;
import com.lbg.ecp.entities.tables.Commit;
import com.lbg.ecp.entities.tables.Health;
import com.lbg.ecp.entities.tables.Repository;
import com.lbg.ecp.repository.BranchRepo;
import com.lbg.ecp.repository.CommitRepo;
import com.lbg.ecp.repository.HealthRepo;
import com.lbg.ecp.repository.PullRequestRepo;
import com.lbg.ecp.repository.RepositoryRepo;
import jakarta.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
      HealthCheckService healthCheckService,
      HealthRepo healthRepo) {
    this.branchRepo = branchRepo;
    this.commitRepo = commitRepo;
    this.githubApi = githubApi;
    this.repositoryRepo = repositoryRepo;
    this.pullRequestRepo = pullRequestRepo;
    this.healthCheckService = healthCheckService;
  }

  @Transactional
  public void updateRepositoryData(Repository repository) {

    // TODO Add error check

    Double branchHealthQuality = updateBranches(repository);
    Double pullRequestHealthQuality = updatePullRequests(repository);

    repository.getHealth().setHealthQuality((branchHealthQuality + pullRequestHealthQuality) / 2);
    repositoryRepo.save(repository);
  }

  private Double updatePullRequests(Repository repository) {
    List<PullRequest> pullRequests =
        githubApi.getPullRequests(repository.getOwner(), repository.getName());
    if (pullRequests.isEmpty()) {
      return 1.0;
    }
    return pullRequests.stream()
            .mapToDouble(
                    pullRequest -> {
                        return pullRequestRepo.findPullRequestByUrl(pullRequest.getUrl()).map(
                                existingPullRequest -> {
                                    Health health = healthCheckService.getHealth(existingPullRequest);
                                    existingPullRequest.getHealth().setHealthQuality(
                                            health.getHealthQuality()
                                    );
                                    existingPullRequest.getHealth().getComments().forEach(
                                            comment -> comment.setHealth(existingPullRequest.getHealth())
                                    );
                                    existingPullRequest.getHealth().setComments(
                                            health.getComments()
                                    );
                                    pullRequestRepo.save(existingPullRequest);
                                    return health.getHealthQuality();
                                }
                        ).orElseGet(
                                () -> {
                                    com.lbg.ecp.entities.tables.PullRequest newPullRequest = new com.lbg.ecp.entities.tables.PullRequest(
                                            pullRequest.getUrl(),
                                            repository,
                                            pullRequest.getTitle(),
                                            Timestamp.valueOf(
                                                    LocalDateTime.parse(
                                                            pullRequest.getCreatedAt(),
                                                            DateTimeFormatter.ISO_DATE_TIME)),
                                            pullRequest.getNumber()
                                    );
                                    Health health = healthCheckService.getHealth(newPullRequest);
                                    health.getComments().forEach(
                                            comment -> comment.setHealth(health)
                                    );
                                    newPullRequest.setHealth(health);
                                    pullRequestRepo.save(newPullRequest);
                                    return health.getHealthQuality();
                                }
                        );
                    }
            ).sum() / pullRequests.size();

  }

  private Double updateBranches(Repository repository) {
    List<Branch> branches = githubApi.getBranches(repository.getOwner(), repository.getName());
    if(branches.isEmpty()) {
      return 1.0;
    }
    return branches.stream()
            .mapToDouble(
                branch -> {
                  log.info("BRANCH: {}", branch);

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
                                  healthCheckService.getHealth(
                                      existingBranch.getBranchName(), commitDetails);
                              health.getComments().forEach(
                                  comment -> comment.setHealth(existingBranch.getHealth())
                              );
                              existingBranch.getHealth().setComments(health.getComments());
                              existingBranch
                                  .getHealth()
                                  .setHealthQuality(health.getHealthQuality());
                              branchRepo.save(existingBranch);
                              return health.getHealthQuality();
                            }
                            return 0.0;
                          })
                      .orElseGet(
                          () -> {
                            var newBranch =
                                new com.lbg.ecp.entities.tables.Branch(
                                    repository, branch.getName(), new Health(1.0));
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
                                healthCheckService.getHealth(
                                    newBranch.getBranchName(), commitDetails);
                            newHealth.getComments().forEach(
                                comment -> comment.setHealth(newHealth)
                            );
                            newBranch.setHealth(newHealth);
                            branchRepo.save(newBranch);
                            return newHealth.getHealthQuality();
                          });
                })
            .sum()
        / branches.size();
  }
}
