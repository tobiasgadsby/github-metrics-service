package com.lbg.ecp.entities.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitCommit {
  private String sha;

  @JsonProperty("node_id")
  private String nodeId;

  private Commit commit;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Commit {
    private Author author;
    private Author committer;
    private String message;
    private Tree tree;
    private String url;

    @JsonProperty("comment_count")
    private int commentCount;

    private Verification verification;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Author {
    private String name;
    private String email;
    private String date;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Tree {
    private String sha;
    private String url;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Verification {
    private boolean verified;
    private String reason;
    private String signature;
    private String payload;

    @JsonProperty("verified_at")
    private String verifiedAt;
  }
}
