package dev.rdcl.www.api.label.entities;

import dev.rdcl.www.api.auth.entities.Identity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Label")
@Table(name = "label")
@NamedQueries({
    @NamedQuery(name = "Label.find", query = """
        select l
        from Label l
        where l.owner.id = :owner
        """),

    @NamedQuery(name = "Label.clear", query = """
        delete from Label l
        where l.owner.id = :owner
        """),
})
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, insertable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner", nullable = false, updatable = false)
    private Identity owner;

    @Column(name = "text", unique = true, nullable = false, length = 31)
    private String text;

    @Column(name = "color", length = 31)
    private String color;

    @Column(name = "text_color", length = 31)
    private String textColor;
}
