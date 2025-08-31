package com.lbg.ecp.entities.frontend.components;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Label {

    private String name;

    private String description;

    private String color;

    public Label() {}

}
