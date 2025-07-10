package com.lbg.ecp.entities.tables;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
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

  @ManyToOne() private Repository repository;

  @OneToOne(cascade = CascadeType.ALL)
  @JsonIgnore
  private Health health;

  public PullRequest() {}

  public PullRequest(String url, Repository repository, String title, Timestamp createdAt) {
    this.url = url;
    this.repository = repository;
    this.title = title;
    this.createdAt = createdAt;
    this.health = health;
  }
}
