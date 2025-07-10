package com.lbg.ecp.repository;

import com.lbg.ecp.entities.tables.PullRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PullRequestRepo extends JpaRepository<PullRequest, Long> {

  Optional<PullRequest> findPullRequestByUrl(String url);

  List<PullRequest> findPullRequestsByRepositoryId(Long id);
}
