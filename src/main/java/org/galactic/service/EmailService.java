package org.galactic.service;

import org.galactic.dto.view.TriageEmail;
import org.galactic.model.Email;
import org.galactic.util.EmailStatus;

import java.io.IOException;
import java.util.List;

public interface EmailService {
    TriageEmail getEmailDataById(Long id);

    Email getEmailById(Long id);

    boolean existsById(Long emailId);

    List<Email> getMailsByType(int page, int size, List<EmailStatus> classifyTypes);

    void fetchMails() throws IOException;

    void setStatus(Long id, EmailStatus emailStatus);
}
