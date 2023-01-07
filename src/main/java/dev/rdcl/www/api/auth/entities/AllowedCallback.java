package dev.rdcl.www.api.auth.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "AllowedCallback")
@Table(name = "auth_allowed_callback")
@NamedQueries({
    @NamedQuery(name = "AllowedCallback.findByUrl", query = """
        select a
        from AllowedCallback a
        where url = :url
        """),
})
public class AllowedCallback {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, insertable = false, updatable = false)
    @ColumnDefault("gen_random_uuid()")
    private UUID id;

    @Column(name = "url", nullable = false, unique = true, length = 511)
    private String url;
}
