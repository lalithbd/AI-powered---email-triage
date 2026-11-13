package org.galactic.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {
    NEEDS_REPLY("Needs reply"),
    FYI("FYI"),
    NEWSLETTER("Newsletter"),
    JUNK("Junk");

    private final String label;
}
