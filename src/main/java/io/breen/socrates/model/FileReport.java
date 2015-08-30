package io.breen.socrates.model;

import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.util.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.Enumeration;

/**
 * A mutable data structure containing outcomes of tests for a given submitted file. The outcomes
 * are stored in a tree of TestWrapperNode objects, each of which individually represents either a
 * Test or a TestGroup from the criteria.
 *
 * This object adds the tree of TestWrapperNode objects to a DefaultTreeModel. When the GUI needs to
 * display a tree containing the current state of the tests for the file, it can simply set its
 * model reference to the one contained by the FileReport.
 */
public class FileReport {

    public final SubmittedFile submittedFile;
    public final File matchingFile;

    public final DefaultTreeModel treeModel;
    public final ConstraintUpdater updater;

    public FileReport(SubmittedFile submittedFile, File matchingFile) {
        if (submittedFile == null)
            throw new IllegalArgumentException("submitted file must not be null");
        if (matchingFile == null)
            throw new IllegalArgumentException("must have non-null matching file");

        this.submittedFile = submittedFile;
        this.matchingFile = matchingFile;

        treeModel = new DefaultTreeModel(buildTree(matchingFile.testRoot));
        updater = new ConstraintUpdater(treeModel);

        DefaultMutableTreeNode root = (DefaultMutableTreeNode)treeModel.getRoot();
        Enumeration dfs = root.depthFirstEnumeration();
        while (dfs.hasMoreElements()) {
            Object node = dfs.nextElement();
            if (node instanceof TestWrapperNode) ((TestWrapperNode)node).addObserver(updater);
        }
    }

    private static DefaultMutableTreeNode buildTree(TestGroup root) {
        TestGroupWrapperNode parent = new TestGroupWrapperNode(root);

        for (Either<Test, TestGroup> member : root.members) {
            if (member instanceof Left) {
                Test test = member.getLeft();
                TestWrapperNode child = new TestWrapperNode(test);
                parent.add(child);

            } else if (member instanceof Right) {
                TestGroup group = member.getRight();
                parent.add(buildTree(group));
            }
        }

        return parent;
    }
}
