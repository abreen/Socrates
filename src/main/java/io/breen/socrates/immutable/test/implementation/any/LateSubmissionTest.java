package io.breen.socrates.immutable.test.implementation.any;

import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.Automatable;
import io.breen.socrates.immutable.test.CannotBeAutomatedException;
import io.breen.socrates.immutable.test.Test;

import java.time.LocalDateTime;

/**
 * A test that checks the submitted file's receipt to determine whether it was submitted
 * after a cutoff. This is an automatable test, but it may fail to be automatable if
 * the submitted file does not have a receipt.
 */
public class LateSubmissionTest extends Test implements Automatable {

    protected final LocalDateTime cutoff;

    public LateSubmissionTest(double deduction, LocalDateTime cutoff) {
        super(deduction);
        this.cutoff = cutoff;
    }

    @Override
    public boolean shouldPass(File parent, SubmittedFile target, Submission submission)
            throws CannotBeAutomatedException
    {
        if (target.receipt == null)
            throw new CannotBeAutomatedException();

        LocalDateTime ldt = target.receipt.getLatestDate();
        return ldt.isAfter(cutoff);
    }

    @Override
    public String toString() {
        return "LateSubmissionTest(deduction=" + deduction + ", cutoff=" + cutoff + ")";
    }
}
