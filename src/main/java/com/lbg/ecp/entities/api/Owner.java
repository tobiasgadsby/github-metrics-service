package com.lbg.ecp.entities.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Owner {

  private String login;
  private Long id;

  @JsonProperty("node_id")
  private String nodeId;

  private String url;
}
