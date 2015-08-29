package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.List;

public class SubmissionTree {

    private JPanel rootPanel;
    private JScrollPane scrollPane;

    private JTree tree;
    private DefaultMutableTreeNode root;
    private DefaultMutableTreeNode ungradedRoot;
    private DefaultMutableTreeNode gradedRoot;
    private DefaultMutableTreeNode skippedRoot;

    private void createUIComponents() {
        root = new DefaultMutableTreeNode(null);

        ungradedRoot = new DefaultMutableTreeNode("Ungraded");
        gradedRoot = new DefaultMutableTreeNode("Graded");
        skippedRoot = new DefaultMutableTreeNode("Skipped");

        root.add(ungradedRoot);
        root.add(gradedRoot);
        root.add(skippedRoot);

        tree = new JTree(root) {
            @Override
            public String convertValueToText(Object value, boolean selected, boolean expanded,
                                             boolean leaf, int row, boolean hasFocus)
            {
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
                    if (userObject instanceof Submission) {
                        return ((Submission)userObject).studentName;
                    } else if (userObject instanceof SubmittedFile) {
                        return ((SubmittedFile)userObject).localPath.toString();
                    }
                }

                return super.convertValueToText(
                        value, selected, expanded, leaf, row, hasFocus
                );
            }
        };

        /*
         * This ensures that only submitted files in the tree can be selected --- not
         * submission directories or anything else.
         */
        tree.setSelectionModel(
                new PredicateTreeSelectionModel(
                        path -> {
                            Object last = path.getLastPathComponent();
                            if (last instanceof DefaultMutableTreeNode) {
                                DefaultMutableTreeNode n = (DefaultMutableTreeNode)last;
                                return n.getUserObject() instanceof SubmittedFile;
                            } else {
                                return false;
                            }
                        }
                )
        );

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

