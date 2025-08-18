package com.lbg.ecp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.lbg.ecp")
@EnableJpaRepositories("com.lbg.ecp.*")
public class GithubMetricsApplication {
  public static void main(String[] args) {
    SpringApplication.run(GithubMetricsApplication.class, args);
  }
}
