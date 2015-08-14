package io.breen.socrates.immutable.submission;

/**
 * Class representing immutable objects storing information about a single file
 * found on the file system that represents one part of a student's submission.
 */
public class SubmittedFile {

    private final java.io.File file;
    private final String localPath;

    public SubmittedFile(java.io.File file, String localPath) {
        this.file = file;
        this.localPath = localPath;
    }
}
