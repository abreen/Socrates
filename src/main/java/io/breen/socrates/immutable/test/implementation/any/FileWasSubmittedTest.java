package io.breen.socrates.immutable.test.implementation.any;

import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.Automatable;
import io.breen.socrates.immutable.test.Test;

/**
 *
 */
public class FileWasSubmittedTest extends Test implements Automatable {

    public FileWasSubmittedTest(double deduction) {
        super(deduction);
    }

    @Override
    public boolean shouldPass(File parent, SubmittedFile target, Submission submission) {
        return target != null;
    }

    @Override
    public String toString() {
        return "FileWasSubmitted" + super.toString();
    }
}
