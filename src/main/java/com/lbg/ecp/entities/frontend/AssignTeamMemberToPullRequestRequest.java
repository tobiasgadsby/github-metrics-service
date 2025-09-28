package com.lbg.ecp.entities.frontend;

import lombok.Data;

@Data
public class AssignTeamMemberToPullRequestRequest {

    private Long pullRequestId;
    private Long teamMemberId;

}
