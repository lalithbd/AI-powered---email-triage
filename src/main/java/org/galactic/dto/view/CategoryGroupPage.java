package org.galactic.dto.view;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record CategoryGroupPage(List<CategoryGroup> categoryGroups, int page, int pageCount, int pageSize) {
}
