package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.model.TestResult;
import io.breen.socrates.model.wrapper.SubmittedFileWrapperNode;
import io.breen.socrates.model.wrapper.TestWrapperNode;
import io.breen.socrates.util.ObservableChangedEvent;
import io.breen.socrates.util.Observer;
import io.breen.socrates.view.icon.TestIcon;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.Enumeration;

public class TestTree implements Observer<TestWrapperNode> {

    public final Action nextTest;
    public final Action previousTest;
    private JPanel rootPanel;
    private JScrollPane scrollPane;
    private JTree tree;

    public TestTree(MenuBarManager menuBar, SubmissionTree submissionTree) {
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        int shift = InputEvent.SHIFT_DOWN_MASK;
        int alt = InputEvent.ALT_DOWN_MASK;

        nextTest = new AbstractAction(menuBar.nextTest.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                goToNextTest();
            }
        };
        nextTest.setEnabled(false);
        nextTest.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ctrl)
        );
        menuBar.nextTest.setAction(nextTest);

        previousTest = new AbstractAction(menuBar.previousTest.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                goToPreviousTest();
            }
        };
        previousTest.setEnabled(false);
        previousTest.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ctrl)
        );
        menuBar.previousTest.setAction(previousTest);

        submissionTree.addTreeSelectionListener(
                new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        TreePath path = e.getPath();
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path
                                .getLastPathComponent();

                        if (!e.isAddedPath()) node = null;

                        if (node != null) {
                            if (node instanceof SubmittedFileWrapperNode) {
                                SubmittedFileWrapperNode sfwn = (SubmittedFileWrapperNode)node;

                                update(sfwn.treeModel);

                                if (atLeastOneTestSelectable()) nextTest.setEnabled(true);
                                else nextTest.setEnabled(false);

                            } else {
                                nextTest.setEnabled(false);
                                previousTest.setEnabled(false);
                                reset();
                            }
                        } else {
                            nextTest.setEnabled(false);
                            previousTest.setEnabled(false);
                            reset();
                        }
                    }
                }
        );
    }

    private static String testGroupToString(TestGroup group) {
        int maxNum = group.maxNum;
        double maxValue = group.maxValue;

        DecimalFormat fmt = new DecimalFormat("#.#");

        if (maxNum == 0 && maxValue == 0.0) {
            return "fail any";
        } else if (maxNum != 0 && maxValue == 0.0) {
            return "fail ≤ " + maxNum;
        } else if (maxNum == 0 && maxValue != 0.0) {
            return "take ≤ " + fmt.format(maxValue) + " points";
        } else {
            return "fail ≤ " + maxNum + " and take ≤ " + fmt.format(maxValue) + " points";
        }
    }

    private void createUIComponents() {
        tree = new JTree((TreeModel)null) {
            @Override
            public String convertValueToText(Object value, boolean selected, boolean expanded,
                                             boolean leaf, int row, boolean hasFocus)
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
                Object userObject = node.getUserObject();

                if (userObject instanceof TestGroup) {
                    return testGroupToString((TestGroup)userObject);

                } else if (userObject instanceof Test) {
                    return ((Test)userObject).description;
                }

                return super.convertValueToText(
                        value, selected, expanded, leaf, row, hasFocus
                );
            }
        };

        /*
         * This ensures that only tests can be selected.
         */
        tree.setSelectionModel(
                new PredicateTreeSelectionModel() {
                    @Override
                    public boolean predicate(TreePath path) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path
                                .getLastPathComponent();
                        return node.getUserObject() instanceof Test;
                    }
                }
        );

        tree.addTreeSelectionListener(
                new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        TreePath path = e.getPath();
                        TestWrapperNode node = (TestWrapperNode)path.getLastPathComponent();

                        if (!e.isAddedPath()) node = null;

                        if (node == null) {
                            if (atLeastOneTestSelectable()) nextTest.setEnabled(true);
                            else nextTest.setEnabled(false);

                            previousTest.setEnabled(false);
                        } else {
                            if (lastTestForFileSelected()) nextTest.setEnabled(false);
                            else nextTest.setEnabled(true);

                            if (firstTestForFileSelected()) previousTest.setEnabled(false);
                            else previousTest.setEnabled(true);
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

                        if (value instanceof TestWrapperNode) {
                            TestWrapperNode testNode = (TestWrapperNode)value;

                            TestIcon i = TestControls.newIcon(testNode);
                            i.setIconHeight(16);
                            i.setIconWidth(16);

                            if (!selected) {
                                if (testNode.isConstrained()) {
                                    setForeground(UIManager.getColor("textInactiveText"));
                                } else {
                                    setForeground(Color.BLACK);
                                }
                            }

                            setIcon(i);

                        } else {
                            setIcon(null);
                        }

                        return this;
                    }
                }
        );

        tree.setShowsRootHandles(true);

        /*
         * Set up scroll pane.
         */
        scrollPane = new JScrollPane(tree);
        if (Globals.operatingSystem == Globals.OS.OSX) {
            Border border = new LineBorder(new Color(197, 197, 197));
            scrollPane.setBorder(border);
        }
    }

    public void update(DefaultTreeModel treeModel) {
        TreeModel currentModel = tree.getModel();
        if (currentModel != null) {
            @SuppressWarnings("unchecked") Enumeration<DefaultMutableTreeNode> dfs = getRoot()
                    .depthFirstEnumeration();
            while (dfs.hasMoreElements()) {
                DefaultMutableTreeNode n = dfs.nextElement();

                if (n instanceof TestWrapperNode) {
                    TestWrapperNode node = (TestWrapperNode)n;
                    node.removeObserver(this);
                }
            }
        }

        tree.setModel(treeModel);

        if (treeModel != null) {
            @SuppressWarnings("unchecked") Enumeration<DefaultMutableTreeNode> dfs = getRoot()
                    .depthFirstEnumeration();
            while (dfs.hasMoreElements()) {
                DefaultMutableTreeNode n = dfs.nextElement();

                if (n instanceof TestWrapperNode) {
                    TestWrapperNode node = (TestWrapperNode)n;
                    node.addObserver(this);
                }
            }
        }

        expandTree();
    }

    public void reset() {
        tree.setModel(null);
    }

    public void addTreeSelectionListener(TreeSelectionListener listener) {
        tree.addTreeSelectionListener(listener);
    }

    public TestWrapperNode getSelectedTestWrapperNode() {
        if (!hasSelection()) return null;

        return (TestWrapperNode)tree.getLastSelectedPathComponent();
    }

    public boolean hasSelection() {
        return tree.getSelectionPath() != null;
    }

    /**
     * Returns true if the currently selected test is the last test for this file, or false
     * otherwise. This method returns false if there is no selection.
     */
    public boolean lastTestForFileSelected() {
        if (!hasSelection()) return false;
        return getRoot().getLastLeaf() == tree.getLastSelectedPathComponent();
    }

    /**
     * Returns true if the currently selected test is the first test for this file, or false
     * otherwise. This method returns false if there is no selection.
     */
    public boolean firstTestForFileSelected() {
        if (!hasSelection()) return false;
        return getRoot().getFirstLeaf() == tree.getLastSelectedPathComponent();
    }

    private TestWrapperNode getFirstSelectableTest() {
        DefaultMutableTreeNode root = getRoot();
        if (root == null) return null;

        Enumeration<DefaultMutableTreeNode> dfs = root.depthFirstEnumeration();
        while (dfs.hasMoreElements()) {
            DefaultMutableTreeNode node = dfs.nextElement();

            if (node instanceof TestWrapperNode) {
                TestWrapperNode n = (TestWrapperNode)node;
                if (!n.isConstrained()) {
                    if (n.getResult() == TestResult.NONE) return n;
                }
            }
        }

        return null;
    }

    public boolean atLeastOneTestSelectable() {
        DefaultMutableTreeNode root = getRoot();
        if (root == null) return false;
        return getFirstSelectableTest() != null;
    }

    public void selectFirstTest() {
        DefaultMutableTreeNode root = getRoot();
        if (root == null) return;

        TestWrapperNode node = getFirstSelectableTest();
        if (node == null) return;
        tree.setSelectionPath(new TreePath(node.getPath()));
    }

    /**
     * Sets the test tree's current selection to the next test for this file. If the last test for
     * this file is selected, this method clears the selection. If no test is selected, this method
     * selects the first test.
     */
    public void goToNextTest() {
        if (!hasSelection()) {
            selectFirstTest();
            return;
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

        DefaultMutableTreeNode next = node.getNextLeaf();

        while (next != null && (!(next instanceof TestWrapperNode) || ((TestWrapperNode)next)
                .isConstrained())) {
            next = next.getNextLeaf();
        }

        if (next != null) {
            TreePath path = new TreePath(next.getPath());
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
        } else {
            tree.clearSelection();
        }
    }

    /**
     * Sets the test tree's current selection to the previous test for this file. If the first test
     * for this file is selected, this method does nothing. If no test is selected, this method does
     * nothing.
     */
    public void goToPreviousTest() {
        if (!hasSelection() || firstTestForFileSelected()) return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

        DefaultMutableTreeNode prev = node.getPreviousLeaf();

        while (prev != null && (!(prev instanceof TestWrapperNode) || ((TestWrapperNode)prev)
                .isConstrained())) {
            prev = prev.getPreviousLeaf();
        }

        if (prev != null) {
            TreePath path = new TreePath(prev.getPath());
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
        } else {
            tree.clearSelection();
        }
    }

    public void expandFirstTest() {
        DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode)getRoot().getFirstChild();
        tree.expandPath(new TreePath(firstChild.getPath()));
    }

    private void expandTree(TreePath parent) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)parent.getLastPathComponent();

        Enumeration<DefaultMutableTreeNode> children = root.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = children.nextElement();
            expandTree(parent.pathByAddingChild(child));
        }
        tree.expandPath(parent);
    }

    private void expandTree() {
        DefaultMutableTreeNode root = getRoot();
        if (root == null) return;
        expandTree(new TreePath(root.getPath()));
    }

    private DefaultMutableTreeNode getRoot() {
        TreeModel model = tree.getModel();
        return (DefaultMutableTreeNode)(model == null ? null : model.getRoot());
    }

    private DefaultMutableTreeNode getRoot(TreeModel model) {
        return (DefaultMutableTreeNode)model.getRoot();
    }

    private DefaultTreeModel getModel() {
        return (DefaultTreeModel)tree.getModel();
    }

    @Override
    public void objectChanged(ObservableChangedEvent<TestWrapperNode> event) {
        getModel().nodeChanged(event.source);
    }
}
