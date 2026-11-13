package org.galactic.jobs;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galactic.service.ClassificationService;
import org.galactic.service.EmailService;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@ApplicationScoped
@AllArgsConstructor
public class EmailFetchJob {

    private static final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final EmailService emailService;

    @Scheduled(every = "${job.mail-fetch.everyExpression}")
    public void classify(){
        if(isRunning.get()){
            log.info("Previous job still running");
            return;
        }
        isRunning.set(true);
        try {
            emailService.fetchMails();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            isRunning.set(false);
        }

    }
}
