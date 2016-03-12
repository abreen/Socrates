package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.controller.MainController;
import io.breen.socrates.file.File;
import io.breen.socrates.model.event.GradeReportSavedEvent;
import io.breen.socrates.model.event.SubmissionCompletedChangeEvent;
import io.breen.socrates.model.wrapper.*;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.util.*;
import io.breen.socrates.util.Observer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Note: this class observes both SubmittedFileWrapperNodes and SubmissionWrapperNodes.
 */
public class SubmissionTree implements Observer {

    private static final String selectedHex = Globals.toHex(UIManager.getColor(
            "Tree.selectionForeground"));
    private static final String unknownFileHex = Globals.toHex(Color.GRAY);
    private static final String commentHex = Globals.toHex(Color.GRAY);
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

    public SubmissionTree(MenuBarManager menuBar, final MainController main, MainView view) {
        this.view = view;

        notSaved = new LinkedList<>();

        $$$setupUI$$$();
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        int shift = InputEvent.SHIFT_DOWN_MASK;
        int alt = InputEvent.ALT_DOWN_MASK;

        resetAllTests = new AbstractAction(menuBar.resetAllTests.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = getSelectedNode();
                if (node == null || !(node instanceof SubmittedFileWrapperNode)) return;

                ((SubmittedFileWrapperNode) node).resetAllTests();
            }
        };
        resetAllTests.setEnabled(false);
        resetAllTests.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, ctrl | shift)
        );
        menuBar.resetAllTests.setAction(resetAllTests);

        saveGradeReport = new AbstractAction(menuBar.saveGradeReport.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                SubmissionWrapperNode node = getCurrentSubmissionNode();
                Submission s = (Submission) node.getUserObject();

                Path dest = Paths.get(
                        s.submissionDir.toString(), Globals.DEFAULT_GRADE_FILE_NAME
                );
                main.saveGradeReport(node, dest);
            }
        };
        saveGradeReport.setEnabled(false);
        saveGradeReport.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl)
        );
        menuBar.saveGradeReport.setAction(saveGradeReport);

        saveGradeReportAs = new AbstractAction(menuBar.saveGradeReportAs.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                SubmissionWrapperNode node = getCurrentSubmissionNode();
                Submission s = (Submission) node.getUserObject();

                Path dest = chooseSaveLocation(s.submissionDir);
                if (dest == null) return;

                main.saveGradeReport(node, dest);
            }
        };
        saveGradeReportAs.setEnabled(false);
        saveGradeReportAs.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl | shift)
        );
        menuBar.saveGradeReportAs.setAction(saveGradeReportAs);

        nextSubmission = new AbstractAction(menuBar.nextSubmission.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = getSelectedNode();
                if (node instanceof SubmittedFileWrapperNode || node instanceof
                        UnrecognizedFileWrapperNode) {
                    select(getNextSibling(getParent(node)));
                } else if (node instanceof SubmissionWrapperNode) {
                    select(getNextSibling(node));
                }
            }
        };
        nextSubmission.setEnabled(false);
        nextSubmission.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_J, ctrl | shift)
        );
        menuBar.nextSubmission.setAction(nextSubmission);

        previousSubmission = new AbstractAction(menuBar.previousSubmission.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = getSelectedNode();
                if (node instanceof SubmittedFileWrapperNode || node instanceof
                        UnrecognizedFileWrapperNode) {
                    select(getPreviousSibling(getParent(node)));
                } else if (node instanceof SubmissionWrapperNode) {
                    select(getPreviousSibling(node));
                }
            }
        };
        previousSubmission.setEnabled(false);
        previousSubmission.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_K, ctrl | shift)
        );
        menuBar.previousSubmission.setAction(previousSubmission);

        revealSubmission = new AbstractAction(menuBar.revealSubmission.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                SubmissionWrapperNode node = getCurrentSubmissionNode();
                Submission s = (Submission) node.getUserObject();
                Path path = s.submissionDir;
                try {
                    Desktop.getDesktop().open(path.toFile());
                } catch (IOException x) {
                    logger.warning("got I/O exception revealing submission: " + x);
                }
            }
        };
        revealSubmission.setEnabled(false);
        revealSubmission.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, ctrl | shift)
        );
        menuBar.revealSubmission.setAction(revealSubmission);

        nextFile = new AbstractAction(menuBar.nextFile.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
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
        };
        nextFile.setEnabled(false);
        nextFile.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_J, ctrl | alt)
        );
        menuBar.nextFile.setAction(nextFile);

        previousFile = new AbstractAction(menuBar.previousFile.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
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
        };
        previousFile.setEnabled(false);
        previousFile.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_K, ctrl | alt)
        );
        menuBar.previousFile.setAction(previousFile);

        openFile = new AbstractAction(menuBar.openFile.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                SubmittedFile f = getSelectedSubmittedFile();
                Path path = f.fullPath;
                try {
                    Desktop.getDesktop().open(path.toFile());
                } catch (IOException x) {
                    logger.warning("got I/O exception opening file: " + x);
                }
            }
        };
        openFile.setEnabled(false);
        openFile.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ctrl)
        );
        menuBar.openFile.setAction(openFile);
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

    /**
     * This helper method is needed because (stupidly) the way to get the operating system's icon
     * for a file is different on OS X.
     */
    private static Icon getSystemIcon(Path p) {
        if (Globals.operatingSystem == Globals.OS.OSX) {
            JFileChooser c = new JFileChooser();
            return c.getIcon(p.toFile());
        } else {
            FileSystemView fsv = FileSystemView.getFileSystemView();
            return fsv.getSystemIcon(p.toFile());
        }
    }

    private void createUIComponents() {
        root = new DefaultMutableTreeNode(null);

        tree = new JTree(root) {
            @Override
            public String convertValueToText(Object value, boolean selected, boolean expanded,
                                             boolean leaf, int row, boolean hasFocus) {
                if (value instanceof SubmittedFileWrapperNode) {
                    SubmittedFileWrapperNode sfwn = (SubmittedFileWrapperNode) value;
                    return ((SubmittedFile) sfwn.getUserObject()).localPath.toString();

                } else if (value instanceof UnrecognizedFileWrapperNode) {
                    UnrecognizedFileWrapperNode ufwn = (UnrecognizedFileWrapperNode) value;
                    return ((SubmittedFile) ufwn.getUserObject()).localPath.toString();

                } else if (value instanceof SubmissionWrapperNode) {
                    SubmissionWrapperNode swn = (SubmissionWrapperNode) value;
                    Submission submission = (Submission) swn.getUserObject();
                    return submission.studentName;
                }

                return super.convertValueToText(
                        value, selected, expanded, leaf, row, hasFocus
                );
            }
        };

        tree.addTreeSelectionListener(
                new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        TreePath path = e.getPath();
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
                                .getLastPathComponent();

                        if (!e.isAddedPath()) node = null;

                        if (node == null) {
                            resetAllTests.setEnabled(false);

                            revealSubmission.setEnabled(false);
                            openFile.setEnabled(false);

                            nextSubmission.setEnabled(false);
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
                                    DefaultMutableTreeNode next = getNextNonLeafSibling(
                                            getParent(node)
                                    );

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

                                if (isLastSibling(getParent(node)))
                                    nextSubmission.setEnabled(false);
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
                                    DefaultMutableTreeNode nextNonLeaf = getNextNonLeafSibling
                                            (node);
                                    DefaultMutableTreeNode prevNonLeaf = getPreviousNonLeafSibling(
                                            node
                                    );

                                    if (nextNonLeaf == null) nextFile.setEnabled(false);
                                    else nextFile.setEnabled(true);

                                    if (prevNonLeaf == null) previousFile.setEnabled(false);
                                    else previousFile.setEnabled(true);
                                } else {
                                    nextFile.setEnabled(true);

                                    DefaultMutableTreeNode prevNonLeaf = getPreviousNonLeafSibling(
                                            node
                                    );

                                    if (prevNonLeaf == null) previousFile.setEnabled(false);
                                    else previousFile.setEnabled(true);
                                }
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

        final FileSystemView fsView = FileSystemView.getFileSystemView();

        tree.setCellRenderer(
                new DefaultTreeCellRenderer() {
                    @Override
                    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                                  boolean selected,
                                                                  boolean expanded, boolean isLeaf,
                                                                  int row, boolean focused) {
                        JLabel jl = (JLabel) super.getTreeCellRendererComponent(
                                tree, value, selected, expanded, isLeaf, row, focused
                        );

                        if (value instanceof UnrecognizedFileWrapperNode || value instanceof
                                SubmittedFileWrapperNode) {
                            /*
                             * Set the icon for this file using the system's preferred icon for
                             * the file type/extension.
                             */
                            SubmittedFile sf;

                            if (value instanceof UnrecognizedFileWrapperNode)
                                sf = (SubmittedFile) ((UnrecognizedFileWrapperNode) value)
                                        .getUserObject();
                            else
                                sf = (SubmittedFile) ((SubmittedFileWrapperNode) value)
                                        .getUserObject();

                            Icon i = getSystemIcon(sf.fullPath);
                            if (i != null) {
                                jl.setIcon(i);
                            }
                        }

                        String fileName = jl.getText();

                        // displayed in parentheses to the right of the file name
                        String comment = "";

                        if (value instanceof UnrecognizedFileWrapperNode) {
                            comment = "?";

                        } else if (value instanceof SubmittedFileWrapperNode) {
                            SubmittedFileWrapperNode sfwn = (SubmittedFileWrapperNode) value;

                            if (sfwn.isComplete()) comment = "complete";

                        } else if (value instanceof SubmissionWrapperNode) {
                            SubmissionWrapperNode swn = (SubmissionWrapperNode) value;

                            if (swn.isComplete()) {
                                comment = "complete";

                                if (!swn.isSaved()) comment += ", <b>unsaved</b>";
                            }
                        }

                        String s = "<html>";
                        if (selected) {
                            // all text should be the selected text color (probably white)
                            s += "<font color=\"" + selectedHex + "\">";
                            s += fileName;

                            if (!comment.isEmpty()) s += " (" + comment + ")";

                            s += "</font>";
                        } else {
                            s += fileName;

                            if (!comment.isEmpty()) {
                                s += " ";
                                s += "<font color=\"" + commentHex + "\">";
                                s += "(" + comment + ")";
                                s += "</font>";
                            }
                        }
                        s += "</html>";

                        jl.setText(s);

                        return jl;
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
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) return null;

        Object userObject = node.getUserObject();

        if (userObject instanceof SubmittedFile) return (SubmittedFile) userObject;
        else return null;
    }

    public DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
    }

    public SubmissionWrapperNode getCurrentSubmissionNode() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (node instanceof SubmissionWrapperNode) return (SubmissionWrapperNode) node;
        else if (node instanceof SubmittedFileWrapperNode || node instanceof
                UnrecognizedFileWrapperNode)
            return (SubmissionWrapperNode) node.getParent();
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

                if (f == null) {
                    unrecognized.add(new UnrecognizedFileWrapperNode(sf));
                } else {
                    SubmittedFileWrapperNode newSFWN = new SubmittedFileWrapperNode(sf, f);
                    newSFWN.addObserver(this);
                    recognized.add(newSFWN);
                }
            }

            for (MutableTreeNode r : recognized)
                parent.add(r);

            for (MutableTreeNode u : unrecognized)
                parent.add(u);

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
        DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode) root.getFirstChild();
        tree.expandPath(new TreePath(firstChild.getPath()));
    }

    public boolean hasSelection() {
        return tree.getSelectionPath() != null;
    }

    public void select(DefaultMutableTreeNode node) {
        TreePath path = new TreePath(node.getPath());
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }

    public DefaultMutableTreeNode getParent(DefaultMutableTreeNode node) {
        return (DefaultMutableTreeNode) node.getParent();
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
        return isFirstSibling((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
    }

    public boolean lastSiblingSelected() {
        if (!hasSelection()) return false;
        return isLastSibling((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
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
        return (DefaultMutableTreeNode) node.getFirstChild();
    }

    public DefaultMutableTreeNode getLastChild(DefaultMutableTreeNode node) {
        return (DefaultMutableTreeNode) node.getLastChild();
    }

    private DefaultTreeModel getModel() {
        return (DefaultTreeModel) tree.getModel();
    }

    @Override
    public void objectChanged(ObservableChangedEvent event) {
        if (event.source instanceof SubmissionWrapperNode) {
            SubmissionWrapperNode swn = (SubmissionWrapperNode) event.source;
            getModel().nodeChanged(swn);

        } else if (event.source instanceof SubmittedFileWrapperNode) {
            SubmittedFileWrapperNode sfwn = (SubmittedFileWrapperNode) event.source;
            getModel().nodeChanged(sfwn);

        }

        if (event instanceof SubmissionCompletedChangeEvent) {
            SubmissionCompletedChangeEvent e = (SubmissionCompletedChangeEvent) event;

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
            GradeReportSavedEvent e = (GradeReportSavedEvent) event;

            notSaved.remove(e.source);

            if (notSaved.isEmpty()) view.setAllSaved(true);

        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        rootPanel = new JPanel();
        rootPanel.setLayout(new BorderLayout(0, 0));
        scrollPane.setVerticalScrollBarPolicy(22);
        rootPanel.add(scrollPane, BorderLayout.CENTER);
        tree.setMaximumSize(new Dimension(-1, -1));
        scrollPane.setViewportView(tree);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
