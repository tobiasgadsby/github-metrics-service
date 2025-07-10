package com.lbg.ecp.entities.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PullRequest {

  private String url;

  private String title;

  @JsonProperty("created_at")
  private String createdAt;
}
