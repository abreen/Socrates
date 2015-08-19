package io.breen.socrates.immutable.test;

/**
 * When an Automatable test's shouldPass method throws this exception, it effectively
 * indicates that the test must be handled by a human grader instead.
 */
public class CannotBeAutomatedException extends Exception {}