        /*
         * Set up the scroll pane.
         */
        scrollPane = new JScrollPane(tree);
        if (Globals.operatingSystem == Globals.OS.OSX) {
            Border border = new LineBorder(new Color(197, 197, 197));
            scrollPane.setBorder(border);
        }
    }

    public void addTreeSelectionListener(TreeSelectionListener listener) {
        tree.addTreeSelectionListener(listener);
    }

    public SubmittedFile getSelectedSubmittedFile() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        if (node == null) return null;

        Object userObject = node.getUserObject();

        if (userObject instanceof SubmittedFile) return (SubmittedFile)userObject;
        else return null;
    }

    public Submission getSelectedSubmission() {
        if (!hasSelection()) return null;

        TreePath file = tree.getSelectionPath();
        TreePath submission = file.getParentPath();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)submission.getLastPathComponent();

        return (Submission)node.getUserObject();
    }

    public void addUngraded(List<Submission> submissions) {
        for (Submission s : submissions) {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(s);

            for (SubmittedFile f : s.files)
                root.add(new DefaultMutableTreeNode(f));

            ungradedRoot.add(root);
        }
    }

    public void expandFirstSubmission() {
        DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode)ungradedRoot.getFirstChild();
        tree.expandPath(new TreePath(firstChild.getPath()));
    }

    public boolean hasSelection() {
        return tree.getSelectionPath() != null;
    }

    /**
     * Returns true if the currently selected file belongs to the first submission in the current
     * category subtree ("ungraded", "skipped", or "graded"), or false otherwise. This method
     * returns false if there is no selection.
     */
    public boolean firstSubmissionSelected() {
        if (!hasSelection()) return false;

        // get path to a submitted file
        TreePath selection = tree.getSelectionPath();
        // get path to submission
        TreePath parent = selection.getParentPath();
        // get path to category subtree
        TreePath category = parent.getParentPath();

        DefaultMutableTreeNode categoryNode = (DefaultMutableTreeNode)category
                .getLastPathComponent();

        TreeNode firstChild = categoryNode.getFirstChild();

        return parent.getLastPathComponent() == firstChild;
    }

    /**
     * Returns true if the currently selected file belongs to the last submission in the current
     * category subtree ("ungraded", "skipped", or "graded"), or false otherwise. This method
     * returns false if there is no selection.
     */
    public boolean lastSubmissionSelected() {
        if (!hasSelection()) return false;

        // get path to a submitted file
        TreePath selection = tree.getSelectionPath();
        // get path to submission
        TreePath parent = selection.getParentPath();
        // get path to category subtree
        TreePath category = parent.getParentPath();

        DefaultMutableTreeNode categoryNode = (DefaultMutableTreeNode)category
                .getLastPathComponent();

        TreeNode lastChild = categoryNode.getLastChild();

        return parent.getLastPathComponent() == lastChild;
    }

    public void selectFirstSubmission() {
        Enumeration ungraded = root.breadthFirstEnumeration();
        while (ungraded.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)ungraded.nextElement();

            if (node.getUserObject() instanceof Submission) {
                if (node.getChildCount() == 0) continue;

                // get the first file in this submission
                DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getFirstChild();

                tree.setSelectionPath(new TreePath(child.getPath()));
                return;
            }
        }
    }

    /**
     * Sets the submission tree's current selection to the first file in the next submission in the
     * current category subtree ("ungraded", "skipped", or "graded"). If the last submission is
     * currently selected, this method does nothing. If there is no current selection, this method
     * selects the first submission.
     */
    public void goToNextSubmission() {
        if (!hasSelection()) {
            selectFirstSubmission();
            return;
        }

        if (lastSubmissionSelected()) return;

        // get path to submission (parent of current file)
        TreePath parent = tree.getSelectionPath().getParentPath();
        Object parentNode = parent.getLastPathComponent();

        // get path to category subtree
        TreePath category = parent.getParentPath();
        DefaultMutableTreeNode categoryNode = (DefaultMutableTreeNode)category
                .getLastPathComponent();

        TreeNode nextSubmission = categoryNode.getChildAfter((TreeNode)parentNode);

        // find the first submission that contains at least one file
        while (nextSubmission != null && nextSubmission.getChildCount() == 0)
            nextSubmission = categoryNode.getChildAfter(nextSubmission);

        if (nextSubmission == null)     // no submissions after have any files
            return;

        // get the first file
        DefaultMutableTreeNode file = (DefaultMutableTreeNode)nextSubmission.getChildAt(0);

        tree.setSelectionPath(new TreePath(file.getPath()));
    }

    /**
     * Sets the submission tree's current selection to the first file in the previous submission in
     * the current category subtree ("ungraded", "skipped", or "graded"). If the first submission is
     * currently selected, this method does nothing. If there is no current selection, this method
     * does nothing.
     */
    public void goToPreviousSubmission() {
        if (!hasSelection() || firstSubmissionSelected()) return;

        // get path to submission (parent of current file)
        TreePath parent = tree.getSelectionPath().getParentPath();
        Object parentNode = parent.getLastPathComponent();

        // get path to category subtree
        TreePath category = parent.getParentPath();
        DefaultMutableTreeNode categoryNode = (DefaultMutableTreeNode)category
                .getLastPathComponent();

        TreeNode prevSubmission = categoryNode.getChildBefore((TreeNode)parentNode);

        // find the first submission that contains at least one file
        while (prevSubmission != null && prevSubmission.getChildCount() == 0)
            prevSubmission = categoryNode.getChildBefore(prevSubmission);

        if (prevSubmission == null)     // no submissions before have any files
            return;

        // get the first file
        DefaultMutableTreeNode file = (DefaultMutableTreeNode)prevSubmission.getChildAt(0);

        tree.setSelectionPath(new TreePath(file.getPath()));
    }

    /**
     * Returns true if the currently selected file is the first file in the currently selected
     * submission. This method returns false if there is no selection.
     */
    public boolean firstFileInSubmissionSelected() {
        if (!hasSelection()) return false;

        // get path to the submitted file
        TreePath file = tree.getSelectionPath();
        // get path to submission
        TreePath submission = file.getParentPath();

        DefaultMutableTreeNode submissionNode = (DefaultMutableTreeNode)submission
                .getLastPathComponent();

        TreeNode firstChild = submissionNode.getFirstChild();

        return file.getLastPathComponent() == firstChild;
    }

    /**
     * Returns true if the currently selected file is the last file in the currently selected
     * submission. This method returns false if there is no selection.
     */
    public boolean lastFileInSubmissionSelected() {
        if (!hasSelection()) return false;

        // get path to the submitted file
        TreePath file = tree.getSelectionPath();
        // get path to submission
        TreePath submission = file.getParentPath();

        DefaultMutableTreeNode submissionNode = (DefaultMutableTreeNode)submission
                .getLastPathComponent();

        TreeNode lastChild = submissionNode.getLastChild();

        return file.getLastPathComponent() == lastChild;
    }

    /**
     * Sets the submission tree's current selection to the next file in the current submission. If
     * the last file in the current submission is currently selected, this method does nothing. If
     * there is no current selection, this method selects the first file in the first submission, or
     * does nothing if there are no files.
     */
    public void goToNextFile() {
        if (!hasSelection()) {
            selectFirstSubmission();
            return;
        }

        if (lastFileInSubmissionSelected()) return;

        // get path to currently selected file
        TreePath file = tree.getSelectionPath();
        Object fileNode = file.getLastPathComponent();

        // get path to submission of that file
        TreePath submission = file.getParentPath();

        DefaultMutableTreeNode submissionNode = (DefaultMutableTreeNode)submission
                .getLastPathComponent();

        DefaultMutableTreeNode nextFile = (DefaultMutableTreeNode)submissionNode.getChildAfter(
                (TreeNode)fileNode);

        tree.setSelectionPath(new TreePath(nextFile.getPath()));
    }

    /**
     * Sets the submission tree's current selection to the previous file in the current submission.
     * If the first file in the current submission is currently selected, this method does nothing.
     * If there is no current selection, this method does nothing.
     */
    public void goToPreviousFile() {
        if (!hasSelection() || firstFileInSubmissionSelected()) return;

        // get path to currently selected file
        TreePath file = tree.getSelectionPath();
        Object fileNode = file.getLastPathComponent();

        // get path to submission of that file
        TreePath submission = file.getParentPath();

        DefaultMutableTreeNode submissionNode = (DefaultMutableTreeNode)submission
                .getLastPathComponent();

        DefaultMutableTreeNode prevFile = (DefaultMutableTreeNode)submissionNode.getChildBefore(
                (TreeNode)fileNode);

        tree.setSelectionPath(new TreePath(prevFile.getPath()));
    }
}
