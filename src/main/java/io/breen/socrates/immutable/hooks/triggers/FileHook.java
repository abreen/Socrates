package io.breen.socrates.immutable.hooks.triggers;

/**
 * Values representing points in the grading process (hooks) having to do with a
 * particular file, to which scripts may be attached, causing the scripts to run
 * at those points in time.
 */
public enum FileHook {

    /**
     * Before any tests of a given file are run.
     */
    BEFORE_TESTS,

    /**
     * After all tests of a given file are run.
     */
    AFTER_TESTS
}
