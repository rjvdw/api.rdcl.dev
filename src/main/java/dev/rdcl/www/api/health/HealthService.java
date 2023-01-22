package dev.rdcl.www.api.health;

import dev.rdcl.www.api.auth.AuthService;
import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.health.entity.Health;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class HealthService {

    private final HealthProperties healthProperties;

    private final AuthService authService;

    private final EntityManager em;

    private final Clock clock;

    public List<Health> listRecent(UUID ownerId) {
        return listRecent(ownerId, LocalDate.now(clock));
    }

    public List<Health> listRecent(UUID ownerId, LocalDate upto) {
        return em
            .createNamedQuery("Health.findRecent", Health.class)
            .setParameter("owner", ownerId)
            .setParameter("now", upto)
            .setMaxResults(healthProperties.maxResults())
            .getResultStream()
            .sorted(Comparator.comparing(Health::getDate))
            .toList();
    }

    public List<Health> list(UUID ownerId, LocalDate from) {
        return em
            .createNamedQuery("Health.findAfter", Health.class)
            .setParameter("owner", ownerId)
            .setParameter("from", from)
            .setMaxResults(healthProperties.maxResults())
            .getResultList();
    }

    public List<Health> list(UUID ownerId, LocalDate from, LocalDate to) {
        return em
            .createNamedQuery("Health.findBetween", Health.class)
            .setParameter("owner", ownerId)
            .setParameter("from", from)
            .setParameter("to", to)
            .setMaxResults(healthProperties.maxResults())
            .getResultList();
    }

    public long count(UUID ownerId) {
        return em
            .createNamedQuery("Health.count", Long.class)
            .setParameter("owner", ownerId)
            .getSingleResult();
    }

    @Transactional
    public void save(LocalDate date, UUID ownerId, String data) {
        Identity owner = authService.getUser(ownerId);

        Health record = em
            .createNamedQuery("Health.findByDate", Health.class)
            .setParameter("owner", ownerId)
            .setParameter("date", date)
            .getResultStream()
            .findFirst()
            .orElseGet(() -> Health.builder()
                .date(date)
                .owner(owner)
                .build());

        record.setData(data);

        em.persist(record);
    }

    @Transactional
    public void delete(LocalDate date, UUID ownerId) {
        em.createNamedQuery("Health.findByDate", Health.class)
            .setParameter("owner", ownerId)
            .setParameter("date", date)
            .getResultStream()
            .findFirst()
            .ifPresent(em::remove);
    }
}
