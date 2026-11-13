package org.galactic.service;

import org.galactic.dto.view.FeedbackCategory;

public interface FeedbackService {

    void addFeedbackByEmailId(Long id, FeedbackCategory feedbackCategory);

}
