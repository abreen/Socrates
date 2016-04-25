package io.breen.socrates.test.plain;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.plain.PlainFile;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.Automatable;
import io.breen.socrates.test.TestNode;

import javax.swing.text.Document;
import java.nio.file.Path;

public class AlwaysPassingTestNode extends TestNode implements Automatable<PlainFile> {

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public AlwaysPassingTestNode() {}

    public AlwaysPassingTestNode(double deduction) {
        super(deduction, "(always passing)");
    }

    @Override
    public boolean shouldPass(PlainFile parent, SubmittedFile target, Path submissionDir,
                              Criteria criteria, Document transcript, Document notes)
    {
        return true;
    }

    @Override
    public String toString() {
        return "PlainFile:AlwaysPassing" + super.toString();
    }
}
