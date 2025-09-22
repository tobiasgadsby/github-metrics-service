package com.lbg.ecp.entities.tables;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Data
@Table(name = "health")
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Health {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  //
  //  @OneToOne(mappedBy = "health", cascade = CascadeType.MERGE)
  //  @JsonIgnore
  //  private Branch branch;

  @OneToMany(mappedBy = "health", cascade = CascadeType.ALL)
  @JsonIgnore
  private List<Comment> comments;

  @Column private Double healthQuality;

  public Health(Double healthQuality) {
    this.healthQuality = healthQuality;
  }

  public Health(double healthQuality, ArrayList<Comment> comments) {
    this.healthQuality = healthQuality;
    this.comments = comments;
  }
}
