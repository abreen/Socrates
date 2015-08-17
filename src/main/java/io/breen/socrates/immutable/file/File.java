package io.breen.socrates.immutable.file;

import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.immutable.test.ceiling.AtMost;
import io.breen.socrates.immutable.test.ceiling.Ceiling;
import io.breen.socrates.immutable.test.implementation.any.LateSubmissionTest;
import io.breen.socrates.util.Either;
import io.breen.socrates.util.Left;
import io.breen.socrates.util.Right;

import java.time.LocalDateTime;
import java.util.*;

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

    public File(String path,
                double pointValue,
                Map<LocalDateTime, Double> dueDates,
                List<Either<Test, TestGroup>> tests)
    {
        this.path = path;
        this.pointValue = pointValue;
        this.testRoot = createTestRoot(pointValue, dueDates, tests);
    }

    public String toString() {
        return this.getClass().toString() + "(" +
                "path=" + path + ", " +
                "pointValue=" + pointValue + ", " +
                "testRoot=" + testRoot +
                ")";
    }

    /**
     * This method creates this file's test "root". The root is a test group that limits
     * the maximum total value of the descendant tests to the total value of the file.
     * This method may also create a test group of LateSubmissionTest objects, if the
     * criteria file specifies due dates for this file. (That test group would be a
     * child of the root.)
     */
    private static TestGroup createTestRoot(double fileValue,
                                            Map<LocalDateTime, Double> dueDates,
                                            List<Either<Test, TestGroup>> tests)
    {
        if (dueDates != null) {
            SortedMap<LocalDateTime, Double> sorted = new TreeMap<>(Collections.reverseOrder());
            sorted.putAll(dueDates);

            List<Either<Test, TestGroup>> lates = new ArrayList<>(sorted.size());

            for (Map.Entry<LocalDateTime, Double> entry : sorted.entrySet()) {
                LateSubmissionTest lst = new LateSubmissionTest(entry.getValue(), entry.getKey());
                lates.add(new Left<>(lst));
            }

            TestGroup lateGroup = new TestGroup(lates, new AtMost<>(1), Ceiling.ANY);
            tests.add(0, new Right<>(lateGroup));
        }

        return new TestGroup(tests, Ceiling.ANY, new AtMost<>(fileValue));
    }
}
