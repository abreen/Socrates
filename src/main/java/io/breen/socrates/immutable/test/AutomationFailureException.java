package io.breen.socrates.immutable.test;

/**
 * When an Automatable test's shouldPass method throws this exception, the test must be handled by a
 * human grader because there was a fatal error trying to run the automation code.
 *
 * Note: this exception indicates a failure condition. If you want to indicate that some normal
 * conditions make an automatable test non-automatable (for any reason), use
 * CannotBeAutomatedException.
 *
 * @see CannotBeAutomatedException
 */
public class AutomationFailureException extends Exception {

    /**
     * The exception that was thrown when automation failed.
     */
    public final Exception e;

    public AutomationFailureException(Exception e) {
        this.e = e;
    }

    public AutomationFailureException(String msg) {
        super(msg);
        e = null;
    }
}
