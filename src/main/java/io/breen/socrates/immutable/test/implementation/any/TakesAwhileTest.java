package io.breen.socrates.immutable.test.implementation.any;

import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.Automatable;
import io.breen.socrates.immutable.test.CannotBeAutomatedException;
import io.breen.socrates.immutable.test.Test;

/**
 * A test that takes awhile.
 */
public class TakesAwhileTest extends Test implements Automatable {

    public TakesAwhileTest(double deduction) {
        super(deduction, "takes awhile");
    }

    @Override
    public boolean shouldPass(File parent, SubmittedFile target, Submission submission)
            throws CannotBeAutomatedException
    {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException x) {}

        return Math.random() > 0.5;
    }

    @Override
    public String toString() {
        return "TakesAwhileTest(deduction=" + deduction + ")";
    }

    @Override
    public String getTestTypeName() {
        return "it takes awhile";
    }
}
