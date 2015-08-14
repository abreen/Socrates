package io.breen.socrates.immutable.submission;

import java.time.format.DateTimeParseException;
import java.util.logging.Logger;

/**
 * Class representing immutable objects storing information about a single file
 * found on the file system that represents one part of a student's submission.
 */
public class SubmittedFile {

    private static Logger logger = Logger.getLogger(SubmittedFile.class.getName());

    private final java.io.File file;
    private final String localPath;
    private final Receipt receipt;

    public SubmittedFile(java.io.File file, String localPath) {
        this(file, localPath, null);
    }

    public SubmittedFile(java.io.File file, String localPath, java.io.File receiptFile) {
        this.file = file;
        this.localPath = localPath;

        Receipt r = null;
        if (receiptFile != null) {
            try {
                r = Receipt.fromReceiptFile(receiptFile);
            } catch (DateTimeParseException e) {
                logger.info("for file " + file + ", caught DTPE creating receipt: " + e);
            } catch (java.io.FileNotFoundException e) {
                logger.info("for file " + file + ", caught FNFE creating receipt: " + e);
            }
        }

        this.receipt = r;
    }
}
