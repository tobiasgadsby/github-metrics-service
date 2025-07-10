package com.lbg.ecp.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lbg.ecp.entities.api.Branch;
import com.lbg.ecp.entities.api.GitCommit;
import com.lbg.ecp.entities.api.PullRequest;
import com.lbg.ecp.entities.api.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GithubApi {

  private final RestClient restClient;
  private final ObjectMapper objectMapper;

  @Autowired
  GithubApi(RestClient restClient, ObjectMapper objectMapper) {
    this.restClient = restClient;
    this.objectMapper = objectMapper;
  }

  public List<Branch> getBranches(String owner, String repo) {
    return restClient
        .get()
        .uri("/repos/{owner}/{repo}/branches", owner, repo)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

  public Branch getBranch(String owner, String repo, String branchName) {
    return restClient
        .get()
        .uri("/repos/{owner}/{repo}/branches/{branchName}", owner, repo, branchName)
        .retrieve()
        .body(Branch.class);
  }

  public List<PullRequest> getPullRequests(String owner, String repo) {
    return restClient
        .get()
        .uri("/repos/{owner}/{repo}/pulls", owner, repo)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

  public List<Repository> getRepositoriesBySearch(String search) {
    Map<String, Object> response =
        objectMapper.convertValue(
            restClient
                .get()
                .uri("/search/repositories?q={search}", search)
                .retrieve()
                .body(Map.class),
            new TypeReference<>() {});
    final List<Repository> repositories = new ArrayList<>();

    if (response != null) {
      List<Map<String, Object>> items =
          objectMapper.convertValue(response.get("items"), new TypeReference<>() {});
      items.forEach(item -> repositories.add(objectMapper.convertValue(item, Repository.class)));
    }
    return repositories;
  }

  public GitCommit getCommitDetails(String repoOwner, String repoName, String commitSha) {
    return restClient
        .get()
        .uri("/repos/{owner}/{repo}/commits/{sha}", repoOwner, repoName, commitSha)
        .retrieve()
        .body(GitCommit.class);
  }
}
