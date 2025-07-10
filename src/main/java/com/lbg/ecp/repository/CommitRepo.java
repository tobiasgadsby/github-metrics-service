package com.lbg.ecp.repository;

import com.lbg.ecp.entities.tables.Commit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitRepo extends JpaRepository<Commit, Integer> {
  Commit findCommitByBranchId(Integer branchId);
}
