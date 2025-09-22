package com.lbg.ecp.entities.tables;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Cascade;

@Entity
@Data
@Table(name = "watched_repositories")
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchedRepository {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(cascade = CascadeType.MERGE)
  private Tenant tenant;

  @OneToOne(cascade = CascadeType.MERGE)
  private Repository repository;

}
