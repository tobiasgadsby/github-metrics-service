package com.lbg.ecp.entities.tables;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "watched_repositories")
public class WatchedRepository {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne private Tenant tenant;

  @OneToOne private Repository repository;

  public WatchedRepository() {
    // No Args constructor for Hibernate.
  }

  public WatchedRepository(Tenant tenant, Repository repository) {
    this.tenant = tenant;
    this.repository = repository;
  }
}
