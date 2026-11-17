package org.galactic.dto.ollama;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.galactic.util.Category;

@RegisterForReflection
public record TriageResult(Category category, String reason, String draft) {
}
