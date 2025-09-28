package com.lbg.ecp.repository;

import com.lbg.ecp.entities.tables.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMemberRepo extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByTenant_TenantCode(String tenantTenantCode);
}
