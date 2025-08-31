package com.lbg.ecp.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lbg.ecp.entities.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GithubApi {

  private final RestClient restClient;
  private final ObjectMapper objectMapper;

  private static Logger logger = LoggerFactory.getLogger(GithubApi.class);

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

  public List<Repository> getAuthenticatedUsersRepositories() {
    List<Repository> repositories = objectMapper.convertValue(
            restClient
                    .get()
                    .uri("/user/repos?type=owner")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Repository>>() {})
            , new TypeReference<List<Repository>>() {}
    );
    return repositories;
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

  public PullRequest getPullRequest(String owner, String repo, String pull_number) {
    return objectMapper.convertValue(
            restClient
                    .get()
                    .uri("/repos/{owner}/{repo}/pulls/{pull_number}",owner ,repo, pull_number)
                    .retrieve()
                    .body(Map.class),
            PullRequest.class
    );
  }

  public GitCommit getCommitDetails(String repoOwner, String repoName, String commitSha) {
    return restClient
        .get()
        .uri("/repos/{owner}/{repo}/commits/{sha}", repoOwner, repoName, commitSha)
        .retrieve()
        .body(GitCommit.class);
  }

  public void deleteBranch(String owner, String repo, String branchName) {
    logger.info("Delete branch {} from repo {}", branchName, repo);
    try {
      restClient
              .delete()
              .uri("/repos/{owner}/{repo}/git/refs/heads/{branchName}", owner, repo, branchName)
              .retrieve()
              .toBodilessEntity();
    } catch (Exception e) {
      logger.error("ERROR ", e);
    }
  }

  public void deletePullRequest(String owner, String repo, String pullRequestId) {
    logger.info("Delete pull request {} from repo {}", pullRequestId, owner);
    try {
      restClient
              .patch()
              .uri("/repos/{owner}/{repo}/pulls/{pull_number}", owner, repo, pullRequestId)
              .body("{\"state\":\"closed\"}")
              .contentType(MediaType.APPLICATION_JSON)
              .retrieve()
              .toBodilessEntity();
    } catch (Exception e) {
      logger.error("ERROR ", e);
    }

  }

  public OpenPullRequestResponse openPullRequest(String owner, String repo, String title, String branchToMerge, String branchToMergeInto, String body) {
    logger.info("Open pull request for repo: {} for branch: {}", repo, branchToMerge);
    OpenPullRequestRequest openPullRequestRequest = new OpenPullRequestRequest()
            .setTitle(title)
            .setHead(branchToMerge)
            .setBase(branchToMergeInto)
            .setBody(body);
    OpenPullRequestResponse openPullRequestResponse = null;
    try {
      // --- TEMPORARY DEBUGGING CODE ---
      // Use .toEntity() to capture the full response: status, headers, and body as a String
      ResponseEntity<String> responseEntity = restClient
              .post()
              .uri("/repos/{owner}/{repo}/pulls", owner, repo)
              .body(openPullRequestRequest)
              .retrieve()
              .toEntity(String.class); // Retrieve the body as a String

      // Log everything about the response
      logger.info("GitHub API Response Status: {}", responseEntity.getStatusCode());
      logger.info("GitHub API Response Headers: {}", responseEntity.getHeaders());
      logger.info("GitHub API Response Body: {}", responseEntity.getBody());

      ObjectMapper objectMapper = new ObjectMapper();
      openPullRequestResponse = objectMapper.readValue(responseEntity.getBody(), OpenPullRequestResponse.class);

      // Now, check the response before proceeding
      if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
        // If everything looks good in the logs, you can try to manually deserialize it
        // For now, let's just return null to isolate the logging step.
        // In a real fix, you would parse the logged body.
        // For example: new ObjectMapper().readValue(responseEntity.getBody(), OpenPullRequestResponse.class);
      }

      // The method will currently still return null, but your logs will now contain the truth.

    } catch (Exception e) {
      logger.error("AN ERROR OCCURRED: ", e);
    }
    return openPullRequestResponse; // Return null while debugging to see the logs clearly
  }

}
