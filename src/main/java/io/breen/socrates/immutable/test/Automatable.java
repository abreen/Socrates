package io.breen.socrates.immutable.test;

import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;

/**
 * An interface implemented by any test whose running can be automated. Any test that
 * implements this interface can be scheduled for execution without any user interaction.
 * This means that Automatable tests can be skipped at the UI level when it is presenting
 * tests for a user to pass or fail.
 */
public interface Automatable<T extends File> {

    /**
     * For automatable tests, this method is called to determine whether the test should
     * pass or fail.
     *
     * @param parent The File instance created from the criteria file, representing the
     * assignment's requirements
     * @param target A SubmittedFile object representing the actual submission on the file
     * system, or null if the file could not be found
     * @param submission The Submission object containing the target
     *
     * @return Whether this test should pass (i.e. whether the target file is "correct")
     */
    boolean shouldPass(T parent, SubmittedFile target, Submission submission);
}
