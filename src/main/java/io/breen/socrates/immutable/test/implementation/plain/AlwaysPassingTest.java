package io.breen.socrates.immutable.test.implementation.plain;

import io.breen.socrates.immutable.file.implementation.PlainFile;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.Automatable;
import io.breen.socrates.immutable.test.Test;

public class AlwaysPassingTest extends Test implements Automatable<PlainFile> {

    public AlwaysPassingTest(double deduction) {
        super(deduction);
    }

    @Override
    public boolean shouldPass(PlainFile parent, SubmittedFile target, Submission submission) {
        return true;
    }

    @Override
    public String toString() {
        return "PlainFile:AlwaysPassing" + super.toString();
    }
}
