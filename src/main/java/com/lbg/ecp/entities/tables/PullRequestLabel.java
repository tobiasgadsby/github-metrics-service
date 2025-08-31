package com.lbg.ecp.entities.tables;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PullRequestLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column
    private String color;

    @ManyToOne private PullRequest pullRequest;

    public PullRequestLabel(String name, String description, PullRequest pullRequest, String color) {
        this.name = name;
        this.description = description;
        this.pullRequest = pullRequest;
        this.color = color;
    }

    public PullRequestLabel() {

    }
}