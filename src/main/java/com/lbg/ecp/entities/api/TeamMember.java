package com.lbg.ecp.entities.api;

import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamMember {

    private Long id;

    private String name;

}
