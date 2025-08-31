package com.lbg.ecp.entities.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class PullRequest {

  private String url;

  private String title;

  private String number;

  @JsonProperty("created_at")
  private String createdAt;

  private List<Label> labels;

  @Data
  @JsonIgnoreProperties
  @Getter
  public static class Label {
    private Long id;
    private String name;
    private String description;
    private String color;
  }

}
