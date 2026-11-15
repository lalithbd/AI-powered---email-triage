package org.galactic.service.impl;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galactic.adapter.GoogleAdapter;
import org.galactic.dto.view.TriageEmail;
import org.galactic.exception.TriageServiceException;
import org.galactic.model.Email;
import org.galactic.repository.EmailRepository;
import org.galactic.service.EmailService;
import org.galactic.util.EmailStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailRepository emailRepository;
    private final GoogleAdapter googleAdapter;

    @Override
    public TriageEmail getEmailDataById(Long id) {
        return TriageEmail.of(getEmailById(id));
    }

    @Override
    public Email getEmailById(Long id) {
        Email email = emailRepository.findById(id);
        if(email == null) {
            log.warn("No email record under id : {}", id);
            throw new TriageServiceException(Response.Status.BAD_REQUEST, "No email record");
        }
        return email;
    }

    @Override
    public boolean existsById(Long emailId) {
        return emailRepository.existsById(emailId);
    }

    @Override
    public List<Email> getMailsByType(int page, int size, List<EmailStatus> classifyTypes) {
        return emailRepository.getMailsByType(page, size, classifyTypes);
    }


    @Override
    public void fetchMails() {
        List<Message> summaries;
        try {
            summaries = googleAdapter.getMessages();
        } catch (IOException e) {
            throw new TriageServiceException(Response.Status.INTERNAL_SERVER_ERROR, "Gmail list failed: " + e.getMessage());
        }

        int fetched = 0;
        int dupes = 0;
        int failed = 0;
        for (Message summary : summaries) {
            if (emailRepository.existsByMessageId(summary.getId())) {
                dupes++;
                continue;
            }
            try {

                emailRepository.save(toEmail(googleAdapter.getMessage(summary.getId())));
                fetched++;
            } catch (IOException | RuntimeException e) {
                log.error(e.getMessage());
                failed++;                       // per-item failure doesn't abort the run
            }
        }
        log.info("fetchMails: fetched={} dupes={} failed={} listed={}", fetched, dupes, failed, summaries.size());
    }

    @Override
    @Transactional
    public void markFailed(Long id, EmailStatus emailStatus) {
        Email.update("status = ?1, failedCount = failedCount + 1 where id = ?2", emailStatus, id);
    }

    private Email toEmail(Message msg) {
        var payload = msg.getPayload();
        Map<String, String> headers = new HashMap<>();
        if (payload != null && payload.getHeaders() != null) {
            payload.getHeaders().forEach(h -> headers.put(h.getName(), h.getValue()));
        }
        return Email.builder()
                .emailMessageId(msg.getId())
                .threadId(msg.getThreadId())
                .subject(headers.get("Subject"))
                .sender(headers.get("From"))
                .body(extractBody(payload, msg.getSnippet()))
                .attachmentCount(countAttachments(payload))
                .receivedAt(msg.getInternalDate() != null
                        ? Instant.ofEpochMilli(msg.getInternalDate()) : null)
                .fetchedAt(Instant.now())
                .status(EmailStatus.FRESH)
                .build();
    }

    private String extractBody(MessagePart part, String fallback) {
        if (part == null) return fallback;
        if (MediaType.TEXT_PLAIN.equals(part.getMimeType()) && part.getBody() != null
                && part.getBody().getData() != null) {
            return new String(Base64.getUrlDecoder().decode(part.getBody().getData()),
                    StandardCharsets.UTF_8);
        }
        if (part.getParts() != null) {
            for (MessagePart p : part.getParts()) {
                String body = extractBody(p, null);
                if (body != null) return body;
            }
        }
        return fallback;
    }

    private int countAttachments(MessagePart part) {
        if (part == null || part.getParts() == null) return 0;
        int count = 0;
        for (MessagePart p : part.getParts()) {
            count += (p.getFilename() != null && !p.getFilename().isEmpty()) ? 1 : 0;
            count += countAttachments(p);
        }
        return count;
    }
}
