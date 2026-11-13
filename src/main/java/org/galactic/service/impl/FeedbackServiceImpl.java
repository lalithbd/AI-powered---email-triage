package org.galactic.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galactic.dto.view.FeedbackCategory;
import org.galactic.dto.view.TriageEmail;
import org.galactic.exception.TriageServiceException;
import org.galactic.model.Classification;
import org.galactic.model.Email;
import org.galactic.model.Feedback;
import org.galactic.repository.FeedbackRepository;
import org.galactic.service.EmailService;
import org.galactic.service.FeedbackService;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void addFeedbackByEmailId(Long emailId, FeedbackCategory feedbackCategory) {
        Email email = emailService.getEmailById(emailId);
        if(email == null){
            log.warn("No email for id : {}", emailId);
            throw new TriageServiceException(Response.Status.BAD_REQUEST, "No email found to add feedback");
        }
        Classification classification = email.getClassification();
        if(classification == null) {
            log.warn("No classification for email id : {}", emailId);
            throw new TriageServiceException(Response.Status.BAD_REQUEST, "No classification found to add feedback");
        }
        email.id = emailId;
        Feedback feedback = Feedback.builder().classification(classification)
                .correctedCategory(feedbackCategory.category())
                .build();
        feedbackRepository.persist(feedback);
    }
}
