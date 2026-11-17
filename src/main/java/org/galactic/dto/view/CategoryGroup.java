package org.galactic.dto.view;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.galactic.util.Category;

@RegisterForReflection
public record CategoryGroup(Category category, List<TriageEmail> emails) {
}
