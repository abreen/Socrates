package io.breen.socrates.immutable.file;

import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.immutable.test.ceiling.AtMost;
import io.breen.socrates.immutable.test.ceiling.Ceiling;
import io.breen.socrates.util.Either;

import java.util.List;

/**
 * Class representing an expected file specified by the criteria. Instances of
 * non-abstract subclasses of this class are immutable, and are created when a
 * Criteria object is created.
 *
 * @see io.breen.socrates.immutable.criteria.Criteria
 */
public abstract class File {

    /**
     * The relative path from the root of a user's submission directory specifying
     * where the expected file can be found.
     */
    protected final String path;

    /**
     * The number of points that this file contributes to the total value of the
     * assignment being graded.
     */
    protected final double pointValue;

    /**
     * This file's "test tree" root. The root is a TestGroup object whose maxValue
     * field is equal to this file's point value. (This is to prevent tests deducting
     * more points than are allocated to this file.)
     *
     * @see TestGroup
     */
    protected final TestGroup testRoot;

    public File(String path, double pointValue, List<Either<Test, TestGroup>> tests) {
        this.path = path;
        this.pointValue = pointValue;
        this.testRoot = new TestGroup(tests, Ceiling.ANY, new AtMost<>(pointValue));
    }

    public String toString() {
        return this.getClass().toString() + "(" +
                "path=" + path + ", " +
                "pointValue=" + pointValue + ", " +
                "testRoot=" + testRoot +
                ")";
    }
}
