package com.lbg.ecp.repository;

import com.lbg.ecp.entities.tables.Branch;
import com.lbg.ecp.entities.tables.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BranchRepo extends JpaRepository<Branch, Long> {
  Optional<Branch> findBranchByRepositoryAndBranchName(Repository repository, String branchName);

  List<Branch> findByRepository_WatchedRepository_Tenant_Id(Integer tenantId);

  List<Branch> findByRepository(Repository repository);

  List<Branch> findBranchesByRepository_Id(Long repositoryId);
}
