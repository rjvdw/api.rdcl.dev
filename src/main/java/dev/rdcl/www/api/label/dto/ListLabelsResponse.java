package dev.rdcl.www.api.label.dto;

import dev.rdcl.www.api.label.entities.Label;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ListLabelsResponse(Map<String, String> labels) {
    public static ListLabelsResponse from(List<Label> labels) {
        return new ListLabelsResponse(labels
                .stream()
                .collect(Collectors.toMap(Label::getText, Label::getSettings)));
    }
}
