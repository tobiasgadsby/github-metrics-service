package com.lbg.ecp.entities.tables;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Data
@Table(name = "pull_request_labels")
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}