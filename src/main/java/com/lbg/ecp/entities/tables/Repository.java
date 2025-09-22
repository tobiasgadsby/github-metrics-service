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

import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Data
@Table(name = "repositories")
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

  @OneToOne(mappedBy = "repository", cascade = CascadeType.MERGE)
  @JsonIgnore
  private WatchedRepository watchedRepository;

  @OneToMany(mappedBy = "repository")
  private List<PullRequest> pullRequests;

  @OneToMany(mappedBy = "repository")
  private List<Branch> branches;

  @OneToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "health_id")
  private Health health;

}
