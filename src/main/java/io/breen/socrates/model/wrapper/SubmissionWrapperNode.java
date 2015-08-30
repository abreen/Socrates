package io.breen.socrates.model.wrapper;

import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.model.event.*;
import io.breen.socrates.util.Observable;
import io.breen.socrates.util.*;
import io.breen.socrates.util.Observer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.util.*;

/**
 * A class that "wraps" a Submission object. Serves as the parent of all SubmittedFileWrapperNode
 * objects in the submission tree. Also observes its children so that its "completed" state can be
 * computed if all of its children's tests are complete.
 */
public class SubmissionWrapperNode extends DefaultMutableTreeNode
        implements Observer<SubmittedFileWrapperNode>, Observable<SubmissionWrapperNode>
{

    protected final List<Observer<SubmissionWrapperNode>> observers;
    private final Set<SubmittedFileWrapperNode> unfinishedFiles;

    /**
     * Whether a grade report has been saved for this submission.
     */
    private boolean saved;

    public SubmissionWrapperNode(Submission submission) {
        super(submission);
        saved = false;
        unfinishedFiles = new HashSet<>();
        observers = new LinkedList<>();
    }

    public boolean isComplete() {
        return unfinishedFiles.isEmpty();
    }

    @Override
    public void add(MutableTreeNode newChild) {
        if (newChild instanceof SubmittedFileWrapperNode) {
            super.add(newChild);
            unfinishedFiles.add((SubmittedFileWrapperNode)newChild);
            ((SubmittedFileWrapperNode)newChild).addObserver(this);
        } else if (newChild instanceof UnrecognizedFileWrapperNode) {
            super.add(newChild);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void objectChanged(ObservableChangedEvent<SubmittedFileWrapperNode> event) {
        int numBefore = unfinishedFiles.size();

        if (event instanceof FileCompletedChangeEvent) {
            FileCompletedChangeEvent e = (FileCompletedChangeEvent)event;
            if (e.isNowComplete) unfinishedFiles.remove(e.source);
            else unfinishedFiles.add(e.source);
        }

        int numAfter = unfinishedFiles.size();

        SubmissionCompletedChangeEvent e;
        if (numBefore == 0 && numAfter > 0) {
            e = new SubmissionCompletedChangeEvent(this, false);
            observers.forEach(o -> o.objectChanged(e));
        } else if (numBefore > 0 && numAfter == 0) {
            e = new SubmissionCompletedChangeEvent(this, true);
            observers.forEach(o -> o.objectChanged(e));
        }
    }

    @Override
    public void addObserver(Observer<SubmissionWrapperNode> observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<SubmissionWrapperNode> observer) {
        observers.remove(observer);
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        if (saved == this.saved) return;
        this.saved = saved;
        GradeReportSavedEvent e = new GradeReportSavedEvent(this);
        observers.forEach(o -> o.objectChanged(e));
    }
}
