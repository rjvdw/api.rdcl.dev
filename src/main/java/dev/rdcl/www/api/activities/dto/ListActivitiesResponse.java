package dev.rdcl.www.api.activities.dto;

import dev.rdcl.www.api.activities.entities.Activity;

import java.util.List;

public record ListActivitiesResponse(
    List<ActivityResponse> activities
) {
    public static ListActivitiesResponse from(List<Activity> activities) {
        return new ListActivitiesResponse(activities
            .stream()
            .map(ActivityResponse::from)
            .toList());
    }
}
