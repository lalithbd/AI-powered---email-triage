package org.galactic.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import lombok.*;
import org.galactic.util.EmailStatus;

import java.time.Instant;

@Entity(name = "email")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Email extends PanacheEntity {

    private String emailMessageId;
    private String threadId;
    private String subject;
    private String body;
    private String sender;
    private int attachmentCount;
    private Instant receivedAt;
    private Instant fetchedAt;
    private int failedCount;

    @Enumerated(value = EnumType.STRING)
    private EmailStatus status;

    @OneToOne(mappedBy = "email")
    private Classification classification;
}
