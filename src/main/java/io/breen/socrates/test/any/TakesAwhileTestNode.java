package io.breen.socrates.test.any;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.File;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;

import javax.swing.text.Document;
import java.nio.file.Path;

/**
 * A test that takes awhile.
 */
public class TakesAwhileTestNode extends TestNode implements Automatable {

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public TakesAwhileTestNode() {}

    public TakesAwhileTestNode(double deduction) {
        super(deduction, "takes awhile");
    }

    @Override
    public boolean shouldPass(File parent, SubmittedFile target, Path submissionDir,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException
    {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException x) {
            return true;
        }

        return Math.random() > 0.5;
    }

    @Override
    public String toString() {
        return "TakesAwhileTestNode(deduction=" + getDeduction() + ")";
    }
}
