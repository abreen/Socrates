package io.breen.socrates.model.wrapper;

import io.breen.socrates.immutable.submission.SubmittedFile;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A class that "wraps" a SubmittedFile that cannot be recognized as a file specified by the
 * criteria.
 */
public class UnrecognizedFileWrapperNode extends DefaultMutableTreeNode {

    public UnrecognizedFileWrapperNode(SubmittedFile submittedFile) {
        super(submittedFile);
    }
}
