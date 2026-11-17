package org.galactic.dto.view;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Summary(List<StatChip> statChips, List<CategoryGroup> categoryGroups) {
}
