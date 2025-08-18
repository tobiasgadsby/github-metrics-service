package com.lbg.ecp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Value("${env.github.auth-token}")
  public String githubAuthToken;

  @Bean
  public RestClient restClient(RestClient.Builder builder) {
    return builder
        .baseUrl("https://api.github.com")
        .defaultHeader("Authorization", "Bearer " + githubAuthToken)
        .defaultHeader("Accept", "application/vnd.github+json")
        .build();
  }
}
