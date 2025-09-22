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
import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Data
@Table(name = "commits")
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Branch {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column private String branchName;

  @ManyToOne() private Repository repository;

  @OneToOne(mappedBy = "branch", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private Commit latestCommit;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private Health health;

}
