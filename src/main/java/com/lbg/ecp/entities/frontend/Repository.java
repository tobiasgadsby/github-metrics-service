package com.lbg.ecp.entities.frontend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class Repository {

  private Long id;

  private String name;

  private Double healthQuality;
}
