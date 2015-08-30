package io.breen.socrates.model.wrapper;

import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.model.ConstraintUpdater;
import io.breen.socrates.model.event.*;
import io.breen.socrates.util.*;
import io.breen.socrates.util.Observable;
import io.breen.socrates.util.Observer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.*;

/**
 * This class "wraps" a SubmittedFile and contains outcomes of tests for the file. The outcomes are
 * stored in a tree of TestWrapperNode objects, each of which individually represents either a Test
 * or a TestGroup from the criteria.
 *
 * Instances add the tree of TestWrapperNode objects to a DefaultTreeModel. When the GUI needs to
 * display a tree containing the current state of the tests for the file, it can simply set its
 * model reference to the one contained by an object of this class.
 */
public class SubmittedFileWrapperNode extends DefaultMutableTreeNode
        implements Observer<TestWrapperNode>, Observable<SubmittedFileWrapperNode>
{

    public final File matchingFile;

    public final DefaultTreeModel treeModel;
    public final ConstraintUpdater updater;

    private final Set<TestWrapperNode> unfinishedTests;
    private final List<Observer<SubmittedFileWrapperNode>> observers;

    public SubmittedFileWrapperNode(SubmittedFile submittedFile, File matchingFile) {
        super(submittedFile);

        if (submittedFile == null)
            throw new IllegalArgumentException("submitted file must not be null");
        if (matchingFile == null)
            throw new IllegalArgumentException("must have non-null matching file");

        this.matchingFile = matchingFile;

        unfinishedTests = new HashSet<>();
        observers = new LinkedList<>();

        treeModel = new DefaultTreeModel(null);
        updater = new ConstraintUpdater(treeModel);
        DefaultMutableTreeNode root = buildTree(matchingFile.testRoot);
        treeModel.setRoot(root);
    }

    private DefaultMutableTreeNode buildTree(TestGroup root) {
        TestGroupWrapperNode parent = new TestGroupWrapperNode(root);

        for (Either<Test, TestGroup> member : root.members) {
            if (member instanceof Left) {
                Test test = member.getLeft();
                TestWrapperNode child = new TestWrapperNode(test);
                child.addObserver(updater);
                child.addObserver(this);
                unfinishedTests.add(child);
                parent.add(child);

            } else if (member instanceof Right) {
                TestGroup group = member.getRight();
                parent.add(buildTree(group));
            }
        }

        return parent;
    }

    @Override
    public void objectChanged(ObservableChangedEvent<TestWrapperNode> event) {
        int numBefore = unfinishedTests.size();

        if (event instanceof ResultChangedEvent) {
            ResultChangedEvent e = (ResultChangedEvent)event;
            switch (e.newResult) {
            case PASSED:
            case FAILED:
                unfinishedTests.remove(e.source);
                break;
            case NONE:
                unfinishedTests.add(e.source);
            }
        } else if (event instanceof ConstraintChangedEvent) {
            ConstraintChangedEvent e = (ConstraintChangedEvent)event;
            if (e.isNowConstrained) unfinishedTests.remove(e.source);
            else unfinishedTests.add(e.source);
        }

        int numAfter = unfinishedTests.size();

        FileCompletedChangeEvent e;
        if (numAfter == 0 && numBefore != 0) {
            e = new FileCompletedChangeEvent(this, true);
            observers.forEach(o -> o.objectChanged(e));
        } else if (numAfter > 0 && numBefore == 0) {
            e = new FileCompletedChangeEvent(this, false);
            observers.forEach(o -> o.objectChanged(e));
        }
    }

    @Override
    public void addObserver(Observer<SubmittedFileWrapperNode> observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<SubmittedFileWrapperNode> observer) {
        observers.add(observer);
    }

    public boolean isComplete() {
        return unfinishedTests.isEmpty();
    }
}
