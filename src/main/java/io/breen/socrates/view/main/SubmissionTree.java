package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.controller.MainController;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.model.event.GradeReportSavedEvent;
import io.breen.socrates.model.event.SubmissionCompletedChangeEvent;
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
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

public class SubmissionTree implements Observer<SubmissionWrapperNode> {

    private static Logger logger = Logger.getLogger(SubmissionTree.class.getName());
    public final Action resetAllTests;
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
    private MainView view;
    private List<SubmissionWrapperNode> notSaved;

    public SubmissionTree(MenuBarManager menuBar, MainController main, MainView view) {
        this.view = view;

        notSaved = new LinkedList<>();

        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        int shift = InputEvent.SHIFT_DOWN_MASK;
        int alt = InputEvent.ALT_DOWN_MASK;

        resetAllTests = MenuBarManager.newMenuItemAction(
                menuBar.resetAllTests, e -> {
                    DefaultMutableTreeNode node = getSelectedNode();
                    if (node == null || !(node instanceof SubmittedFileWrapperNode)) return;

                    ((SubmittedFileWrapperNode)node).resetAllTests();
                }
        );
        resetAllTests.setEnabled(false);

        saveGradeReport = MenuBarManager.newMenuItemAction(
                menuBar.saveGradeReport, e -> {
                    SubmissionWrapperNode node = getCurrentSubmissionNode();
                    Submission s = (Submission)node.getUserObject();

                    Path dest = Paths.get(
                            s.submissionDir.toString(), Globals.DEFAULT_GRADE_FILE_NAME
                    );
                    main.saveGradeReport(node, dest);
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

                    Path dest = chooseSaveLocation(s.submissionDir);
                    if (dest == null) return;

                    main.saveGradeReport(node, dest);
                }
        );
        saveGradeReportAs.setEnabled(false);
        saveGradeReportAs.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl | shift)
        );

        nextSubmission = MenuBarManager.newMenuItemAction(
                menuBar.nextSubmission, e -> {
                    DefaultMutableTreeNode node = getSelectedNode();
                    if (node instanceof SubmittedFileWrapperNode || node instanceof
                            UnrecognizedFileWrapperNode) {
                        select(getNextSibling(getParent(node)));
                    } else if (node instanceof SubmissionWrapperNode) {
                        select(getNextSibling(node));
                    }
                }
        );
        nextSubmission.setEnabled(true);
        nextSubmission.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ctrl | shift)
        );

        previousSubmission = MenuBarManager.newMenuItemAction(
                menuBar.previousSubmission, e -> {
                    DefaultMutableTreeNode node = getSelectedNode();
                    if (node instanceof SubmittedFileWrapperNode || node instanceof
                            UnrecognizedFileWrapperNode) {
                        select(getPreviousSibling(getParent(node)));
                    } else if (node instanceof SubmissionWrapperNode) {
                        select(getPreviousSibling(node));
                    }
                }
        );
        previousSubmission.setEnabled(false);
        previousSubmission.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ctrl | shift)
        );

        revealSubmission = MenuBarManager.newMenuItemAction(
                menuBar.revealSubmission, e -> {
                    SubmissionWrapperNode node = getCurrentSubmissionNode();
                    Submission s = (Submission)node.getUserObject();
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
                menuBar.nextFile, e -> {
                    DefaultMutableTreeNode node = getSelectedNode();
                    if (node instanceof SubmittedFileWrapperNode || node instanceof
                            UnrecognizedFileWrapperNode) {
                        if (isLastSibling(node)) {
                            select(getFirstChild(getNextNonLeafSibling(getParent(node))));
                        } else {
                            select(getNextSibling(node));
                        }
                    } else if (node instanceof SubmissionWrapperNode) {
                        if (isLeaf(node)) {
                            select(getFirstChild(getNextSibling(node)));
                        } else {
                            select(getFirstChild(node));
                        }
                    }
                }
        );
        nextFile.setEnabled(true);
        nextFile.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ctrl | alt)
        );

        previousFile = MenuBarManager.newMenuItemAction(
                menuBar.previousFile, e -> {
                    DefaultMutableTreeNode node = getSelectedNode();
                    if (node instanceof SubmittedFileWrapperNode || node instanceof
                            UnrecognizedFileWrapperNode) {
                        if (isFirstSibling(node)) {
                            select(getLastChild(getPreviousNonLeafSibling(getParent(node))));
                        } else {
                            select(getPreviousSibling(node));
                        }
                    } else if (node instanceof SubmissionWrapperNode) {
                        select(getLastChild(getPreviousNonLeafSibling(node)));
                    }
                }
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

    private static Path chooseSaveLocation(Path initialDir) {
        JFileChooser fc = new JFileChooser(initialDir.toFile());
        fc.setSelectedFile(new java.io.File(Globals.DEFAULT_GRADE_FILE_NAME));
        int rv = fc.showSaveDialog(null);

        switch (rv) {
        case JFileChooser.APPROVE_OPTION:
            return fc.getSelectedFile().toPath();
        case JFileChooser.CANCEL_OPTION:
        default:
            return null;
        }
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

                } else if (value instanceof SubmissionWrapperNode) {
                    SubmissionWrapperNode swn = (SubmissionWrapperNode)value;
                    Submission submission = (Submission)swn.getUserObject();
                    return submission.studentName;
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
                        resetAllTests.setEnabled(false);

                        revealSubmission.setEnabled(false);
                        openFile.setEnabled(false);

                        nextSubmission.setEnabled(true);
                        previousSubmission.setEnabled(false);
                        nextFile.setEnabled(false);
                        previousFile.setEnabled(false);

                        saveGradeReport.setEnabled(false);
                        saveGradeReportAs.setEnabled(false);

                    } else {
                        revealSubmission.setEnabled(true);

                        SubmissionWrapperNode submissionNode = getCurrentSubmissionNode();

                        if (submissionNode.isComplete()) {
                            saveGradeReport.setEnabled(true);
                            saveGradeReportAs.setEnabled(true);
                        } else {
                            saveGradeReport.setEnabled(false);
                            saveGradeReportAs.setEnabled(false);
                        }

                        if (node instanceof SubmittedFileWrapperNode || node instanceof
                                UnrecognizedFileWrapperNode) {

                            openFile.setEnabled(true);

                            if (node instanceof SubmittedFileWrapperNode) {
                                resetAllTests.setEnabled(true);
                            } else {
                                resetAllTests.setEnabled(false);
                            }

                            if (isLastSibling(node)) {
                                DefaultMutableTreeNode next = getNextNonLeafSibling(getParent
                                                                                            (node));

                                if (next == null) nextFile.setEnabled(false);
                                else nextFile.setEnabled(true);
                            } else {
                                nextFile.setEnabled(true);
                            }

                            if (isFirstSibling(node)) {
                                DefaultMutableTreeNode next = getPreviousNonLeafSibling(
                                        getParent(
                                                node
                                        )
                                );
                               
                                if (next == null) previousFile.setEnabled(false);
                                else previousFile.setEnabled(true);
                            } else {
                                previousFile.setEnabled(true);
                            }

                            if (isLastSibling(getParent(node))) nextSubmission.setEnabled(false);
                            else nextSubmission.setEnabled(true);

                            if (isFirstSibling(getParent(node)))
                                previousSubmission.setEnabled(false);
                            else previousSubmission.setEnabled(true);

                        } else if (node instanceof SubmissionWrapperNode) {
                            openFile.setEnabled(false);

                            if (isLastSibling(node)) nextSubmission.setEnabled(false);
                            else nextSubmission.setEnabled(true);

                            if (isFirstSibling(node)) previousSubmission.setEnabled(false);
                            else previousSubmission.setEnabled(true);

                            if (isLeaf(node)) {
                                DefaultMutableTreeNode nextNonLeaf = getNextNonLeafSibling(node);
                                DefaultMutableTreeNode prevNonLeaf = getPreviousNonLeafSibling
                                        (node);

                                if (nextNonLeaf == null) nextFile.setEnabled(false);
                                else nextFile.setEnabled(true);

                                if (prevNonLeaf == null) previousFile.setEnabled(false);
                                else previousFile.setEnabled(true);
                            } else {
                                nextFile.setEnabled(true);

                                DefaultMutableTreeNode prevNonLeaf = getPreviousNonLeafSibling
                                        (node);

                                if (prevNonLeaf == null) previousFile.setEnabled(false);
                                else previousFile.setEnabled(true);
                            }
                        }
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

                        Font normal = Font.decode("Dialog");
                        Font italic = Font.decode("Dialog-ITALIC");
                        Color inactive = UIManager.getColor("textInactiveText");

                        setFont(normal);

                        if (!selected) {
                            if (value instanceof UnrecognizedFileWrapperNode) {
                                setForeground(inactive);

                            } else if (value instanceof SubmittedFileWrapperNode) {
                                SubmittedFileWrapperNode sfwn = (SubmittedFileWrapperNode)value;

                                if (sfwn.isComplete()) setForeground(Globals.GREEN);

                            } else if (value instanceof SubmissionWrapperNode) {
                                SubmissionWrapperNode swn = (SubmissionWrapperNode)value;
                                if (swn.isComplete()) setForeground(Globals.GREEN);
                            }
                        }

                        if (value instanceof SubmissionWrapperNode) {
                            SubmissionWrapperNode swn = (SubmissionWrapperNode)value;
                            if (swn.isComplete() && !swn.isSaved()) setFont(italic);
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

    public DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
    }

    public SubmissionWrapperNode getCurrentSubmissionNode() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

        if (node instanceof SubmissionWrapperNode) return (SubmissionWrapperNode)node;
        else if (node instanceof SubmittedFileWrapperNode || node instanceof
                UnrecognizedFileWrapperNode)
            return (SubmissionWrapperNode)node.getParent();
        else return null;
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

            /*
             * A submission might be "complete" at this point, if the submission contained no
             * expected files or was empty.
             */
            if (parent.isComplete()) {
                notSaved.add(parent);
                view.setAllSaved(false);
            }

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

    public void select(DefaultMutableTreeNode node) {
        tree.setSelectionPath(new TreePath(node.getPath()));
    }

    public DefaultMutableTreeNode getParent(DefaultMutableTreeNode node) {
        return (DefaultMutableTreeNode)node.getParent();
    }

    public boolean isLeaf(DefaultMutableTreeNode node) {
        return node.isLeaf();
    }

    public boolean isFirstSibling(DefaultMutableTreeNode node) {
        return node.getPreviousSibling() == null;
    }

    public boolean isLastSibling(DefaultMutableTreeNode node) {
        return node.getNextSibling() == null;
    }

    public boolean firstSiblingSelected() {
        if (!hasSelection()) return false;
        return isFirstSibling((DefaultMutableTreeNode)tree.getLastSelectedPathComponent());
    }

    public boolean lastSiblingSelected() {
        if (!hasSelection()) return false;
        return isLastSibling((DefaultMutableTreeNode)tree.getLastSelectedPathComponent());
    }

    public DefaultMutableTreeNode getNextSibling(DefaultMutableTreeNode node) {
        return node.getNextSibling();
    }

    public DefaultMutableTreeNode getPreviousSibling(DefaultMutableTreeNode node) {
        return node.getPreviousSibling();
    }

    public DefaultMutableTreeNode getNextNonLeafSibling(DefaultMutableTreeNode node) {
        do {
            node = getNextSibling(node);
        } while (node != null && node.getChildCount() == 0);

        return node;
    }

    public DefaultMutableTreeNode getPreviousNonLeafSibling(DefaultMutableTreeNode node) {
        do {
            node = getPreviousSibling(node);
        } while (node != null && node.getChildCount() == 0);

        return node;
    }

    public DefaultMutableTreeNode getFirstChild(DefaultMutableTreeNode node) {
        return (DefaultMutableTreeNode)node.getFirstChild();
    }

    public DefaultMutableTreeNode getLastChild(DefaultMutableTreeNode node) {
        return (DefaultMutableTreeNode)node.getLastChild();
    }

    private DefaultTreeModel getModel() {
        return (DefaultTreeModel)tree.getModel();
    }

    @Override
    public void objectChanged(ObservableChangedEvent<SubmissionWrapperNode> event) {
        getModel().nodeChanged(event.source);

        if (event instanceof SubmissionCompletedChangeEvent) {
            SubmissionCompletedChangeEvent e = (SubmissionCompletedChangeEvent)event;

            if (e.isNowComplete) {
                notSaved.add(e.source);
                view.setAllSaved(false);
            } else {
                notSaved.remove(e.source);
            }

            if (event.source == getCurrentSubmissionNode()) {
                if (e.isNowComplete) {
                    saveGradeReport.setEnabled(true);
                    saveGradeReportAs.setEnabled(true);
                } else {
                    saveGradeReport.setEnabled(false);
                    saveGradeReportAs.setEnabled(false);
                }
            }
        } else if (event instanceof GradeReportSavedEvent) {
            GradeReportSavedEvent e = (GradeReportSavedEvent)event;

            notSaved.remove(e.source);

            if (notSaved.isEmpty()) view.setAllSaved(true);

        }
    }
}
