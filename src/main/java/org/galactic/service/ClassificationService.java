package org.galactic.service;

import org.galactic.dto.view.*;
import org.galactic.model.Classification;

import java.util.List;

public interface ClassificationService {
    Summary getDailySummary();

    List<CategoryGroup> getWeeklyMails(int pageNo, int pageSize, String type);

    List<StatChip> getDailyStats();

    List<StatChip> getWeeklyStats();

    void classify();

    void persist(Classification classification);
}
