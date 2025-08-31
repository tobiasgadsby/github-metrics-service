package com.lbg.ecp.entities.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenPullRequestResponse {

    @JsonProperty("html_url")
    private String htmlUrl;

}
