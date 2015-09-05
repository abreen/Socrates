package io.breen.socrates.immutable.test.implementation.plain;

import io.breen.socrates.immutable.file.plain.PlainFile;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.Automatable;
import io.breen.socrates.immutable.test.Test;

public class AlwaysPassingTest extends Test implements Automatable<PlainFile> {

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public AlwaysPassingTest() {}

    public AlwaysPassingTest(double deduction) {
        super(deduction, "(always passing)");
    }

    @Override
    public boolean shouldPass(PlainFile parent, SubmittedFile target, Submission submission) {
        return true;
    }

    @Override
    public String toString() {
        return "PlainFile:AlwaysPassing" + super.toString();
    }

    @Override
    public String getTestTypeName() {
        return "always passing";
    }
}
