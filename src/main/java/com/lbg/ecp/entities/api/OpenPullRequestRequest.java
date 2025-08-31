package com.lbg.ecp.entities.api;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OpenPullRequestRequest {

    private String title;
    private String head;
    private String base;
    private String body;

}
