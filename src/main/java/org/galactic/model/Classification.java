package org.galactic.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.*;
import org.galactic.util.Category;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "classification")
public class Classification extends PanacheEntity {

    @Enumerated(EnumType.STRING)
    private Category category;
    private String reason;
    private String draft;
    private Instant classifiedAt;
    private String modelId;
    private String promptVersion;

    @OneToOne
    @JoinColumn(name = "email_id")
    private Email email;

    @OneToOne(mappedBy = "classification")
    private Feedback feedback;

}
