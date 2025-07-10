package com.lbg.ecp.repository;

import com.lbg.ecp.entities.tables.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositoryRepo extends JpaRepository<Repository, Integer> {
  Optional<Repository> findRepositoryByFullName(String fullName);

  List<Repository> findByWatchedRepository_Tenant_TenantCode(String tenantCode);
}
