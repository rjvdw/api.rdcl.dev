package dev.rdcl.www.api.health;

import dev.rdcl.www.api.auth.AuthService;
import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.health.entity.Health;
import dev.rdcl.www.api.health.entity.HealthSettings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class HealthService {

    private final HealthProperties healthProperties;

    private final AuthService authService;

    private final EntityManager em;

    public String getSettings(UUID ownerId) {
        return em
                .createNamedQuery("HealthSettings.get", HealthSettings.class)
                .setParameter("owner", ownerId)
                .getResultStream()
                .findAny()
                .map(HealthSettings::getSettings)
                .orElse("{}");
    }

    @Transactional
    public void saveSettings(UUID ownerId, String settings) {
        Optional<HealthSettings> result = em
                .createNamedQuery("HealthSettings.get", HealthSettings.class)
                .setParameter("owner", ownerId)
                .getResultStream()
                .findAny();

        if (result.isPresent()) {
            result.get().setSettings(settings);
            em.merge(result.get());
        } else {
            Identity owner = authService.getUser(ownerId);
            HealthSettings entity = HealthSettings.builder()
                    .owner(owner)
                    .settings(settings)
                    .build();
            em.persist(entity);
        }
    }

    public List<Health> findBefore(UUID ownerId, LocalDate to) {
        return em
                .createNamedQuery("Health.findBefore", Health.class)
                .setParameter("owner", ownerId)
                .setParameter("to", to)
                .setMaxResults(healthProperties.maxResults())
                .getResultStream()
                .sorted(Comparator.comparing(Health::getDate))
                .toList();
    }

    public List<Health> findAfter(UUID ownerId, LocalDate from) {
        return em
                .createNamedQuery("Health.findAfter", Health.class)
                .setParameter("owner", ownerId)
                .setParameter("from", from)
                .setMaxResults(healthProperties.maxResults())
                .getResultList();
    }

    public List<Health> findBetween(UUID ownerId, LocalDate from, LocalDate to) {
        return em
                .createNamedQuery("Health.findBetween", Health.class)
                .setParameter("owner", ownerId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setMaxResults(healthProperties.maxResults())
                .getResultList();
    }

    public long countBefore(UUID ownerId, LocalDate to) {
        return em
                .createNamedQuery("Health.countBefore", Long.class)
                .setParameter("owner", ownerId)
                .setParameter("to", to)
                .getSingleResult();
    }

    public long countAfter(UUID ownerId, LocalDate from) {
        return em
                .createNamedQuery("Health.countAfter", Long.class)
                .setParameter("owner", ownerId)
                .setParameter("from", from)
                .getSingleResult();
    }

    public long countBetween(UUID ownerId, LocalDate from, LocalDate to) {
        return em
                .createNamedQuery("Health.countBetween", Long.class)
                .setParameter("owner", ownerId)
                .setParameter("from", from)
                .setParameter("to", to)
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
