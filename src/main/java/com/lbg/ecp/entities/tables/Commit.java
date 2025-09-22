package com.lbg.ecp.entities.tables;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;

import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Data
@Table(name = "Commits")
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Commit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne(cascade = CascadeType.MERGE)
  @JsonIgnore
  private Branch branch;

  @Column private String commitSha;

  @Column private Timestamp commitDate;

  public Commit(String latestCommitSha, Branch branch, Timestamp commitDate) {
    this.commitSha = latestCommitSha;
    this.branch = branch;
    this.commitDate = commitDate;
  }
}
