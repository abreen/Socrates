package io.breen.socrates.test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a sequence of one or more "member" tests that run together. In addition, a GroupNode represents two
 * "ceilings": a maximum number of tests that can possibly fail in the group, and a maximum number of points that can
 * be taken by tests in the group that fail. Either, both, or none of these ceilings may be specified in a
 * criteria file.
 *
 * In the test tree specified by a criteria file, test groups represent interior nodes: they should contain one or
 * more children that could be leaf nodes representing a single test, or more test groups, recursively.
 *
 * Test groups are the only way to represent "decisions" in a criteria file. For example, it is common to use a test
 * group with a maxNum of 1 and two member tests. This means that, as soon as the first test fails, the second test
 * will be skipped.
 *
 * @see TestNode
 */
public class GroupNode extends Node {

    private int maxNum;

    private double maxValue;

    /**
     * An optional label to display in the GUI above the members of this group. If the group
     * contains logically related members, this can enhance readability.
     */
    private String label;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public GroupNode() {}

    public GroupNode(List<Node> children, int maxNum, double maxValue) {
        this(children, maxNum, maxValue, null);
    }

    public GroupNode(List<Node> children, int maxNum, double maxValue, String label) {
        addAllChildren(children);
        this.maxNum = maxNum;
        this.maxValue = maxValue;
        this.label = label;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public void setMaxNum(int maxNum) {
        checkFrozen();
        this.maxNum = maxNum;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        checkFrozen();
        this.maxValue = maxValue;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        checkFrozen();
        this.label = label;
    }

    public String toString() {
        return "GroupNode(" +
                "maxNum=" + maxNum + ", " +
                "maxValue=" + maxValue + ", " +
                "label=" + label + ", " +
                "children=" + getChildren() +
                ")";
    }
}
