package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class SubmissionTree {

    private JPanel rootPanel;
    private JScrollPane scrollPane;

    private JTree tree;
    private DefaultMutableTreeNode ungradedRoot;
    private DefaultMutableTreeNode gradedRoot;
    private DefaultMutableTreeNode skippedRoot;

    private void createUIComponents() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(null);

        ungradedRoot = new DefaultMutableTreeNode("Ungraded");
        gradedRoot = new DefaultMutableTreeNode("Graded");
        skippedRoot = new DefaultMutableTreeNode("Skipped");

        root.add(ungradedRoot);
        root.add(gradedRoot);
        root.add(skippedRoot);

        tree = new JTree(root) {
            @Override
            public String convertValueToText(Object value, boolean selected,
                                             boolean expanded, boolean leaf, int row,
                                             boolean hasFocus)
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
                        value,
                        selected,
                        expanded,
                        leaf,
                        row,
                        hasFocus
                );
            }
        };

        tree.setSelectionModel(
                new DefaultTreeSelectionModel() {
                    private boolean pathEndsWithSubmittedFile(TreePath path) {
                        Object last = path.getLastPathComponent();
                        if (last instanceof DefaultMutableTreeNode) {
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode)last;
                            return node.getUserObject() instanceof SubmittedFile;
                        }

                        return false;
                    }

                    @Override
                    public void setSelectionPath(TreePath path) {
                        if (pathEndsWithSubmittedFile(path))
                            super.setSelectionPath(path);
                    }

                    @Override
                    public void setSelectionPaths(TreePath[] paths) {
                        TreePath[] filteredPaths = Arrays.copyOf(paths, paths.length);
                        for (int i = 0; i < filteredPaths.length; i++)
                            if (!pathEndsWithSubmittedFile(filteredPaths[i]))
                                filteredPaths[i] = null;

                        super.setSelectionPaths(filteredPaths);
                    }

                    @Override
                    public void addSelectionPath(TreePath path) {
                        if (pathEndsWithSubmittedFile(path))
                            super.addSelectionPath(path);
                    }

                    @Override
                    public void addSelectionPaths(TreePath[] paths) {
                        TreePath[] filteredPaths = Arrays.copyOf(paths, paths.length);
                        for (int i = 0; i < filteredPaths.length; i++)
                            if (!pathEndsWithSubmittedFile(filteredPaths[i]))
                                filteredPaths[i] = null;

                        super.addSelectionPaths(filteredPaths);
                    }
                }
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
        if (node == null)
            return null;

        Object userObject = node.getUserObject();

        if (userObject instanceof SubmittedFile)
            return (SubmittedFile)userObject;
        else
            return null;
    }

    public void addUngraded(List<Submission> submissions) {
        for (Submission s : submissions) {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(s);

            for (SubmittedFile f : s.files)
                root.add(new DefaultMutableTreeNode(f));

            ungradedRoot.add(root);
        }
    }

    public void setActiveSubmittedFile(SubmittedFile submittedFile) {
        Enumeration ungraded = ungradedRoot.depthFirstEnumeration();
        while (ungraded.hasMoreElements()) {
            Object node = ungraded.nextElement();
            if (node instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode)node;
                if (mutableTreeNode.getUserObject() == submittedFile) {
                    tree.addSelectionPath(new TreePath(mutableTreeNode.getPath()));
                    break;
                }
            }
        }
    }
}
