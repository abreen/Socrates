package io.breen.socrates.test.any;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.File;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;

import javax.swing.text.Document;

/**
 * A test that takes awhile.
 */
public class TakesAwhileTest extends Test implements Automatable {

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public TakesAwhileTest() {}

    public TakesAwhileTest(double deduction) {
        super(deduction, "takes awhile");
    }

    @Override
    public boolean shouldPass(File parent, SubmittedFile target, Submission submission,
                              Criteria criteria, Document transcript, Document notes)
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
