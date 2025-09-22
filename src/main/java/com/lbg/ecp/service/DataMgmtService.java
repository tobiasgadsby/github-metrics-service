package com.lbg.ecp.service;

import com.lbg.ecp.api.GithubApi;
import com.lbg.ecp.entities.tables.Branch;
import com.lbg.ecp.repository.BranchRepo;
import com.lbg.ecp.repository.PullRequestRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DataMgmtService {

    private final BranchRepo branchRepo;
    private final PullRequestRepo pullRequestRepo;
    private final GithubApi githubApi;
    Logger logger = LoggerFactory.getLogger(DataMgmtService.class);

    @Autowired
    public DataMgmtService(BranchRepo branchRepo,
                           PullRequestRepo pullRequestRepo, GithubApi githubApi) {
        this.branchRepo = branchRepo;
        this.pullRequestRepo = pullRequestRepo;
        this.githubApi = githubApi;
    }

    public void deleteComponent(String componentType, Long componentId) {
        logger.info("Delete component {} with id {}", componentType, componentId);
        if (componentType.equals("BRANCH")) {
            branchRepo.findById(componentId).ifPresentOrElse(
                    branch -> {
                        branchRepo.delete(branch);
                        githubApi.deleteBranch(branch.getRepository().getOwner(), branch.getRepository().getName(), branch.getBranchName());
                    }, () -> logger.error("BRANCH NOT FOUND")
            );
        } else {
            pullRequestRepo.findById(componentId).ifPresentOrElse(
                    pullRequest -> {
                        pullRequestRepo.delete(pullRequest);
                        githubApi.deletePullRequest(pullRequest.getRepository().getOwner(), pullRequest.getRepository().getName(), pullRequest.getNumber());
                    }, () -> logger.error("PULL REQUEST NOT FOUND")
            );
        }
    }

}
