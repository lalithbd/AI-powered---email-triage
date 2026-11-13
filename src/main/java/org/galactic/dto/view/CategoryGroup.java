package org.galactic.dto.view;

import java.util.List;

import org.galactic.util.Category;

public record CategoryGroup(Category category, List<TriageEmail> emails) {
}
