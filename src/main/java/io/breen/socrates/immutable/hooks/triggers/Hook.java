package io.breen.socrates.immutable.hooks.triggers;

/**
 * Values representing points in the grading process (hooks), to which scripts may be attached,
 * causing the scripts to run at those points in time.
 */
public enum Hook {

    /**
     * After a criteria file or package is loaded, but before submissions are loaded.
     */
    AFTER_CRITERIA_LOAD("after_criteria_load"),

    /**
     * After a criteria file or package is loaded and after submissions are loaded.
     */
    BEFORE_GRADING("before_grading"),

    /**
     * Before Socrates exits.
     */
    AFTER_GRADING("after_grading");

    public final String type;

    Hook(String type) {
        this.type = type;
    }

    public static Hook fromString(String type) {
        for (Hook h : Hook.values())
            if (h.type.equals(type)) return h;

        return null;
    }

    @Override
    public String toString() {
        return type;
    }
}
