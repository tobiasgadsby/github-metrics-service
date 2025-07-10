package com.lbg.ecp.entities.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Repository {

  private Integer id;

  private String name;

  @JsonProperty("full_name")
  private String fullName;

  private Owner owner;
}
