package com.lbg.ecp;

import com.lbg.ecp.api.GithubApi;
import com.lbg.ecp.entities.tables.Health;
import com.lbg.ecp.entities.tables.Repository;
import com.lbg.ecp.entities.tables.Tenant;
import com.lbg.ecp.entities.tables.WatchedRepository;
import com.lbg.ecp.repository.CommentRepository;
import com.lbg.ecp.repository.PullRequestRepo;
import com.lbg.ecp.repository.RepositoryRepo;
import com.lbg.ecp.repository.TenantRepo;
import com.lbg.ecp.repository.WatchedRepositoryRepo;
import com.lbg.ecp.service.DataLoadService;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupDataLoader implements ApplicationRunner {

  private static final Logger LOG = LogManager.getLogger(StartupDataLoader.class);

  private final WatchedRepositoryRepo watchedRepositoryRepo;
  private final TenantRepo tenantRepo;
  private final RepositoryRepo repositoryRepo;
  private final GithubApi githubApi;
  private final PullRequestRepo pullRequestRepo;
  private final DataLoadService dataLoadService;
  private final CommentRepository commentRepository;

  @Value("${env.tenant-code}")
  private String tenantCode;

  @Value("${env.add-by-prefix:false}")
  private boolean addByPrefix;

  @Value("${env.prefix:}")
  private String prefix;

  @Value("${env.watch-prefix-repos:false}")
  private boolean watchPrefixRepos;

  @Autowired
  public StartupDataLoader(
      WatchedRepositoryRepo watchedRepositoryRepo,
      TenantRepo tenantRepo,
      RepositoryRepo repositoryRepo,
      GithubApi githubApi,
      PullRequestRepo pullRequestRepo,
      DataLoadService dataLoadService, CommentRepository commentRepository) {
    this.watchedRepositoryRepo = watchedRepositoryRepo;
    this.tenantRepo = tenantRepo;
    this.repositoryRepo = repositoryRepo;
    this.githubApi = githubApi;
    this.pullRequestRepo = pullRequestRepo;
    this.dataLoadService = dataLoadService;
    this.commentRepository = commentRepository;
  }

  @Override
  public void run(ApplicationArguments args) {

    //CLEANUP
    commentRepository.deleteAllInBatch();

    // Get tenant based on tenant code, or created a new tenant entity and save to DB if not
    // present.
    Tenant tenant =
        tenantRepo
            .findByTenantCode(tenantCode)
            .orElseGet(() -> tenantRepo.save(new Tenant(tenantCode)));

    // If addbyprefix option is selected, service will query for all repositories based on set
    // prefix.
    if (addByPrefix) {
      LOG.info("REPOSITORIES BY SEARCH");
      // get all repositories based on prefix.
      final List<com.lbg.ecp.entities.api.Repository> repositorySearchResults =
          githubApi.getAuthenticatedUsersRepositories();

      repositorySearchResults.forEach(
          searchRepository -> {
            // check if already present in the database, if not create a new repository entity.
            final Repository repository =
                repositoryRepo
                    .findRepositoryByFullName(searchRepository.getFullName())
                    .orElseGet(
                        () ->
                            repositoryRepo.save(
                                new Repository(
                                    searchRepository.getName(),
                                    searchRepository.getOwner().getLogin(),
                                    searchRepository.getFullName(),
                                    new Health(1.0))));

            // If the setting is enabled to add searched repos to the watchlist.
            // check if already present in the database, if not create a new watched repository
            // entity.
            if (watchPrefixRepos
                && !watchedRepositoryRepo.existsByTenantAndRepository(tenant, repository)) {
              watchedRepositoryRepo.save(new WatchedRepository(tenant, repository));
            }
          });
    }

    List<WatchedRepository> watchedRepositories = watchedRepositoryRepo.findByTenant(tenant);
    watchedRepositories.forEach(
        watchedRepository ->
            dataLoadService.updateRepositoryData(
                repositoryRepo.findById(watchedRepository.getId()).get()));
  }
}
