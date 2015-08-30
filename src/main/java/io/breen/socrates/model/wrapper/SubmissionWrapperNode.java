package io.breen.socrates.model.wrapper;

import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.util.ObservableChangedEvent;
import io.breen.socrates.util.Observer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * A class that "wraps" a Submission object. Serves as the parent of all SubmittedFileWrapperNode
 * objects in the submission tree. Also observes its children so that its "completed" state can be
 * computed if all of its children's tests are complete.
 */
public class SubmissionWrapperNode extends DefaultMutableTreeNode
        implements Observer<SubmittedFileWrapperNode>
{

    public SubmissionWrapperNode(Submission submission) {
        super(submission);
    }

    public boolean isComplete() {
        for (Object child : children) {
            if (child instanceof SubmittedFileWrapperNode) {
                if (!((SubmittedFileWrapperNode)child).isComplete()) return false;
            }
        }
        return true;
    }

    @Override
    public void add(MutableTreeNode newChild) {
        if (newChild instanceof SubmittedFileWrapperNode) {
            super.add(newChild);
            ((SubmittedFileWrapperNode)newChild).addObserver(this);
        } else if (newChild instanceof UnrecognizedFileWrapperNode) {
            super.add(newChild);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void objectChanged(ObservableChangedEvent<SubmittedFileWrapperNode> event) {

    }
}
