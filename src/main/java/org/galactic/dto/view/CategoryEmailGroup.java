package org.galactic.dto.view;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.galactic.util.Category;

import java.util.List;

@RegisterForReflection
public record CategoryEmailGroup(Category category, List<TriageEmail> emails) {
}
