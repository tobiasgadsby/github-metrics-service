package com.lbg.ecp.controller;

import com.lbg.ecp.StartupDataLoader;
import com.lbg.ecp.api.GithubApi;
import com.lbg.ecp.entities.api.OpenPullRequestResponse;
import com.lbg.ecp.entities.frontend.RepositoryComponent;
import com.lbg.ecp.entities.frontend.components.Comment;
import com.lbg.ecp.entities.frontend.components.ComponentType;
import com.lbg.ecp.entities.frontend.components.Health;
import com.lbg.ecp.entities.frontend.components.Label;
import com.lbg.ecp.entities.tables.Branch;
import com.lbg.ecp.entities.tables.PullRequest;
import com.lbg.ecp.entities.tables.Repository;
import com.lbg.ecp.repository.BranchRepo;
import com.lbg.ecp.repository.PullRequestRepo;
import com.lbg.ecp.repository.RepositoryRepo;
import com.lbg.ecp.service.DataMgmtService;
import com.lbg.ecp.service.HealthCheckService;

import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class GithubMetricsController {

    private final RepositoryRepo repositoryRepo;
    private final BranchRepo branchRepo;
    private final HealthCheckService healthCheckService;
    private final PullRequestRepo pullRequestRepo;
    private final DataMgmtService dataMgmtService;
    private final GithubApi githubApi;
    private final StartupDataLoader startupDataLoader;
    private Logger logger = LogManager.getLogger(GithubMetricsController.class);

    public GithubMetricsController(
            BranchRepo branchRepo,
            RepositoryRepo repositoryRepo,
            HealthCheckService healthCheckService,
            PullRequestRepo pullRequestRepo,
            DataMgmtService dataMgmtService, GithubApi githubApi, StartupDataLoader startupDataLoader) {
        this.branchRepo = branchRepo;
        this.repositoryRepo = repositoryRepo;
        this.healthCheckService = healthCheckService;
        this.pullRequestRepo = pullRequestRepo;
        this.dataMgmtService = dataMgmtService;
        this.githubApi = githubApi;
        this.startupDataLoader = startupDataLoader;
    }

    @GetMapping("/getBranches")
    public List<com.lbg.ecp.entities.frontend.RepositoryComponent> getBranches(
            @RequestParam("repositoryId") Long repositoryId) {
        List<Branch> branches = branchRepo.findBranchesByRepository_Id(repositoryId);

        return  branches.stream()
                .map(
                        branch -> new RepositoryComponent()
                                .setName(branch.getBranchName())
                                .setComponentId(branch.getId())
                                .setHealth(
                                        new Health(
                                                branch.getHealth().getHealthQuality(),
                                                branch.getHealth().getComments().stream()
                                                        .map(
                                                                comment -> new Comment(comment.getIsNegative(), comment.getDetails(), comment.getSolution()))
                                                        .toList()
                                        )
                                )
                                .setComponentType(ComponentType.BRANCH)
                )
                .toList();
    }

    @GetMapping("/getRepositories")
    public List<com.lbg.ecp.entities.frontend.Repository> getRepositories(
            @RequestParam(name = "tenantCode") String tenantCode) {
        List<Repository> repositories =
                repositoryRepo.findByWatchedRepository_Tenant_TenantCode(tenantCode);
        return repositories.stream().map(repository -> new com.lbg.ecp.entities.frontend.Repository(
                repository.getId(),
                repository.getName(),
                repository.getHealth().getHealthQuality()
        )).toList();
    }

    @GetMapping("/getPullRequests")
    public List<RepositoryComponent> getPullRequests(
            @RequestParam(name = "repositoryId") Long repositoryId) {
        List<com.lbg.ecp.entities.tables.PullRequest> pullRequests =
                pullRequestRepo.findPullRequestsByRepositoryId(repositoryId);
        return buildPullRequests(pullRequests);
    }

    @GetMapping("/getAllPullRequests")
    public List<RepositoryComponent> getAllPullRequests() {
        List<com.lbg.ecp.entities.tables.PullRequest> pullRequests =
                pullRequestRepo.findAll();
        return buildPullRequests(pullRequests);
    }

    private List<RepositoryComponent> buildPullRequests(
            List<PullRequest> pullRequests
    ) {
        return pullRequests.stream()
                .map(
                        pullRequest ->
                                new RepositoryComponent()
                                        .setComponentId(pullRequest.getId())
                                        .setName(pullRequest.getTitle())
                                        .setHealth(
                                                new Health(
                                                        pullRequest.getHealth().getHealthQuality(),
                                                        pullRequest.getHealth().getComments().stream()
                                                                .map(
                                                                        comment ->
                                                                                new Comment(
                                                                                        comment.getIsNegative(),
                                                                                        comment.getDetails(),
                                                                                        comment.getSolution()))
                                                                .toList()))
                                        .setComponentType(ComponentType.PULL_REQUEST)
                                        .setLabels(
                                                pullRequest.getPullRequestLabels().stream().map(
                                                        label -> new Label()
                                                                .setName(label.getName())
                                                                .setDescription(label.getDescription())
                                                                .setColor(label.getColor())
                                                )
                                                        .toList()
                                        ))
                .toList();
    }

    @GetMapping("/getRepositoryComponents")
    public List<RepositoryComponent> getRepositoryComponents(
            @RequestParam(name = "repositoryId") Long repositoryId) {
        return Stream.concat(
                getBranches(repositoryId).stream(),
                getPullRequests(repositoryId).stream()).toList();
    }

    @PostMapping("/deleteComponent")
    public void deleteComponent(
            @RequestParam Long componentId,
            @RequestParam String componentType
    ) {
        dataMgmtService.deleteComponent(componentType, componentId);
    }

    @PostMapping("/openPullRequest")
    public OpenPullRequestResponse openPullRequest(
            @RequestParam String componentId,
            @RequestParam String title,
            @RequestParam String branchToMergeInto,
            @RequestParam String body
    ) {
        Branch branch = branchRepo.findBramchById(Long.valueOf(componentId)).get();
        OpenPullRequestResponse openPullRequestResponse = githubApi.openPullRequest(
                branch.getRepository().getOwner(),
                branch.getRepository().getName(),
                title,
                branch.getBranchName(),
                branchToMergeInto,
                body
        );
        return openPullRequestResponse;
    }

    @PostMapping("/executeFullRefresh")
    public void executeFullRefresh() {
        startupDataLoader.executeFullRefresh();
    }
}
