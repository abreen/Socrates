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
    BEFORE_TEST("before_test"),

    /**
     * After all tests of a given file are run.
     */
    AFTER_TEST("after_test");

    public final String type;

    TestHook(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    public static TestHook fromString(String type) {
        for (TestHook h : TestHook.values())
            if (h.type.equals(type))
                return h;

        return null;
    }
}
