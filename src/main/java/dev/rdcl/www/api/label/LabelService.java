package dev.rdcl.www.api.label;

import dev.rdcl.www.api.auth.AuthService;
import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.label.entities.Label;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class LabelService {

    private final EntityManager em;

    private final AuthService authService;

    public List<Label> get(UUID ownerId) {
        return em
            .createNamedQuery("Label.find", Label.class)
            .setParameter("owner", ownerId)
            .getResultList();
    }

    @Transactional
    public void update(UUID ownerId, List<Label> labels) {
        em.createNamedQuery("Label.clear")
            .setParameter("owner", ownerId)
            .executeUpdate();

        Identity owner = authService.getUser(ownerId);

        for (Label label : labels) {
            label.setOwner(owner);
            em.persist(label);
        }
    }
}
