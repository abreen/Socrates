package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.model.wrapper.*;
import io.breen.socrates.util.*;
import io.breen.socrates.util.Observer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

public class SubmissionTree implements Observer<SubmissionWrapperNode> {

    private static Logger logger = Logger.getLogger(SubmissionTree.class.getName());
    public final Action saveGradeReport;
    public final Action saveGradeReportAs;
    public final Action nextSubmission;
    public final Action previousSubmission;
    public final Action revealSubmission;
    public final Action nextFile;
    public final Action previousFile;
    public final Action openFile;
    private JPanel rootPanel;
    private JScrollPane scrollPane;
    private JTree tree;
    private DefaultMutableTreeNode root;

    public SubmissionTree(MenuBarManager menuBar) {
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        int shift = InputEvent.SHIFT_DOWN_MASK;
        int alt = InputEvent.ALT_DOWN_MASK;

        saveGradeReport = MenuBarManager.newMenuItemAction(
                menuBar.saveGradeReport, e -> {
                    SubmissionWrapperNode node = getCurrentSubmissionNode();
                    Submission s = (Submission)node.getUserObject();
                    Path dest = s.submissionDir;

                    // TODO
                }
        );
        saveGradeReport.setEnabled(false);
        saveGradeReport.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl)
        );

        saveGradeReportAs = MenuBarManager.newMenuItemAction(
                menuBar.saveGradeReportAs, e -> {
                    SubmissionWrapperNode node = getCurrentSubmissionNode();
                    Submission s = (Submission)node.getUserObject();
                    Path dest = s.submissionDir;

                    // TODO
                }
        );
        saveGradeReportAs.setEnabled(false);
        saveGradeReportAs.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl | shift)
        );

        nextSubmission = MenuBarManager.newMenuItemAction(
                menuBar.nextSubmission, e -> goToNextSubmission()
        );
        nextSubmission.setEnabled(true);
        nextSubmission.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ctrl | shift)
        );

        previousSubmission = MenuBarManager.newMenuItemAction(
                menuBar.previousSubmission, e -> goToPreviousSubmission()
        );
        previousSubmission.setEnabled(false);
        previousSubmission.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ctrl | shift)
        );

        revealSubmission = MenuBarManager.newMenuItemAction(
                menuBar.revealSubmission, e -> {
                    Submission s = getSelectedSubmission();
                    Path path = s.submissionDir;
                    try {
                        Desktop.getDesktop().open(path.toFile());
                    } catch (IOException x) {
                        logger.warning("got I/O exception revealing submission: " + x);
                    }
                }
        );
        revealSubmission.setEnabled(false);
        revealSubmission.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, ctrl | shift)
        );

        nextFile = MenuBarManager.newMenuItemAction(
                menuBar.nextFile, e -> goToNextFile()
        );
        nextFile.setEnabled(true);
        nextFile.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ctrl | alt)
        );

        previousFile = MenuBarManager.newMenuItemAction(
                menuBar.previousFile, e -> goToPreviousFile()
        );
        previousFile.setEnabled(false);
        previousFile.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ctrl | alt)
        );

        openFile = MenuBarManager.newMenuItemAction(
                menuBar.openFile, e -> {
                    SubmittedFile f = getSelectedSubmittedFile();
                    Path path = f.fullPath;
                    try {
                        Desktop.getDesktop().open(path.toFile());
                    } catch (IOException x) {
                        logger.warning("got I/O exception revealing file: " + x);
                    }
                }
        );
        openFile.setEnabled(false);
    }

    private void createUIComponents() {
        root = new DefaultMutableTreeNode(null);

        tree = new JTree(root) {
            @Override
            public String convertValueToText(Object value, boolean selected, boolean expanded,
                                             boolean leaf, int row, boolean hasFocus)
            {
                if (value instanceof SubmittedFileWrapperNode) {
                    SubmittedFileWrapperNode sfwn = (SubmittedFileWrapperNode)value;
                    return ((SubmittedFile)sfwn.getUserObject()).localPath.toString();

                } else if (value instanceof UnrecognizedFileWrapperNode) {
                    UnrecognizedFileWrapperNode ufwn = (UnrecognizedFileWrapperNode)value;
                    return ((SubmittedFile)ufwn.getUserObject()).localPath.toString();

                } else if (value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)value;
                    Object userObject = dmtn.getUserObject();

                    if (userObject instanceof Submission) {
                        return ((Submission)userObject).studentName;
                    }
                }

                return super.convertValueToText(
                        value, selected, expanded, leaf, row, hasFocus
                );
            }
        };

        tree.addTreeSelectionListener(
                event -> {
                    TreePath path = event.getPath();
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)path
                            .getLastPathComponent();

                    if (!event.isAddedPath()) node = null;

                    if (node == null) {
                        revealSubmission.setEnabled(false);
                        openFile.setEnabled(false);

                        nextSubmission.setEnabled(true);
                        previousSubmission.setEnabled(false);
                        nextFile.setEnabled(true);
                        previousFile.setEnabled(false);
                    } else {
                        revealSubmission.setEnabled(true);
                        openFile.setEnabled(true);

                        if (lastSubmissionSelected()) nextSubmission.setEnabled(false);
                        else nextSubmission.setEnabled(true);

                        if (firstSubmissionSelected()) previousSubmission.setEnabled(false);
                        else previousSubmission.setEnabled(true);

                        if (lastFileInSubmissionSelected()) nextFile.setEnabled(false);
                        else nextFile.setEnabled(true);

                        if (firstFileInSubmissionSelected()) previousFile.setEnabled(false);
                        else previousFile.setEnabled(true);
                    }
                }
        );

        tree.addMouseListener(
                new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int row = tree.getRowForLocation(e.getX(), e.getY());
                        if (row == -1) tree.clearSelection();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {

                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {

                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {

                    }

                    @Override
                    public void mouseExited(MouseEvent e) {

                    }
                }
        );

        tree.setCellRenderer(
                new DefaultTreeCellRenderer() {
                    @Override
                    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                                  boolean selected,
                                                                  boolean expanded, boolean isLeaf,
                                                                  int row, boolean focused)
                    {
                        super.getTreeCellRendererComponent(
                                tree, value, selected, expanded, isLeaf, row, focused
                        );

                        if (!selected) {
                            if (value instanceof UnrecognizedFileWrapperNode) {
                                setForeground(UIManager.getColor("textInactiveText"));
                            } else if (value instanceof SubmittedFileWrapperNode) {
                                SubmittedFileWrapperNode sfwn = (SubmittedFileWrapperNode)value;
                                if (sfwn.isComplete()) setForeground(Globals.GREEN);
                            } else if (value instanceof SubmissionWrapperNode) {
                                SubmissionWrapperNode swn = (SubmissionWrapperNode)value;
                                if (swn.isComplete()) setForeground(Globals.GREEN);
                            }
                        }

                        return this;
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

    public DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
    }

    public SubmissionWrapperNode getCurrentSubmissionNode() {
        TreePath file = tree.getSelectionPath();
        return (SubmissionWrapperNode)file.getParentPath().getLastPathComponent();
    }

    public void addUngraded(Map<Submission, List<Pair<SubmittedFile, File>>> map) {
        for (Map.Entry<Submission, List<Pair<SubmittedFile, File>>> entry : map.entrySet()) {
            Submission s = entry.getKey();

            SubmissionWrapperNode parent = new SubmissionWrapperNode(s);

            List<MutableTreeNode> recognized = new LinkedList<>();
            List<MutableTreeNode> unrecognized = new LinkedList<>();

            List<Pair<SubmittedFile, File>> pairs = entry.getValue();
            for (Pair<SubmittedFile, File> p : pairs) {
                SubmittedFile sf = p.first;
                File f = p.second;

                if (f == null) unrecognized.add(new UnrecognizedFileWrapperNode(sf));
                else recognized.add(new SubmittedFileWrapperNode(sf, f));
            }

            recognized.forEach(parent::add);
            unrecognized.forEach(parent::add);

            parent.addObserver(this);

            root.add(parent);
        }
    }

    public void expandFirstSubmission() {
        DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode)root.getFirstChild();
        tree.expandPath(new TreePath(firstChild.getPath()));
    }

    public boolean hasSelection() {
        return tree.getSelectionPath() != null;
    }

    /**
     * Returns true if the currently selected file belongs to the first submission in the tree.
     * This method returns false if there is no selection.
     */
    public boolean firstSubmissionSelected() {
        if (!hasSelection()) return false;

        TreePath file = tree.getSelectionPath();
        TreePath submission = file.getParentPath();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)submission.getLastPathComponent();

        return root.getFirstChild() == node;
    }

    /**
     * Returns true if the currently selected file belongs to the last submission in the tree.
     * This method returns false if there is no selection.
     */
    public boolean lastSubmissionSelected() {
        if (!hasSelection()) return false;

        TreePath file = tree.getSelectionPath();
        TreePath submission = file.getParentPath();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)submission.getLastPathComponent();

        return root.getLastChild() == node;
    }

    public void selectFirstSubmission() {
        DefaultMutableTreeNode submission = (DefaultMutableTreeNode)root.getFirstChild();

        while (submission != null && submission.getChildCount() == 0)
            submission = submission.getNextSibling();

        if (submission == null) return;

        DefaultMutableTreeNode child = (DefaultMutableTreeNode)submission.getFirstChild();
        tree.setSelectionPath(new TreePath(child.getPath()));
    }

    /**
     * Sets the submission tree's current selection to the first file in the next submission in the
     * tree. If the last submission is currently selected, this method does nothing. If there is
     * no current selection, this method selects the first submission.
     */
    public void goToNextSubmission() {
        if (!hasSelection()) {
            selectFirstSubmission();
            return;
        }

        if (lastSubmissionSelected()) return;

        TreePath file = tree.getSelectionPath();
        TreePath submission = file.getParentPath();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)submission.getLastPathComponent();

        do {
            node = node.getNextSibling();
        } while (node != null && node.getChildCount() == 0);

        if (node == null) return;

        DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getFirstChild();
        tree.setSelectionPath(new TreePath(child.getPath()));
    }

    /**
     * Sets the submission tree's current selection to the first file in the previous submission in
     * the tree. If the first submission is currently selected, this method does nothing. If there
     * is no current selection, this method does nothing.
     */
    public void goToPreviousSubmission() {
        if (!hasSelection() || firstSubmissionSelected()) return;

        TreePath file = tree.getSelectionPath();
        TreePath submission = file.getParentPath();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)submission.getLastPathComponent();

        do {
            node = node.getPreviousSibling();
        } while (node != null && node.getChildCount() == 0);

        if (node == null) return;

        DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getFirstChild();
        tree.setSelectionPath(new TreePath(child.getPath()));
    }

    /**
     * Returns true if the currently selected file is the first file in the currently selected
     * submission. This method returns false if there is no selection.
     */
    public boolean firstFileInSubmissionSelected() {
        if (!hasSelection()) return false;

        TreePath file = tree.getSelectionPath();
        TreePath submission = file.getParentPath();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)submission.getLastPathComponent();

        TreeNode firstChild = node.getFirstChild();

        return file.getLastPathComponent() == firstChild;
    }

    /**
     * Returns true if the currently selected file is the last file in the currently selected
     * submission. This method returns false if there is no selection.
     */
    public boolean lastFileInSubmissionSelected() {
        if (!hasSelection()) return false;

        TreePath file = tree.getSelectionPath();
        TreePath submission = file.getParentPath();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)submission.getLastPathComponent();

        TreeNode lastChild = node.getLastChild();

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

        DefaultMutableTreeNode file = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        DefaultMutableTreeNode sibling = file.getNextSibling();

        if (sibling == null) return;

        tree.setSelectionPath(new TreePath(sibling.getPath()));
    }

    /**
     * Sets the submission tree's current selection to the previous file in the current submission.
     * If the first file in the current submission is currently selected, this method does nothing.
     * If there is no current selection, this method does nothing.
     */
    public void goToPreviousFile() {
        if (!hasSelection() || firstFileInSubmissionSelected()) return;

        DefaultMutableTreeNode file = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        DefaultMutableTreeNode sibling = file.getPreviousSibling();

        if (sibling == null) return;

        tree.setSelectionPath(new TreePath(sibling.getPath()));
    }

    private DefaultTreeModel getModel() {
        return (DefaultTreeModel)tree.getModel();
    }

    @Override
    public void objectChanged(ObservableChangedEvent<SubmissionWrapperNode> event) {
        getModel().nodeChanged(event.source);
    }
}
