package org.galactic.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.*;
import org.galactic.util.Category;

import java.time.Instant;

@Entity(name = "feedback")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback extends PanacheEntity {

    private Category correctedCategory;
    private Instant createdAt;

    @OneToOne
    @JoinColumn(name = "classification_id")
    private Classification classification;


}
