package io.breen.socrates.immutable.test;

import io.breen.socrates.immutable.test.ceiling.AtMost;
import io.breen.socrates.immutable.test.ceiling.Ceiling;
import io.breen.socrates.util.Either;

import java.util.List;
import java.util.StringJoiner;

/**
 * Represents a sequence of one or more tests that should be run together.
 */
public class TestGroup {

    protected final List<Either<Test, TestGroup>> members;

    protected final Ceiling<Integer> maxNum;

    protected final Ceiling<Double> maxValue;

    public TestGroup(List<Either<Test, TestGroup>> members, Ceiling<Integer> maxNum,
                     Ceiling<Double> maxValue)
    {
        if (maxNum == null) throw new IllegalArgumentException("maxNum cannot be null");

        if (maxValue == null)
            throw new IllegalArgumentException("maxValue cannot be null");

        if (maxNum != Ceiling.ANY) {
            int num = AtMost.getValue(maxNum);

            if (num < 0) throw new IllegalArgumentException("maxNum cannot be negative");
            if (num > members.size()) throw new IllegalArgumentException(
                    "maxNum cannot be greater than the number of tests"
            );
        }

        if (maxValue != Ceiling.ANY && AtMost.getValue(maxValue) < 0)
            throw new IllegalArgumentException("maxValue cannot be negative");

        this.members = members;
        this.maxNum = maxNum;
        this.maxValue = maxValue;
    }

    public TestGroup(List<Either<Test, TestGroup>> newMembers, TestGroup oldGroup) {
        this.members = newMembers;
        this.maxNum = oldGroup.maxNum;
        this.maxValue = oldGroup.maxValue;
    }

    public Ceiling<Integer> getMaxNum() {
        return maxNum;
    }

    public Ceiling<Double> getMaxValue() {
        return maxValue;
    }

    public List<Either<Test, TestGroup>> getMembers() {
        return members;
    }

    public String toString() {
        return "TestGroup" + new StringJoiner(", ", "(", ")")
                .add("maxNum=" + maxNum)
                .add("maxValue=" + maxValue)
                .add("members=" + members);
    }

//    public List<Deduction> runTests() {
//        return null;
//    }

//    public List<Deduction> getPossibleDeductions() {
//        return null;
//    }
}
