package io.breen.socrates.model;

import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.util.Either;

import java.util.List;

public class TestGroupNode {

    public final TestGroup testGroup;
    public final List<Either<TestNode, TestGroupNode>> members;

    public TestGroupNode(TestGroup testGroup,
                         List<Either<TestNode, TestGroupNode>> members)
    {
        this.testGroup = testGroup;
        this.members = members;
    }
}
