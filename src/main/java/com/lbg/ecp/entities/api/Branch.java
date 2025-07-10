package com.lbg.ecp.entities.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Branch {

  private String name;

  @JsonProperty("protected")
  private Boolean isProtected;

  private Commit commit;

  @Data
  public static class Commit {
    private String sha;
    private String url;
  }
}
