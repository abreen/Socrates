package io.breen.socrates.test.plain;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.plain.PlainFile;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.Automatable;
import io.breen.socrates.test.Test;

import javax.swing.text.Document;

public class AlwaysPassingTest extends Test implements Automatable<PlainFile> {

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public AlwaysPassingTest() {}

    public AlwaysPassingTest(double deduction) {
        super(deduction, "(always passing)");
    }

    @Override
    public boolean shouldPass(PlainFile parent, SubmittedFile target, Submission submission,
                              Criteria criteria, Document transcript, Document notes)
    {
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
