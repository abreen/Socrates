package io.breen.socrates.immutable.file;

import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.immutable.test.ceiling.AtMost;
import io.breen.socrates.immutable.test.ceiling.Ceiling;
import io.breen.socrates.immutable.test.implementation.any.LateSubmissionTest;
import io.breen.socrates.util.*;

import java.nio.file.Path;
import java.util.*;

/**
 * Class representing an expected file specified by the criteria. Instances of non-abstract
 * subclasses of this class are immutable, and are created when a Criteria object is created.
 *
 * @see io.breen.socrates.immutable.criteria.Criteria
 */
public abstract class File {

    /**
     * The relative path from the root of any student's submission directory specifying where the
     * expected file can be found.
     */
    public final Path localPath;

    /**
     * The number of points that this file contributes to the total value of the assignment being
     * graded.
     */
    public final double pointValue;

    /**
     * The content type value for this file. This may not be a MIME type; it should be whatever is
     * specified in the JSyntaxPane libraries in order for syntax highlighting to work.
     */
    public final String contentType;

    /**
     * This file's "test tree" root. The root is a TestGroup object whose maxValue field is equal to
     * this file's point value. (This is to prevent tests deducting more points than are allocated
     * to this file.)
     *
     * @see TestGroup
     */
    public final TestGroup testRoot;

    public File(Path localPath, double pointValue, String contentType, Map<Date, Double> dueDates,
                List<Either<Test, TestGroup>> tests)
    {
        this.localPath = localPath;
        this.contentType = contentType;
        this.pointValue = pointValue;
        this.testRoot = createTestRoot(pointValue, dueDates, tests);
    }

    /**
     * This method creates this file's test "root". The root is a test group that limits the maximum
     * total value of the descendant tests to the total value of the file. This method may also
     * create a test group of LateSubmissionTest objects, if the criteria file specifies due dates
     * for this file. (That test group would be a child of the root.)
     */
    private static TestGroup createTestRoot(double fileValue, Map<Date, Double> dueDates,
                                            List<Either<Test, TestGroup>> tests)
    {
        if (dueDates != null) {
            SortedMap<Date, Double> sorted = new TreeMap<>(Collections.reverseOrder());
            sorted.putAll(dueDates);

            List<Either<Test, TestGroup>> lates = new ArrayList<>(sorted.size());

            /*
             * It is *very* important that we process these due dates latest-to-earliest,
             * and that we add them to the list in that order. This will ensure that the
             * highest-valued deduction is chosen first. In the case that there are many
             * due dates specifying different late periods, we will want to take the
             * deduction corresponding to the "latest" cutoff timestamp first.
             */
            for (Map.Entry<Date, Double> entry : sorted.entrySet()) {
                LateSubmissionTest lst = new LateSubmissionTest(entry.getValue(), entry.getKey());
                lates.add(new Left<Test, TestGroup>(lst));
            }

            TestGroup lateGroup = new TestGroup(lates, new AtMost<>(1), Ceiling.ANY);

            /*
             * Here we add the late tests before any of the other tests specified from
             * the criteria file. This just makes sense, since we want any late
             * deductions to be taken first, and therefore appear first in the grade
             * report.
             */
            tests.add(0, new Right<Test, TestGroup>(lateGroup));
        }

        return new TestGroup(tests, Ceiling.ANY, new AtMost<>(fileValue));
    }

    public String toString() {
        return this.getClass().toString() + "(" +
                "localPath=" + localPath + ", " +
                "pointValue=" + pointValue + ", " +
                "testRoot=" + testRoot +
                ")";
    }

    /**
     * Returns the human-readable, user-friendly string representing the type of the file. This is
     * used by the GUI.
     */
    public abstract String getFileTypeName();
}
