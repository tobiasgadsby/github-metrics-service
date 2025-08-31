package com.lbg.ecp.entities.tables;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "pull_requests")
public class PullRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String url;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private Timestamp createdAt;

  @Column(nullable = false)
  private String number;

  @ManyToOne() private Repository repository;

  @OneToOne(cascade = CascadeType.ALL)
  @JsonIgnore
  private Health health;

  @OneToMany(mappedBy = "pullRequest", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PullRequestLabel> pullRequestLabels;

  public PullRequest() {}

  public PullRequest(String url, Repository repository, String title, Timestamp createdAt, String number) {
    this.url = url;
    this.repository = repository;
    this.title = title;
    this.createdAt = createdAt;
    this.number = number;
  }
}
