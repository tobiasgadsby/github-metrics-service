package com.lbg.ecp.service;

import com.lbg.ecp.entities.api.GitCommit;
import com.lbg.ecp.entities.tables.Comment;
import com.lbg.ecp.entities.tables.Health;
import com.lbg.ecp.entities.tables.PullRequest;
import com.lbg.ecp.entities.tables.components.CommentType;
import com.lbg.ecp.repository.BranchRepo;
import com.lbg.ecp.repository.RepositoryRepo;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckService {

  private final RepositoryRepo repositoryRepo;
  private final BranchRepo branchRepo;

  public HealthCheckService(RepositoryRepo repositoryRepo, BranchRepo branchRepo) {
    this.repositoryRepo = repositoryRepo;
    this.branchRepo = branchRepo;
  }

  private static Optional<com.lbg.ecp.entities.tables.Comment> calculateStaleness(
      String branchName, GitCommit commit) {
    if (branchName.equals("main")) {
      return Optional.empty();
    }
    long daysSinceLastCommit =
        calculateDaysOld(Timestamp.from(Instant.parse((commit.getCommit().getAuthor().getDate()))));
    // TODO remove hardcoded values
    if (daysSinceLastCommit > 14) {
      return Optional.of(
          new com.lbg.ecp.entities.tables.Comment(
              true, daysSinceLastCommit + " days since last commit", String.format("Merge or Delete Branch %s.",branchName), CommentType.STALE));
    }
    return Optional.empty();
  }

  private static Optional<Comment> calculateStaleness(PullRequest pullRequest) {
    // TODO remove hardcoded values
    long daysOld = calculateDaysOld(pullRequest.getCreatedAt());
    if (daysOld > -1) {
      return Optional.of(new Comment(true, daysOld + " days since creation", "", CommentType.STALE));
    }
    return Optional.empty();
  }

  private static Long calculateDaysOld(Timestamp timestamp) {
    return ChronoUnit.DAYS.between(timestamp.toLocalDateTime().toLocalDate(), LocalDate.now());
  }

  //  public Health getHealth(Repository repository) {
  //    Health health = new Health(1.0, List.of());
  //    branchRepo.findByRepository(repository).stream().mapToDouble(
  //        branch -> getHealth(branch).getHealthQuality()
  //    )
  //  }

  public com.lbg.ecp.entities.tables.Health calculateHealth(String branchName, GitCommit commit) {
    ArrayList<com.lbg.ecp.entities.tables.Comment> comments = new ArrayList<>();
    calculateStaleness(branchName, commit).ifPresent(comments::add);
    com.lbg.ecp.entities.tables.Health health = new com.lbg.ecp.entities.tables.Health();
    // TODO Remove hardcoded values
    health.setHealthQuality(
        comments.isEmpty()
            ? 1.0
            : 1.0
                - (comments.stream()
                        .filter(com.lbg.ecp.entities.tables.Comment::getIsNegative)
                        .count()
                    * 0.1));
    health.setComments(comments);
    return health;
  }

  public Health calculateHealth(PullRequest pullRequest) {
    ArrayList<com.lbg.ecp.entities.tables.Comment> comments = new ArrayList<>();
    calculateStaleness(pullRequest).ifPresent(comments::add);
    com.lbg.ecp.entities.tables.Health health = new com.lbg.ecp.entities.tables.Health();
    // TODO Remove hardcoded values
    health.setHealthQuality(
            comments.isEmpty()
                    ? 1.0
                    : 1.0
                    - (comments.stream()
                    .filter(com.lbg.ecp.entities.tables.Comment::getIsNegative)
                    .count()
                    * 0.1));
    health.setComments(comments);
    return health;
  }
}
