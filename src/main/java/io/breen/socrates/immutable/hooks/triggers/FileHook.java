package io.breen.socrates.immutable.hooks.triggers;

/**
 * Values representing points in the grading process (hooks) having to do with a particular file, to
 * which scripts may be attached, causing the scripts to run at those points in time.
 */
public enum FileHook {

    /**
     * Before any tests of a given file are run.
     */
    BEFORE_TESTS("before_tests"),

    /**
     * After all tests of a given file are run.
     */
    AFTER_TESTS("after_tests");

    public final String type;

    FileHook(String type) {
        this.type = type;
    }

    public static FileHook fromString(String type) {
        for (FileHook h : FileHook.values())
            if (h.type.equals(type)) return h;

        return null;
    }

    @Override
    public String toString() {
        return type;
    }
}
