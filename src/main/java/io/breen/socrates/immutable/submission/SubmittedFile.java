package io.breen.socrates.immutable.submission;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Class representing immutable objects storing information about a single file
 * found on the file system that represents one part of a student's submission.
 */
public class SubmittedFile {

    private static Logger logger = Logger.getLogger(SubmittedFile.class.getName());

    /**
     * This file's location relative to the submission directory. This path should
     * match the path specified in a criteria file, if this SubmittedFile is indeed
     * relevant to grading.
     */
    private final Path localPath;

    /**
     * This file's receipt, storing the submission timestamps. If there was no receipt
     * for this file, this is null.
     */
    private final Receipt receipt;

    public SubmittedFile(Path localPath) {
        this.localPath = localPath;
        this.receipt = null;
    }

    public SubmittedFile(Path localPath, Path receipt)
            throws IOException, ReceiptFormatException
    {
        this.localPath = localPath;

        Receipt r = null;
        if (receipt != null) {
            r = Receipt.fromReceiptFile(receipt);
        }

        this.receipt = r;
    }
}
