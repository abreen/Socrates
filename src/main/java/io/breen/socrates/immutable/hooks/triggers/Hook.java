package io.breen.socrates.immutable.hooks.triggers;

/**
 * Values representing points in the grading process (hooks), to which scripts may
 * be attached, causing the scripts to run at those points in time.
 */
public enum Hook {

    /**
     * After a criteria file or package is loaded, but before submissions are loaded.
     */
    BEFORE_FILE_SEARCH,

    /**
     * After a criteria file or package is loaded and after submissions are loaded.
     */
    BEFORE_GRADING,

    /**
     * Before Socrates exits.
     */
    AFTER_GRADING
}
