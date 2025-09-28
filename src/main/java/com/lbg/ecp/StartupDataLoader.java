package com.lbg.ecp;

import com.lbg.ecp.api.GithubApi;
import com.lbg.ecp.entities.api.Branch;
import com.lbg.ecp.entities.api.PullRequest;
import com.lbg.ecp.entities.tables.Repository;
import com.lbg.ecp.entities.tables.TeamMember;
import com.lbg.ecp.entities.tables.Tenant;
import com.lbg.ecp.entities.tables.WatchedRepository;
import com.lbg.ecp.repository.*;
import com.lbg.ecp.service.DataLoadService;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final BranchRepo branchRepo;
    private final TeamMemberRepo teamMemberRepo;

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
            DataLoadService dataLoadService, CommentRepository commentRepository, BranchRepo branchRepo, TeamMemberRepo teamMemberRepo) {
        this.watchedRepositoryRepo = watchedRepositoryRepo;
        this.tenantRepo = tenantRepo;
        this.repositoryRepo = repositoryRepo;
        this.githubApi = githubApi;
        this.pullRequestRepo = pullRequestRepo;
        this.dataLoadService = dataLoadService;
        this.commentRepository = commentRepository;
        this.branchRepo = branchRepo;
        this.teamMemberRepo = teamMemberRepo;
    }

    @Override
    public void run(ApplicationArguments args) {

        executeFullRefresh();

    }

    public void executeFullRefresh() {

        // Get tenant based on tenant code, or created a new tenant entity and save to DB if not
        // present.
        Tenant tenant =
                tenantRepo
                        .findByTenantCode(tenantCode)
                        .orElseGet(() -> tenantRepo.save(new Tenant(tenantCode)));

        //TODO Update to remove dummy data.
        List<TeamMember> teamMembers = List.of(
                TeamMember.builder().tenant(tenant).name("John").build(),
                TeamMember.builder().tenant(tenant).name("Paul").build(),
                TeamMember.builder().tenant(tenant).name("James").build(),
                TeamMember.builder().tenant(tenant).name("Ringo").build()
        );
        teamMemberRepo.saveAll(teamMembers);

        // If addbyprefix option is selected, service will query for all repositories based on set
        // prefix.
        LOG.info("REPOSITORIES BY SEARCH");
        // get all repositories based on prefix.
        final List<com.lbg.ecp.entities.api.Repository> repositorySearchResults =
                githubApi.getAuthenticatedUsersRepositories();

        // For each repository pulled based on the prefix, update its data and related data e.g PullRequests.
        repositorySearchResults.forEach(
                searchRepository -> {

                    Repository updatedOrCreatedRepository = dataLoadService.updateRepositoryBaseDetails(searchRepository);

                    //Update Pull Requests for this Repository.
                    List<PullRequest> newPullRequests = githubApi.getPullRequests(searchRepository.getOwner().getLogin(), searchRepository.getName());
                    List<com.lbg.ecp.entities.tables.PullRequest> existingPullRequests = pullRequestRepo.findPullRequestsByRepositoryId(updatedOrCreatedRepository.getId());
                    Set<String> validUrls = newPullRequests.stream().map(PullRequest::getUrl).collect(Collectors.toSet());

                    List<com.lbg.ecp.entities.tables.PullRequest> stalePullRequests =
                            existingPullRequests.stream()
                                            .filter(
                                                    pullRequest -> !validUrls.contains(pullRequest.getUrl())
                                            ).toList();

                    //Removes closed pull requests
                    if(!stalePullRequests.isEmpty()) {

                        updatedOrCreatedRepository.getPullRequests().removeAll(stalePullRequests);
                        pullRequestRepo.deleteAllInBatch(stalePullRequests);

                    }

                    updatedOrCreatedRepository.setPullRequests(
                            newPullRequests.stream().map(
                                    pullRequest -> dataLoadService.updatePullRequest(updatedOrCreatedRepository, pullRequest)
                            ).toList()
                    );


                    if (!watchedRepositoryRepo.existsByTenantAndRepository(tenant, updatedOrCreatedRepository)) {
                        // Mark as a watched repository by the current tenant.
                        watchedRepositoryRepo.save(
                                WatchedRepository.builder()
                                        .tenant(tenant)
                                        .repository(updatedOrCreatedRepository)
                                        .build()
                        );
                    }



                    List<Branch> newBranches = githubApi.getBranches(searchRepository.getOwner().getLogin(), searchRepository.getName());
                    List<com.lbg.ecp.entities.tables.Branch> existingBranches = branchRepo.findBranchesByRepository_Id(updatedOrCreatedRepository.getId());
                    Set<String> validBranchNames =  newBranches.stream().map(Branch::getName).collect(Collectors.toSet());

                    List<com.lbg.ecp.entities.tables.Branch> staleBranches = existingBranches.stream().filter(
                            branch -> !validBranchNames.contains(branch.getBranchName())
                    ).toList();

                    if(!staleBranches.isEmpty()) {

                        updatedOrCreatedRepository.getBranches().removeAll(staleBranches);
                        branchRepo.deleteAllInBatch(staleBranches);

                    }

                    updatedOrCreatedRepository.setBranches(
                            newBranches.stream().map(
                                    branch -> dataLoadService.updateBranch(updatedOrCreatedRepository, branch)
                            ).toList()
                    );

                });
    }
}
