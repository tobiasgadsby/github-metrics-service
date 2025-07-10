package com.lbg.ecp.entities.tables;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "repositories")
public class Repository {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String owner;

  @Column(nullable = false)
  private String fullName;

  @OneToOne(mappedBy = "repository")
  @JsonIgnore
  private WatchedRepository watchedRepository;

  @OneToMany(mappedBy = "repository")
  private List<PullRequest> pullRequests;

  @OneToMany(mappedBy = "repository")
  private List<Branch> branches;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "health_id")
  private Health health;

  public Repository() {}

  public Repository(String name, String owner, String fullName, Health health) {
    this.name = name;
    this.owner = owner;
    this.fullName = fullName;
    this.health = health;
  }
}
