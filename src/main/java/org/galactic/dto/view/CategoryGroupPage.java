package org.galactic.dto.view;

import java.util.List;

public record CategoryGroupPage(List<CategoryGroup> categoryGroups, int page, int pageCount, int pageSize) {
}
