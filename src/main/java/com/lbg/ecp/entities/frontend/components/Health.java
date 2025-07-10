package com.lbg.ecp.entities.frontend.components;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class Health {
  private Double healthQuality;
  private List<Comment> comments;
}
