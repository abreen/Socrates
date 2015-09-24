package io.breen.socrates.model.wrapper;

import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.*;
import io.breen.socrates.model.*;
import io.breen.socrates.model.event.*;
import io.breen.socrates.util.Observable;
import io.breen.socrates.util.*;
import io.breen.socrates.util.Observer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.*;

/**
 * This class "wraps" a SubmittedFile and contains outcomes of tests for the file. The outcomes are
 * stored in a tree of wrapper objects, each of which individually represents either a Test (in
 * the case of TestWrapperNode) or a TestGroup (in the case of TestGroupWrapperNode) from the
 * criteria.
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

    private final Map<TestWrapperNode, Boolean> finished;
    private final List<Observer<SubmittedFileWrapperNode>> observers;

    public SubmittedFileWrapperNode(SubmittedFile submittedFile, File matchingFile) {
        super(submittedFile);

        if (submittedFile == null)
            throw new IllegalArgumentException("submitted file must not be null");
        if (matchingFile == null)
            throw new IllegalArgumentException("must have non-null matching file");

        this.matchingFile = matchingFile;

        finished = new HashMap<>();
        observers = new LinkedList<>();

        treeModel = new DefaultTreeModel(null);
        updater = new ConstraintUpdater(treeModel);
        DefaultMutableTreeNode root = buildTree(matchingFile.testRoot);
        treeModel.setRoot(root);
    }

    private DefaultMutableTreeNode buildTree(TestGroup root) {
        TestGroupWrapperNode parent = new TestGroupWrapperNode(root);

        for (Object member : root.members) {
            if (member instanceof Test) {
                Test test = (Test)member;
                TestWrapperNode child = new TestWrapperNode(test);
                child.addObserver(updater);
                child.addObserver(this);
                finished.put(child, false);
                parent.add(child);

            } else if (member instanceof TestGroup) {
                TestGroup group = (TestGroup)member;
                parent.add(buildTree(group));
            }
        }

        return parent;
    }

    @Override
    public void objectChanged(ObservableChangedEvent<TestWrapperNode> event) {
        boolean notCompleteBefore = !isComplete();

        if (event instanceof ResultChangedEvent) {
            ResultChangedEvent e = (ResultChangedEvent)event;
            switch (e.newResult) {
            case PASSED:
            case FAILED:
                finished.put(e.source, true);
                break;
            case NONE:
                finished.put(e.source, false);
            }

        } else if (event instanceof ConstraintChangedEvent) {
            ConstraintChangedEvent e = (ConstraintChangedEvent)event;
            if (e.isNowConstrained) {
                finished.put(e.source, true);
            } else if (e.source.getResult() == TestResult.NONE) {
                finished.put(e.source, false);
            }
        }

        boolean notCompleteAfter = !isComplete();

        FileCompletedChangeEvent e;
        if (notCompleteBefore && !notCompleteAfter) {
            e = new FileCompletedChangeEvent(this, true);
            for (Observer<SubmittedFileWrapperNode> o : observers)
                o.objectChanged(e);

        } else if (!notCompleteBefore && notCompleteAfter) {
            e = new FileCompletedChangeEvent(this, false);
            for (Observer<SubmittedFileWrapperNode> o : observers)
                o.objectChanged(e);
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
        return finished.isEmpty() || !finished.containsValue(false);
    }

    public void resetAllTests() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)treeModel.getRoot();
        @SuppressWarnings("unchecked") Enumeration<DefaultMutableTreeNode> dfs = root
                .depthFirstEnumeration();
        while (dfs.hasMoreElements()) {
            DefaultMutableTreeNode n = dfs.nextElement();

            if (n instanceof TestWrapperNode) {
                TestWrapperNode node = (TestWrapperNode)n;
                Test test = (Test)node.getUserObject();

                node.setResult(TestResult.NONE);
                if (test instanceof Automatable) node.setAutomationStage(AutomationStage.NONE);
            }
        }
    }
}
