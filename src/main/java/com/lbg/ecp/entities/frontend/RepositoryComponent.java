package com.lbg.ecp.entities.frontend;

import com.lbg.ecp.entities.frontend.components.ComponentType;
import com.lbg.ecp.entities.frontend.components.Health;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Setter
@AllArgsConstructor
public class RepositoryComponent {

  private String name;

  private Health health;

  private ComponentType componentType;

  public RepositoryComponent() {

  }
}
