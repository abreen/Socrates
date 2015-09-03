package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.test.Automatable;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.model.AutomationStage;
import io.breen.socrates.model.TestResult;
import io.breen.socrates.model.event.*;
import io.breen.socrates.model.wrapper.TestWrapperNode;
import io.breen.socrates.util.ObservableChangedEvent;
import io.breen.socrates.util.Observer;
import io.breen.socrates.view.icon.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

public class TestControls implements Observer<TestWrapperNode> {

    private enum TestProperty {
        TEST_TYPE(0, "Test type", "—"),
        DEDUCTION(1, "Deduction", "—"),
        IS_AUTOMATED(2, "Automated", "—");

        public final int index;
        public final String labelText;
        public final String defaultText;

        TestProperty(int index, String labelText, String defaultText) {
            this.index = index;
            this.labelText = labelText;
            this.defaultText = defaultText;
        }

        public static String[] getKeys() {
            String[] keys = new String[TestProperty.values().length];
            int i = 0;
            for (TestProperty p : TestProperty.values())
                keys[i++] = p.labelText;
            return keys;
        }

        public static String[] getDefaults() {
            String[] defaults = new String[TestProperty.values().length];
            int i = 0;
            for (TestProperty p : TestProperty.values())
                defaults[i++] = p.defaultText;
            return defaults;
        }
    }
    private static final String NO_TEST_SELECTED_DESC = "(no test selected)";
    private static final Document EMPTY_DOCUMENT = new PlainDocument();
    private static final int ICON_WIDTH = 24;
    private static final int ICON_HEIGHT = 24;

    public final Action passTest;
    public final Action failTest;
    public final Action resetTest;
    public final Action clearNotes;
    public final Action focusOnNotes;

    private JPanel rootPanel;
    private JPanel innerPanel;
    private JPanel buttonPanel;
    private JButton failButton;
    private JButton passButton;
    private JTextArea notes;
    private PropertiesList properties;
    private JTextArea description;
    private JScrollPane notesScrollPane;
    private JLabel icon;
    private JButton resetButton;
    private TestWrapperNode currentNode;

