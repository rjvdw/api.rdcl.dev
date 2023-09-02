package dev.rdcl.www.api.settings;

import dev.rdcl.www.api.auth.AuthService;
import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.settings.entity.UserSettings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class SettingsService {

    private final AuthService authService;

    private final EntityManager em;

    public String getSettings(UUID ownerId) {
        return em
            .createNamedQuery("UserSettings.get", UserSettings.class)
            .setParameter("owner", ownerId)
            .getResultStream()
            .findAny()
            .map(UserSettings::getSettings)
            .orElse("{}");
    }

    @Transactional
    public void saveSettings(UUID ownerId, String settings) {
        Optional<UserSettings> result = em
            .createNamedQuery("UserSettings.get", UserSettings.class)
            .setParameter("owner", ownerId)
            .getResultStream()
            .findAny();

        if (result.isPresent()) {
            result.get().setSettings(settings);
            em.merge(result.get());
        } else {
            Identity owner = authService.getUser(ownerId);
            UserSettings entity = UserSettings.builder()
                .owner(owner)
                .settings(settings)
                .build();
            em.persist(entity);
        }
    }

}
