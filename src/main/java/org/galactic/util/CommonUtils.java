package org.galactic.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class CommonUtils {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public static String formatTime(Instant at) {
        return at == null ? "" : TIME_FMT.format(at.atZone(ZoneOffset.UTC));
    }
}
