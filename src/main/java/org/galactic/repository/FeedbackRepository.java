package org.galactic.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.galactic.model.Feedback;

@ApplicationScoped
public class FeedbackRepository implements PanacheRepository<Feedback> {
}
