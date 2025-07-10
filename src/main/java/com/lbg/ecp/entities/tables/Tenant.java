package com.lbg.ecp.entities.tables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;

@Entity
@Getter
@Table(name = "tenants")
public class Tenant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false)
  private String tenantCode;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "tenant")
  private List<WatchedRepository> watchedRepositories;

  public Tenant() {
    // No Args constructor for Hibernate.
  }

  public Tenant(String tenantCode) {
    this.tenantCode = tenantCode;
  }
}
