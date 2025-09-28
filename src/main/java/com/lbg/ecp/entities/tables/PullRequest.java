package com.lbg.ecp.entities.tables;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;

import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Data
@Table(name = "pull_requests")
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

  @Column(nullable = false)
  private String mergeableState;

  @ManyToOne() private Repository repository;

  @OneToOne(cascade = CascadeType.ALL)
  @JsonIgnore
  private Health health;

  @OneToMany(mappedBy = "pullRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private List<PullRequestLabel> pullRequestLabels;

  @ManyToOne()
  private TeamMember teamMember;

}
