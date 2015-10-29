package io.breen.socrates.test.any;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.File;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;

import javax.swing.text.Document;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A test that checks the submitted file's receipt to determine whether it was submitted after a
 * cutoff. This is an automatable test, but it may fail to be automatable if the submitted file does
 * not have a receipt.
 */
public class LateSubmissionTest extends Test implements Automatable {

    private final static SimpleDateFormat formatter = new SimpleDateFormat(
            "M/d h:mm:ss a"
    );

    protected final Date cutoff;

    public LateSubmissionTest(double deduction, Date cutoff) {
        super(deduction, "submitted after " + formatter.format(cutoff));
        this.cutoff = cutoff;
    }

    @Override
    public boolean shouldPass(File parent, SubmittedFile target, Submission submission,
                              Criteria criteria, Document transcript)
            throws CannotBeAutomatedException
    {
        if (target.receipt == null) throw new CannotBeAutomatedException("missing receipt file");

        Date d = target.receipt.getLatestDate();
        return d.before(cutoff);
    }

    @Override
    public String toString() {
        return "LateSubmissionTest(deduction=" + deduction + ", cutoff=" + cutoff + ")";
    }

    @Override
    public String getTestTypeName() {
        return "late submission check";
    }
}