    public TestControls(MenuBarManager menuBar, final TestTree testTree,
                        SubmissionTree submissionTree)
    {
        updateIcon();
        description.setText(NO_TEST_SELECTED_DESC);

        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        int shift = InputEvent.SHIFT_DOWN_MASK;
        int alt = InputEvent.ALT_DOWN_MASK;

        passTest = new AbstractAction(menuBar.passTest.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                TestWrapperNode node = testTree.getSelectedTestWrapperNode();
                Test test = (Test)node.getUserObject();

                if (test instanceof Automatable) {
                    AutomationStage stage = node.getAutomationStage();
                    switch (stage) {
                    case STARTED:
                        // cannot change result while test is running
                        return;
                    case FINISHED_NORMAL:
                    case NONE:
                        if (!userWantsToOverride()) return;
                    case FINISHED_ERROR:
                        node.setResult(TestResult.PASSED);
                    }
                } else {
                    node.setResult(TestResult.PASSED);
                }
            }
        };
        passTest.setEnabled(false);
        passTest.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UP, ctrl)
        );
        menuBar.passTest.setAction(passTest);

        failTest = new AbstractAction(menuBar.failTest.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                TestWrapperNode node = testTree.getSelectedTestWrapperNode();
                Test test = (Test)node.getUserObject();

                if (test instanceof Automatable) {
                    AutomationStage stage = node.getAutomationStage();
                    switch (stage) {
                    case STARTED:
                        // cannot change result while test is running
                        return;
                    case FINISHED_NORMAL:
                    case NONE:
                        if (!userWantsToOverride()) return;
                    case FINISHED_ERROR:
                        node.setResult(TestResult.FAILED);
                    }
                } else {
                    node.setResult(TestResult.FAILED);
                }
            }
        };
        failTest.setEnabled(false);
        failTest.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ctrl)
        );
        menuBar.failTest.setAction(failTest);

        resetTest = new AbstractAction(menuBar.resetTest.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                TestWrapperNode node = testTree.getSelectedTestWrapperNode();
                Test test = (Test)node.getUserObject();
                node.setResult(TestResult.NONE);
                if (test instanceof Automatable) node.setAutomationStage(AutomationStage.NONE);
            }
        };
        resetTest.setEnabled(false);
        menuBar.resetTest.setAction(resetTest);

        clearNotes = new AbstractAction(menuBar.clearNotes.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearNotes();
            }
        };
        clearNotes.setEnabled(false);
        clearNotes.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_K, ctrl)
        );
        menuBar.clearNotes.setAction(clearNotes);

        focusOnNotes = new AbstractAction(menuBar.focusOnNotes.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                focusOnNotes();
            }
        };
        focusOnNotes.setEnabled(false);
        focusOnNotes.putValue(
                Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ctrl)
        );
        menuBar.focusOnNotes.setAction(focusOnNotes);

        testTree.addTreeSelectionListener(
                new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        TreePath path = e.getPath();
                        TestWrapperNode node = (TestWrapperNode)path.getLastPathComponent();

                        if (node == null) {
                            clearNotes.setEnabled(false);
                            focusOnNotes.setEnabled(false);
                        } else {
                            update(node);
                            clearNotes.setEnabled(true);
                            focusOnNotes.setEnabled(true);
                        }
                    }
                }
        );

        submissionTree.addTreeSelectionListener(
                new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        reset();
                    }
                }
        );

        passButton.setAction(passTest);
        failButton.setAction(failTest);
        resetButton.setAction(resetTest);

        /*
         * The menu item text for the menu item Actions are too long for our buttons, so we
         * override the text from each Action here.
         */
        passButton.setText("Pass");
        failButton.setText("Fail");
        resetButton.setText("Reset");
    }

    public static TestIcon newIcon(TestWrapperNode node) {
        if (node == null) return new DefaultTestIcon();

        if (node.getAutomationStage() == AutomationStage.STARTED) return new RunningTestIcon();

        switch (node.getResult()) {
        case NONE:
            if (node.getAutomationStage() == AutomationStage.FINISHED_ERROR)
                return new ErrorTestIcon();
            else return new NoResultTestIcon();
        case PASSED:
            return new PassedTestIcon();
        case FAILED:
            return new FailedTestIcon();
        default:
            return new DefaultTestIcon();
        }
    }

    private void createUIComponents() {
        rootPanel = new JPanel();

        /*
         * Make center pane inset on OS X
         */
        if (Globals.operatingSystem == Globals.OS.OSX) {
            rootPanel.setBorder(UIManager.getBorder("InsetBorder.aquaVariant"));
        }

        /*
         * Set up the notes text area and scroll pane. If we are running on OS X, we
         * set the scroll pane's border color to better match the OS' style. We also
         * add padding to the notes text area.
         */
        notes = new JTextArea();
        notesScrollPane = new JScrollPane(notes);
        if (Globals.operatingSystem == Globals.OS.OSX) {
            Border border = new LineBorder(new Color(197, 197, 197));
            notesScrollPane.setBorder(border);
            notes.setBorder(new EmptyBorder(4, 4, 4, 4));
        }

        properties = new PropertiesList(
                TestProperty.getKeys(), TestProperty.getDefaults()
        );
        properties.setOpaque(false);
    }

    public void reset() {
        if (currentNode != null) currentNode.removeObserver(this);

        currentNode = null;

        description.setText(NO_TEST_SELECTED_DESC);

        updateIcon();

        properties.resetAll();

        notes.setEnabled(false);
        notes.setDocument(EMPTY_DOCUMENT);

        passTest.setEnabled(false);
        failTest.setEnabled(false);
        resetTest.setEnabled(false);
        clearNotes.setEnabled(false);
        focusOnNotes.setEnabled(false);
    }

    public void update(TestWrapperNode testNode) {
        if (testNode == null) {
            reset();
            return;
        }

        if (currentNode != null) currentNode.removeObserver(this);
        currentNode = testNode;
        currentNode.addObserver(this);

        Test test = (Test)testNode.getUserObject();
        DecimalFormat fmt = new DecimalFormat("#.#");

        description.setText(test.description);

        updateIcon();

        updateButtons();

        properties.set(
                TestProperty.TEST_TYPE.index, test.getTestTypeName()
        );

        properties.set(
                TestProperty.DEDUCTION.index,
                test.deduction == 1.0 ? "1 point" : fmt.format(test.deduction) + " points"
        );

        properties.set(
                TestProperty.IS_AUTOMATED.index, test instanceof Automatable ? "Yes" : "No"
        );

        notes.setEnabled(true);
        notes.setDocument(testNode.notes);
    }

    private void updateIcon() {
        TestIcon i = newIcon(currentNode);
        i.setIconHeight(24);
        i.setIconWidth(24);
        icon.setIcon(i);
    }

    private void updateButtons() {
        if (currentNode == null) {
            setEnabledForAllButtons(false);
        } else if (currentNode.isConstrained()) {
            resetTest.setEnabled(true);

            if (currentNode.getResult() == TestResult.FAILED) passTest.setEnabled(true);
            else passTest.setEnabled(false);

            failTest.setEnabled(false);
        } else {
            setEnabledForAllButtons(true);
        }
    }

    /**
     * Clears the notes Document of the currently selected test node.
     */
    public void clearNotes() {
        if (currentNode == null) return;

        try {
            currentNode.notes.remove(0, currentNode.notes.getLength());
        } catch (BadLocationException ignored) {}
    }

    public void focusOnNotes() {
        notes.requestFocusInWindow();
    }

    @Override
    public void objectChanged(ObservableChangedEvent<TestWrapperNode> event) {
        if (event instanceof ResultChangedEvent) {
            updateIcon();
        } else if (event instanceof ConstraintChangedEvent) {
            updateButtons();
        } else if (event instanceof StageChangedEvent) {
            updateIcon();
            AutomationStage stage = ((StageChangedEvent)event).newStage;
            switch (stage) {
            case STARTED:
                setEnabledForAllButtons(false);
                break;
            case FINISHED_ERROR:
            case FINISHED_NORMAL:
                setEnabledForAllButtons(true);
                break;
            }
        }
    }

    private void setEnabledForAllButtons(boolean enabled) {
        resetTest.setEnabled(enabled);
        passTest.setEnabled(enabled);
        failTest.setEnabled(enabled);
    }

    private boolean userWantsToOverride() {
        int rv = JOptionPane.showConfirmDialog(
                null,
                "This is an automated test. Are you sure you want to\noverride the automated " +
                        "test's result?",
                "Override Automated Test?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return rv == JOptionPane.YES_OPTION;
    }
}
