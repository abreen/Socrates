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

    protected final double deduction;

    public Test(double deduction) {
        this.deduction = deduction;
    }

    public String toString() {
        return "Test(deduction=" + deduction + ")";
    }
}
