package org.galactic.dto.view;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.galactic.util.Category;

@RegisterForReflection
public record StatChip(Category category, long count) {
}
