package com.lbg.ecp.repository;

import com.lbg.ecp.entities.tables.Repository;
import com.lbg.ecp.entities.tables.Tenant;
import com.lbg.ecp.entities.tables.WatchedRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchedRepositoryRepo extends JpaRepository<WatchedRepository, Integer> {
  Optional<WatchedRepository> findByTenantAndRepository(Tenant tenant, Repository repository);

  Boolean existsByTenantAndRepository(Tenant tenant, Repository repository);

  List<WatchedRepository> findByTenant(Tenant tenant);
}
