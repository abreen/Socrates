package io.breen.socrates.immutable.test;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a sequence of one or more "member" tests that run together. In addition, a TestGroup
 * represents two "ceilings": a maximum number of tests that can possibly fail in the group, and a
 * maximum number of points that can be taken by tests in the group that fail. Either, both, or none
 * of these ceilings may be specified. If a ceiling is not specified, it defaults to Ceiling.ANY
 * (which represents no limit).
 *
 * Test groups are the only way to represent "decisions" in a criteria file. For example, it is
 * common to use a test group with a maxNum of 1 and two member tests. This means that, as soon as
 * the first test fails, the second test will be skipped. Furthermore, test groups can contain other
 * tests groups as members (not just tests), adding deep recursive functionality in a relatively
 * lightweight way.
 *
 * @see io.breen.socrates.immutable.test.Test
 * @see io.breen.socrates.immutable.test.ceiling.Ceiling
 */
public class TestGroup {

    public List<Object> members = new LinkedList<>();

    public int maxNum;

    public double maxValue;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public TestGroup() {}

    public TestGroup(List<Object> members, int maxNum, double maxValue) {
        this.members = members;
        this.maxNum = maxNum;
        this.maxValue = maxValue;
    }

    //    public TestGroup(List<Either<Test, TestGroup>> newMembers, TestGroup oldGroup) {
    //        this.members = newMembers;
    //        this.maxNum = oldGroup.maxNum;
    //        this.maxValue = oldGroup.maxValue;
    //    }

    public String toString() {
        return "TestGroup(" +
                "maxNum=" + maxNum + ", " +
                "maxValue=" + maxValue + ", " +
                "members=" + members + ")";
    }
}
