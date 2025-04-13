package org.maxkratz.releasetoissue;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogEntryFormatter extends Formatter {

    @Override
    public String format(final LogRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Given log entry was null.");
        }
        return record.getMessage() + System.lineSeparator();
    }

}
