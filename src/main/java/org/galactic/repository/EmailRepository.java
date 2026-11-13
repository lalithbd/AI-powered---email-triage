package org.galactic.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.galactic.model.Email;
import org.galactic.util.EmailStatus;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EmailRepository implements PanacheRepository<Email> {

    public boolean existsById(Long emailId) {
        return findById(emailId) != null;
    }

    public List<Email> getMailsByType(int page, int size, List<EmailStatus> classifyTypes) {
        return find("select e from email e where e.status in :types", Map.of("types", classifyTypes)).page(page, size).list();
    }

    public boolean existsByMessageId(String messageId) {
        return find("emailMessageId", messageId).firstResultOptional().isPresent();
    }

    @Transactional
    public void save(Email email) {
        persist(email);
    }
}
