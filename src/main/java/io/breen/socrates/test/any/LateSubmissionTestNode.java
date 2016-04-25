package io.breen.socrates.test.any;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.File;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;

import javax.swing.text.Document;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A test that checks the submitted file's receipt to determine whether it was submitted after a
 * cutoff. This is an automatable test, but it may fail to be automatable if the submitted file does
 * not have a receipt.
 */
public class LateSubmissionTestNode extends TestNode implements Automatable {

    private final static SimpleDateFormat formatter = new SimpleDateFormat(
            "M/d h:mm:ss a"
    );

    protected final Date cutoff;


    public LateSubmissionTestNode(double deduction, Date cutoff) {
        super(deduction, "submitted after " + formatter.format(cutoff));
        this.cutoff = cutoff;
    }

    @Override
    public boolean shouldPass(File parent, SubmittedFile target, Path submissionDir,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException
    {
        if (target.getReceipt() == null) throw new CannotBeAutomatedException("missing receipt file");

        Date d = target.getReceipt().getLatestDate();
        return d.before(cutoff);
    }
}
