package com.lbg.ecp.entities.tables;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lbg.ecp.entities.tables.components.CommentType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Setter
@NoArgsConstructor
@Table(name = "Comment")
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "health_id")
  @JsonIgnore
  private Health health;

  @Column private Boolean isNegative;

  @Column private CommentType commentType;

  @Column private String details;

  @Column private String solution;

  public Comment(Boolean isNegative, String details, String solution, CommentType commentType) {
    this.isNegative = isNegative;
    this.details = details;
    this.solution = solution;
    this.commentType = commentType;
  }
}
