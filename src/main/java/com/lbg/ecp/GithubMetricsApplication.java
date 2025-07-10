package com.lbg.ecp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.lbg.ecp")
public class GithubMetricsApplication {
  public static void main(String[] args) {
    SpringApplication.run(GithubMetricsApplication.class, args);
  }
}
