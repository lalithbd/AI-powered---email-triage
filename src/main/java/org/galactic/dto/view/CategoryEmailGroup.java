package org.galactic.dto.view;

import org.galactic.util.Category;

import java.util.List;

public record CategoryEmailGroup(Category category, List<TriageEmail> emails) {
}
