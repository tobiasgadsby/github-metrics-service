package com.lbg.ecp.entities.frontend.components;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Comment {
  private Boolean isNegative;
  private String details;
  private String solution;
}
