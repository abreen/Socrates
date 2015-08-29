package io.breen.socrates.constructor;

/**
 * Thrown when the criteria requires a resource (for example, a script needed for a script-based
 * test) but it cannot be found in the criteria package.
 */
public class MissingResourceException extends RuntimeException {

    public MissingResourceException(String fileName) {
        super("missing required resource: " + fileName);
    }
}
