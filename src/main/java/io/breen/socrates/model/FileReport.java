package io.breen.socrates.model;

import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.util.Either;
import io.breen.socrates.util.Left;
import io.breen.socrates.util.Right;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A mutable data structure containing outcomes of tests for a given submitted file.
 *
 * Insofar as this represents a tree data structure, since it is based on the immutable
 * "tree" formed by the Test and TestGroup objects "rooted" in the File object, this class
 * is also a TreeModel that can be displayed by the GUI.
 *
 * @see io.breen.socrates.model.GradeReportFormatter
 */
public class FileReport implements TreeModel {

    public final SubmittedFile submittedFile;
    public final File matchingFile;
    public final TestGroupNode testRoot;
    private final List<TreeModelListener> listeners;

    public FileReport(SubmittedFile submittedFile, File matchingFile) {
        if (submittedFile == null)
            throw new IllegalArgumentException("SubmittedFile must not be null");
        if (matchingFile == null)
            throw new IllegalArgumentException("must have non-null File");

        this.submittedFile = submittedFile;
        this.matchingFile = matchingFile;
        testRoot = buildTree(matchingFile.testRoot);
        listeners = new LinkedList<>();
    }

    private static TestGroupNode buildTree(TestGroup root) {
        int n = root.members.size();
        List<Either<TestNode, TestGroupNode>> newMembers = new ArrayList<>(n);

        for (Either<Test, TestGroup> member : root.members) {
            if (member instanceof Left) {
                Test test = member.getLeft();
                newMembers.add(new Left<>(new TestNode(test)));
            } else if (member instanceof Right) {
                TestGroup group = member.getRight();
                newMembers.add(new Right<>(buildTree(group)));
            }
        }

        return new TestGroupNode(root, newMembers);
    }

    /*
     * TreeModel methods
     */

    public Object getRoot() {
        return testRoot;
    }

    public Object getChild(Object parentObj, int index) {
        if (!(parentObj instanceof TestGroupNode))
            throw new IllegalArgumentException("invalid parent: not a TestGroupNode");

        TestGroupNode parent = (TestGroupNode)parentObj;
        return parent.members.get(index).get();
    }

    public int getChildCount(Object parentObj) {
        if (!(parentObj instanceof TestGroupNode))
            throw new IllegalArgumentException("invalid parent: not a TestGroupNode");

        TestGroupNode parent = (TestGroupNode)parentObj;
        return parent.members.size();
    }

    public boolean isLeaf(Object node) {
        return node instanceof TestNode;
    }

    /**
     * Called when a test node's result should be changed. This method modifies the
     * TestNode and notifies any listeners to the model.
     *
     * Since the JTree's listeners will be notified of this change, the UI will update
     * (if necessary) to display the new TestNode's status.
     *
     * @param path Path to the node changed
     * @param newValue One of TestResult values
     *
     * @see io.breen.socrates.model.TestResult
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
        if (!(newValue instanceof TestResult))
            throw new IllegalArgumentException("new value for TestNode must be a result");

        TestNode node = (TestNode)path.getLastPathComponent();
        TestResult result = (TestResult)newValue;

        node.setResult(result);

        TreePath parent = path.getParentPath();
        int index = getIndexOfChild(parent.getLastPathComponent(), node);

        fireTreeNodesChanged(
                this, parent, new int[] {index}, new Object[] {node}
        );
    }

    public int getIndexOfChild(Object parentObj, Object childObj) {
        if (!(parentObj instanceof TestGroupNode))
            throw new IllegalArgumentException("invalid parent: not a TestGroupNode");

        if (!(childObj instanceof TestGroupNode) && !(childObj instanceof TestNode))
            throw new IllegalArgumentException("invalid child: should be a node");

        TestGroupNode parent = (TestGroupNode)parentObj;

        int i = 0;
        for (Either<TestNode, TestGroupNode> member : parent.members) {
            Object item = member.get();
            if (item == childObj) return i;
            else i++;
        }

        return -1;
    }

    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    protected void fireTreeNodesChanged(FileReport source, TreePath path,
                                        int[] childIndices, Object[] children)
    {
        TreeModelEvent e = new TreeModelEvent(source, path, childIndices, children);

        for (TreeModelListener listener : listeners)
            listener.treeNodesChanged(e);
    }
}
