package org.galactic.util;

import java.util.List;

public class Constant {
    private Constant() {
    }

    public static final int CLASSIFY_BATCH_SIZE = 10;
    public static final List<EmailStatus> CLASSIFY_TYPES = List.of(EmailStatus.FRESH, EmailStatus.RETRY);

}
