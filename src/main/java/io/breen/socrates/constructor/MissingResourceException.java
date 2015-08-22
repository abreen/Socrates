package io.breen.socrates.constructor;

public class MissingResourceException extends RuntimeException {
    public MissingResourceException(String fileName) {
        super("missing required resource: " + fileName);
    }
}
