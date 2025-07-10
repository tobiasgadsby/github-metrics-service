package com.lbg.ecp.controller;

import com.lbg.ecp.entities.frontend.RepositoryComponent;
import com.lbg.ecp.entities.frontend.components.Comment;
import com.lbg.ecp.entities.frontend.components.ComponentType;
import com.lbg.ecp.entities.frontend.components.Health;
import com.lbg.ecp.entities.tables.Branch;
import com.lbg.ecp.entities.tables.Repository;
import com.lbg.ecp.repository.BranchRepo;
import com.lbg.ecp.repository.PullRequestRepo;
import com.lbg.ecp.repository.RepositoryRepo;
import com.lbg.ecp.service.HealthCheckService;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class GithubMetricsController {

  private final RepositoryRepo repositoryRepo;
  private final BranchRepo branchRepo;
  private final HealthCheckService healthCheckService;
  private final PullRequestRepo pullRequestRepo;

  public GithubMetricsController(
      BranchRepo branchRepo,
      RepositoryRepo repositoryRepo,
      HealthCheckService healthCheckService,
      PullRequestRepo pullRequestRepo) {
    this.branchRepo = branchRepo;
    this.repositoryRepo = repositoryRepo;
    this.healthCheckService = healthCheckService;
    this.pullRequestRepo = pullRequestRepo;
  }

  @GetMapping("/getBranches")
  public List<com.lbg.ecp.entities.frontend.RepositoryComponent> getBranches(
      @RequestParam("repositoryId") Long repositoryId) {
    List<Branch> branches = branchRepo.findBranchesByRepository_Id(repositoryId);
    return branches.stream()
        .map(
            branch ->
                new com.lbg.ecp.entities.frontend.RepositoryComponent(
                    branch.getBranchName(),
                    new Health(
                        branch.getHealth().getHealthQuality(),
                        branch.getHealth().getComments().stream()
                            .map(
                                comment ->
                                    new Comment(comment.getIsNegative(), comment.getDetails(), comment.getSolution()))
                            .toList()),
                    ComponentType.BRANCH))
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
    return pullRequests.stream()
        .map(
            pullRequest ->
                new RepositoryComponent()
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
                    .setComponentType(ComponentType.PULL_REQUEST))
        .toList();
  }

  @GetMapping("/getRepositoryComponents")
  public List<RepositoryComponent> getRepositoryComponents(
      @RequestParam(name = "repositoryId") Long repositoryId) {
    return Stream.concat(
        getBranches(repositoryId).stream(),
        getPullRequests(repositoryId).stream()).toList();
  }
}
