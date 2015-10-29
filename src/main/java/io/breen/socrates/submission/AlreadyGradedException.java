package io.breen.socrates.submission;

public class AlreadyGradedException extends Exception {

    public AlreadyGradedException() {
        super("already has a grade file");
    }
}
