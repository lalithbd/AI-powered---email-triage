package org.galactic.dto.view;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.galactic.model.Classification;
import org.galactic.model.Email;
import org.galactic.util.Category;

import java.time.Instant;
import java.util.List;


@RegisterForReflection
public record TriageEmail(long id,
                          String sender,
                          String subject,
                          Instant receivedAt,
                          String snippet,
                          String body,
                          Category category,
                          Category correctedCategory,
                          String reason,
                          String draft) {


    public static TriageEmail of(Classification c) {
        Email e = c.getEmail();
        Category feedback = c.getFeedback() != null? c.getFeedback().getCorrectedCategory(): null;
        return new TriageEmail(e.id, e.getSender(), e.getSubject(),
                e.getReceivedAt(), snippetOf(e.getBody()), e.getBody(),
                c.getCategory(), feedback, c.getReason(), c.getDraft());
    }

    public static TriageEmail of(Email e) {
        Classification c = e.getClassification();
        Category feedback = c.getFeedback() != null? c.getFeedback().getCorrectedCategory(): null;
        return new TriageEmail(e.id, e.getSender(), e.getSubject(),
                e.getReceivedAt(), snippetOf(e.getBody()), e.getBody(),
                c.getCategory(), feedback, c.getReason(), c.getDraft());
    }

    public static List<TriageEmail> ofAll(List<Classification> classifications) {
        return classifications.stream().map(TriageEmail::of).toList();
    }

    private static String snippetOf(String body) {
        return body == null ? "" : body.substring(0, Math.min(120, body.length()));
    }
}
