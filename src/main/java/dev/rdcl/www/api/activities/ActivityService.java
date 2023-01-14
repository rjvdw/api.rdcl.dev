package dev.rdcl.www.api.activities;

import dev.rdcl.www.api.activities.entities.Activity;
import dev.rdcl.www.api.auth.AuthService;
import dev.rdcl.www.api.auth.entities.Identity;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class ActivityService {

    private final AuthService authService;

    private final EntityManager em;

    public List<Activity> getUpcomingActivities(UUID owner, ZonedDateTime when) {
        return em
            .createNamedQuery("Activity.findUpcoming", Activity.class)
            .setParameter("owner", owner)
            .setParameter("when", when)
            .getResultList();
    }

    public List<Activity> getPastActivities(UUID owner, ZonedDateTime when) {
        return em
            .createNamedQuery("Activity.findPast", Activity.class)
            .setParameter("owner", owner)
            .setParameter("when", when)
            .getResultList();
    }

    public Optional<Activity> getActivity(UUID ownerId, UUID activityId) {
        return em
            .createNamedQuery("Activity.findById", Activity.class)
            .setParameter("owner", ownerId)
            .setParameter("id", activityId)
            .getResultStream()
            .findFirst();
    }

    @Transactional
    public void createActivity(UUID ownerId, Activity activity) {
        Identity owner = authService.getUser(ownerId);
        activity.setOwner(owner);
        em.persist(activity);
    }

    @Transactional
    public Optional<Activity> updateActivity(UUID ownerId, UUID activityId, Activity updatedActivity) {
        Optional<Activity> activityOptional = getActivity(ownerId, activityId);

        if (activityOptional.isPresent()) {
            Activity activity = activityOptional.get();

            activity.setTitle(updatedActivity.getTitle());
            activity.setDescription(updatedActivity.getDescription());
            activity.setNotes(updatedActivity.getNotes());
            activity.setUrl(updatedActivity.getUrl());
            activity.setLocation(updatedActivity.getLocation());
            activity.setStarts(updatedActivity.getStarts());
            activity.setEnds(updatedActivity.getEnds());
            activity.setAllDay(updatedActivity.isAllDay());
            activity.setLabels(updatedActivity.getLabels());

            em.persist(activity);
        }

        return activityOptional;
    }

    @Transactional
    public void deleteActivity(UUID ownerId, UUID activityId) {
        Optional<Activity> activityOptional = getActivity(ownerId, activityId);

        if (activityOptional.isPresent()) {
            Activity activity = activityOptional.get();
            em.remove(activity);
        }
    }

}
