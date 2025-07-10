package com.lbg.ecp.repository;

import com.lbg.ecp.entities.tables.Tenant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepo extends JpaRepository<Tenant, Integer> {

  Optional<Tenant> findByTenantCode(String tenantCode);
}
