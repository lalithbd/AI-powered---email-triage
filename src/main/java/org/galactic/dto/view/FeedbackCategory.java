package org.galactic.dto.view;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.galactic.util.Category;

@RegisterForReflection
public record FeedbackCategory(Category category) {
}
