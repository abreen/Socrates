package io.breen.socrates.test;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.File;
import io.breen.socrates.submission.SubmittedFile;

import javax.swing.text.Document;
import java.nio.file.Path;

/**
 * An interface implemented by any test whose running can be automated. Any test that implements
 * this interface can be scheduled for execution without any user interaction.
 */
public interface Automatable<T extends File> {

    /**
     * For automatable tests, this method is called to determine whether the test should pass or
     * fail.
     *
     * @param parent The File instance created from the criteria file, representing the assignment's
     *               requirements
     * @param target A SubmittedFile object representing the actual submission on the file system,
     *               or null if the file could not be found
     * @param submissionDir The Path object referring to the submission directory under which the target
     *                      is located
     * @param criteria The Criteria object that specifies the parent file (may be used to obtain
     *                 resources loaded from a criteria package)
     * @param transcript A Document object to which the "transcript" of a test should be written
     *                   (detailed information showing what test is being run)
     *
     * @return Whether this test should pass (i.e. whether the target file is "correct")
     *
     * @throws CannotBeAutomatedException If, while running the test automatically, it becomes
     * impossible to determine whether the test should pass automatically
     * @throws AutomationFailureException If, while running the test automatically, a fatal error
     * occurs
     */
    boolean shouldPass(T parent, SubmittedFile target, Path submissionDir, Criteria criteria,
                       Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException;
}
