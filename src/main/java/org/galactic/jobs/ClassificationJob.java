package org.galactic.jobs;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galactic.service.ClassificationService;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@ApplicationScoped
@AllArgsConstructor
public class ClassificationJob {

    private static final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final ClassificationService classificationService;

    @Scheduled(every = "${job.classify.everyExpression}")
    public void classify(){
        if(isRunning.get()){
            log.info("Previous job still running");
            return;
        }
        isRunning.set(true);
        try {
            classificationService.classify();
        } catch (Exception e){
            log.error("Error: {}", e.getMessage());
        } finally {
            isRunning.set(false);
        }


    }
}
