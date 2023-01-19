package dev.rdcl.www.api.activities;

import dev.rdcl.www.api.activities.entities.Activity;
import dev.rdcl.www.api.auth.entities.Identity;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
public class ActivityServiceTest {

    private final static Instant NOW = Instant.parse("2000-01-01T12:00:00Z");

    @Inject
    ActivityService activityService;

    @Inject
    EntityManager em;

    @Test
    @TestTransaction
    @DisplayName("Upcoming activities can be listed")
    public void testGetUpcomingActivities() {
        Identity identity = createIdentity();
        createActivities(identity);

        List<Activity> upcomingActivities = activityService.getUpcomingActivities(identity.getId(), NOW);

        assertThat(upcomingActivities, hasSize(7));
        Activity prev = upcomingActivities.get(0);
        for (int i = 0; i < upcomingActivities.size(); i += 1) {
            Activity activity = upcomingActivities.get(i);
            assertThat(activity.getTitle(), anyOf(startsWith("upcoming"), endsWith("in progress")));

            if (i != 0) {
                assertThat(activity.getStarts(), greaterThanOrEqualTo(prev.getStarts()));
                prev = activity;
            }
        }
    }

    @Test
    @TestTransaction
    @DisplayName("Past activities can be listed")
    public void testGetPastActivities() {
        Identity identity = createIdentity();
        createActivities(identity);

        List<Activity> pastActivities = activityService.getPastActivities(identity.getId(), NOW);

        assertThat(pastActivities, hasSize(4));
        Activity prev = pastActivities.get(0);
        for (int i = 0; i < pastActivities.size(); i += 1) {
            Activity activity = pastActivities.get(i);
            assertThat(activity.getTitle(), startsWith("past"));

            if (i != 0) {
                assertThat(activity.getStarts(), lessThanOrEqualTo(prev.getStarts()));
                prev = activity;
            }
        }
    }

    @Test
    @TestTransaction
    @DisplayName("Activities can be retrieved by ID")
    public void testGetActivity() {
        Identity identity = createIdentity();
        List<Activity> activities = createActivities(identity);

        for (Activity activity : activities) {
            Activity retrieved = activityService.getActivity(identity.getId(), activity.getId()).get();

            assertThat(retrieved, equalTo(activity));
        }
    }

    private Identity createIdentity() {
        Identity identity = Identity.builder()
            .name("Test user with activities")
            .email("test.activities@example.com")
            .build();

        em.persist(identity);

        return identity;
    }

    private List<Activity> createActivities(Identity owner) {
        ZonedDateTime startsPast = ZonedDateTime.parse("1998-01-01T12:00:00Z");
        ZonedDateTime startsPresent = ZonedDateTime.parse("2000-01-01T12:00:00Z");
        ZonedDateTime startsFuture = ZonedDateTime.parse("2002-01-01T12:00:00Z");
        ZonedDateTime endsPast = ZonedDateTime.parse("1998-01-01T14:00:00Z");
        ZonedDateTime endsPresent = ZonedDateTime.parse("2000-01-01T23:59:59Z");
        ZonedDateTime endsFuture = ZonedDateTime.parse("2002-01-01T14:00:00Z");

        return List.of(
            createActivity(owner, "past activity without an end date-time", startsPast, false),
            createActivity(owner, "past all-day activity without an end date", startsPast, true),
            createActivity(owner, "past activity with an end date-time", startsPast, endsPast, false),
            createActivity(owner, "past all-day activity with an end date", startsPast, endsPast, true),

            createActivity(owner, "all-day activity without an end date that is in progress", startsPresent, true),
            createActivity(owner, "all-day activity with an end date that is in progress", startsPresent, endsPresent, true),
            createActivity(owner, "activity with an end date-time that is in progress", startsPresent, endsPresent, false),

            createActivity(owner, "upcoming activity without an end date-time", startsFuture, false),
            createActivity(owner, "upcoming all-day activity without an end date", startsFuture, true),
            createActivity(owner, "upcoming activity with an end date-time", startsFuture, endsFuture, false),
            createActivity(owner, "upcoming activity with an end date-time", startsFuture, endsFuture, true)
        );
    }

    private Activity createActivity(Identity owner, String title, ZonedDateTime starts, boolean allDay) {
        return createActivity(owner, title, starts, null, allDay);
    }

    private Activity createActivity(Identity owner, String title, ZonedDateTime starts, ZonedDateTime ends, boolean allDay) {
        Activity activity = Activity.builder()
            .owner(owner)
            .title(title)
            .location("Test location")
            .allDay(allDay)
            .build();

        activity.setZoneId(starts.getZone());
        activity.setStarts(starts);
        activity.setEnds(ends);

        em.persist(activity);

        return activity;
    }

}
