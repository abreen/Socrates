package io.breen.socrates.immutable.test;

/**
 * Class representing a single test specified by the criteria. Instances of
 * non-abstract subclasses of this class are immutable, and are created when a
 * Criteria object is created.
 *
 * All tests are associated with a "target" file. This target file is created after
 * instances of its tests are created.
 *
 * Minimally, a test must define a deduction: the points deducted from a student's
 * score if the test fails during grading.
 *
 * By default, tests are marked as passing or failing by a human grader. However, some
 * tests support automation. Those tests subclass Test but also implement the Automatable
 * interface.
 *
 * @see io.breen.socrates.immutable.file.File
 * @see io.breen.socrates.immutable.criteria.Criteria
 * @see io.breen.socrates.immutable.test.Automatable
 */
public abstract class Test {

    public final double deduction;
    public final String description;

    public Test(double deduction, String description) {
        this.deduction = deduction;
        this.description = description;
    }

    public String toString() {
        return "Test(deduction=" + deduction + ")";
    }

    /**
     * Returns the human-readable, user-friendly string representing the type of the
     * test. This is used by the GUI.
     */
    public abstract String getTestTypeName();
}
