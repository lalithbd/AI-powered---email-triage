package org.galactic.dto.ollama;

import org.galactic.util.Category;

public record TriageResult(Category category, String reason, String draft) {
}
