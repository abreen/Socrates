package io.breen.socrates.immutable.hooks.triggers;

/**
 * Values representing points in the grading process (hooks) having to do with a
 * particular test, to which scripts may be attached, causing the scripts to run
 * at those points in time.
 */
public enum TestHook {

    /**
     * Before any tests of a given file are run.
     */
    BEFORE_TEST,

    /**
     * After all tests of a given file are run.
     */
    AFTER_TEST
}
