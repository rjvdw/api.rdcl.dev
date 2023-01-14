package dev.rdcl.www.api.label.dto;

import dev.rdcl.www.api.label.entities.Label;

import javax.validation.constraints.Size;

public record LabelConfig(
    @Size(max = 31)
    String color,

    @Size(max = 31)
    String textColor
) {
    public static LabelConfig from(Label label) {
        return new LabelConfig(label.getColor(), label.getTextColor());
    }
}
