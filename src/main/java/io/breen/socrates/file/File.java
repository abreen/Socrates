package io.breen.socrates.file;

import io.breen.socrates.test.GroupNode;
import io.breen.socrates.test.Node;
import io.breen.socrates.test.any.LateSubmissionTestNode;
import io.breen.socrates.util.Freezable;

import java.util.*;

/**
 * Class representing an expected file specified by the criteria.
 *
 * @see io.breen.socrates.criteria.Criteria
 */
public abstract class File extends Freezable {

    /**
     * The relative path from the root of any student's submission directory specifying where the
     * expected file can be found.
     */
    private String path;

    /**
     * The number of points that this file contributes to the total value of the assignment being
     * graded.
     */
    private double pointValue;

    private Map<Date, Double> dueDates;

    private List<Node> tests = Collections.emptyList();

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public File() {
    }

    public File(String path, double pointValue, Map<Date, Double> dueDates, List<Node> tests) {
        this.path = path;
        this.pointValue = pointValue;
        this.dueDates = dueDates;
        this.tests = tests;
    }

    public List<Node> getTests() {
        return Collections.unmodifiableList(tests);
    }

    /**
     * Used by SnakeYAML.
     */
    public void setTests(List<Node> tests) {
        checkFrozen();
        this.tests = tests;
    }

    public String getPath() {
        return path;
    }

    /**
     * Used by SnakeYAML.
     */
    public void setPath(String path) {
        checkFrozen();
        this.path = path;
    }

    public double getPointValue() {
        return pointValue;
    }

    /**
     * Used by SnakeYAML.
     */
    public void setPointValue(double pointValue) {
        checkFrozen();
        this.pointValue = pointValue;
    }

    public Map<Date, Double> getDueDates() {
        return Collections.unmodifiableMap(dueDates);
    }

    /**
     * Used by SnakeYAML.
     */
    public void setDueDates(Map<Date, Double> dueDates) {
        checkFrozen();
        this.dueDates = dueDates;
    }

    /**
     * Freezes this File object and its tests. This should be called after SnakeYAML constructs all
     * of the files from a criteria file, to prevent further modifications.
     */
    @Override
    public void freeze() {
        tests.forEach(Node::freezeAll);
        super.freeze();
    }

    /**
     * This method creates this file's test "root". The root is a test group that limits the maximum
     * total value of the descendant tests to the total value of the file. This method may also
     * create a test group of LateSubmissionTestNode objects, if the criteria file specifies due
     * dates for this file. (That test group would be a child of the root.)
     *
     * TODO this code should be moved to the view
     */
    protected GroupNode createTestRoot() {
        if (dueDates != null) {
            SortedMap<Date, Double> sorted = new TreeMap<>(Collections.reverseOrder());
            sorted.putAll(dueDates);

            List<Node> lateTests = new ArrayList<>(sorted.size());

            /*
             * It is *very* important that we process these due dates latest-to-earliest,
             * and that we add them to the list in that order. This will ensure that the
             * highest-valued deduction is chosen first. In the case that there are many
             * due dates specifying different late periods, we will want to take the
             * deduction corresponding to the "latest" cutoff timestamp first.
             */
            for (Map.Entry<Date, Double> entry : sorted.entrySet()) {
                LateSubmissionTestNode lst = new LateSubmissionTestNode(
                        entry.getValue(),
                        entry.getKey()
                );
                lateTests.add(lst);
            }

            GroupNode lateGroup = new GroupNode(lateTests, 1, 0.0);

            /*
             * Here we add the late tests before any of the other tests specified from
             * the criteria file. This just makes sense, since we want any late
             * deductions to be taken first, and therefore appear first in the grade
             * report.
             */
            tests.add(0, lateGroup);
        }

        return new GroupNode(tests, 0, pointValue);
    }
}
