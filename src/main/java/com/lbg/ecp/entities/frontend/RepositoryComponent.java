package com.lbg.ecp.entities.frontend;

import com.lbg.ecp.entities.frontend.components.ComponentType;
import com.lbg.ecp.entities.frontend.components.Health;
import com.lbg.ecp.entities.frontend.components.Label;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Setter
@AllArgsConstructor
public class RepositoryComponent {

  private String name;

  private Long componentId;

  private Health health;

  private ComponentType componentType;

  private List<Label> labels;

  public RepositoryComponent() {

  }
}
