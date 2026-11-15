package org.galactic.service.impl;

import com.google.api.client.util.Value;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.galactic.adapter.OllamaAdapter;
import org.galactic.dto.ollama.TriageResult;
import org.galactic.dto.view.CategoryGroup;
import org.galactic.dto.view.StatChip;
import org.galactic.dto.view.Summary;
import org.galactic.dto.view.TriageEmail;
import org.galactic.exception.TriageServiceException;
import org.galactic.model.Classification;
import org.galactic.model.Email;
import org.galactic.repository.ClassificationRepository;
import org.galactic.service.ClassificationService;
import org.galactic.service.EmailService;
import org.galactic.util.Category;
import org.galactic.util.Constant;

import java.time.Instant;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor
public class ClassificationServiceImpl implements ClassificationService {

    @ConfigProperty(name = "quarkus.langchain4j.ollama.chat-model.model-id")
    private String modelCode;

    @ConfigProperty(name = "prompt.version")
    private String promptVersion;

    private final ClassificationRepository classificationRepository;
    private final EmailService emailService;
    private final OllamaAdapter llmService;
    private final ClassificationService self;

    @Override
    public Summary getDailySummary() {
        Instant dayAgo = Instant.now().minus(Period.ofDays(1));
        List<Classification> classifications = classificationRepository.findClassificationMails(dayAgo);
        List<CategoryGroup> categoryGroups = classifications.stream().collect(Collectors.groupingBy(Classification::getCategory,
                HashMap::new, Collectors.mapping(TriageEmail::of, Collectors.toList())))
                .entrySet().stream().map(e -> new CategoryGroup(e.getKey(), e.getValue())).toList();
        List<StatChip> statChips = categoryGroups.stream().map(v -> new StatChip(v.category(), v.emails().size())).toList();

        return new Summary(statChips, categoryGroups);
    }


    @Override
    public List<CategoryGroup> getWeeklyMails(int pageNo, int pageSize, String type) {
        Instant weekAgo = Instant.now().minus(Period.ofWeeks(1));
        List<Classification> classifications;
        if(type == null) {
            classifications = classificationRepository.findClassificationMails(weekAgo, pageNo, pageSize);
        } else {
            try {
                Category category = Category.valueOf(type);
                classifications = classificationRepository.findClassificationMails(weekAgo, pageNo, pageSize, category);
            } catch (IllegalArgumentException e){
                throw new TriageServiceException(Response.Status.BAD_REQUEST, "Invalid Request ");
            }
        }

        return classifications.stream().collect(
                Collectors.groupingBy(Classification::getCategory))
                .entrySet().stream()
                .map(e -> new CategoryGroup(e.getKey(), TriageEmail.ofAll(e.getValue())))
                .toList();
    }


    @Override
    public List<StatChip> getDailyStats() {
        Instant dayAgo = Instant.now().minus(Period.ofDays(1));
        return classificationRepository.getStats(dayAgo);
    }

    @Override
    public List<StatChip> getWeeklyStats() {
        Instant dayAgo = Instant.now().minus(Period.ofWeeks(1));
        return classificationRepository.getStats(dayAgo);
    }

    @Override
    public void classify() {
        int page = 0;
        boolean nextPage = true;
        while (nextPage){
            List<Email> emails = emailService.getMailsByType(page, Constant.CLASSIFY_BATCH_SIZE, Constant.CLASSIFY_TYPES);
            if(emails.size()<Constant.CLASSIFY_BATCH_SIZE) {
                nextPage = false;
            }
            emails.forEach(email -> {
                TriageResult triageResult = llmService.classify(email.getSender(),email.getSubject(),email.getReceivedAt().toString(), email.getBody(), email.getAttachmentCount());
                Classification classification = Classification.builder()
                        .email(email)
                        .classifiedAt(Instant.now()).draft(triageResult.draft()).category(triageResult.category())
                        .reason(triageResult.reason()).modelId(modelCode).promptVersion(promptVersion).build();
                self.persist(classification);
            });
            page++;
        }

    }

    @Transactional
    public void persist(Classification classification){
        classificationRepository.persist(classification);
    }
}
