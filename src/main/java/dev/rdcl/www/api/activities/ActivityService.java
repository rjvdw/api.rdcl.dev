package dev.rdcl.www.api.activities;

import dev.rdcl.www.api.activities.entities.Activity;
import dev.rdcl.www.api.auth.AuthService;
import dev.rdcl.www.api.auth.entities.Identity;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class ActivityService {

    private final AuthService authService;

    private final EntityManager em;

    public List<Activity> getUpcomingActivities(UUID owner, Instant when) {
        return em
            .createNamedQuery("Activity.findUpcoming", Activity.class)
            .setParameter("owner", owner)
            .setParameter("when", when)
            .getResultList();
    }

    public List<Activity> getPastActivities(UUID owner, Instant when) {
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
        activity.setId(null);
        activity.setOwner(owner);
        em.persist(activity);
    }

    @Transactional
    public void updateActivity(UUID ownerId, UUID activityId, Activity activity) {
        Identity owner = authService.getUser(ownerId);
        activity.setId(activityId);
        activity.setOwner(owner);
        em.merge(activity);
    }

    @Transactional
    public void deleteActivity(UUID ownerId, UUID activityId) {
        getActivity(ownerId, activityId).ifPresent(em::remove);
    }

}
