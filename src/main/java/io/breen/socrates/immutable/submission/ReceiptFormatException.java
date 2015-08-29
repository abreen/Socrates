package io.breen.socrates.immutable.submission;

/**
 * Thrown if a receipt file has an invalid format. For example, if a receipt file has one or more
 * lines with non-ISO 8601 dates.
 */
public class ReceiptFormatException extends Exception {

    public ReceiptFormatException(String message) {
        super(message);
    }
}
