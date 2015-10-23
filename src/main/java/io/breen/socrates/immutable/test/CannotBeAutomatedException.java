package io.breen.socrates.immutable.test;

/**
 * When an Automatable test's shouldPass method throws this exception, it effectively indicates that
 * the test must be handled by a human grader instead.
 *
 * Note: this exception does not indicate a failure condition. This exception merely causes the test
 * to lose its automatable status.
 */
public class CannotBeAutomatedException extends Exception {

    public CannotBeAutomatedException(String s) {
        super(s);
    }
}
