package io.breen.socrates.immutable.test.implementation.any;

import io.breen.socrates.immutable.criteria.Resource;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.Automatable;
import io.breen.socrates.immutable.test.CannotBeAutomatedException;
import io.breen.socrates.immutable.test.Test;

public class ScriptTest extends Test implements Automatable {

    protected final String description;

    protected final Resource script;

    public ScriptTest(double deduction, String description, Resource script) {
        super(deduction);
        this.description = description;
        this.script = script;
    }

    @Override
    public boolean shouldPass(File parent, SubmittedFile target, Submission submission)
            throws CannotBeAutomatedException
    {
        // TODO run the Python script
        return true;
    }
}
