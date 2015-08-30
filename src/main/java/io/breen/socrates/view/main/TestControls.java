package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.test.Automatable;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.model.*;
import io.breen.socrates.util.ObservableChangedEvent;
import io.breen.socrates.util.Observer;
import io.breen.socrates.view.icon.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
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

    public TestControls() {
        updateIcon();
        description.setText(NO_TEST_SELECTED_DESC);
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

    public void update(TestWrapperNode testNode) {
        if (testNode == null) {
            if (currentNode != null) currentNode.removeObserver(this);

            currentNode = null;

            description.setText(NO_TEST_SELECTED_DESC);

            updateIcon();

            properties.resetAll();

            notes.setEnabled(false);
            notes.setDocument(EMPTY_DOCUMENT);

            resetButton.getAction().setEnabled(false);
            passButton.getAction().setEnabled(false);
            failButton.getAction().setEnabled(false);

            return;
        }

        currentNode.removeObserver(this);
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

    /**
     * Sets the Action for the "Pass" button. The button's text will not be changed to the Action's
     * text --- the old text will be retained.
     */
    public void setPassTestAction(Action a) {
        String textBefore = passButton.getText();
        passButton.setAction(a);
        passButton.setText(textBefore);
    }

    /**
     * Sets the Action for the "Fail" button. The button's text will not be changed to the Action's
     * text --- the old text will be retained.
     */
    public void setFailTestAction(Action a) {
        String textBefore = failButton.getText();
        failButton.setAction(a);
        failButton.setText(textBefore);
    }

    /**
     * Sets the Action for the "Reset" button. The button's text will not be changed to the Action's
     * text --- the old text will be retained.
     */
    public void setResetTestAction(Action a) {
        String textBefore = resetButton.getText();
        resetButton.setAction(a);
        resetButton.setText(textBefore);
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
            resetButton.getAction().setEnabled(true);
            if (currentNode.getResult() == TestResult.FAILED)
                passButton.getAction().setEnabled(true);
            else passButton.getAction().setEnabled(false);
            failButton.getAction().setEnabled(false);
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
        resetButton.getAction().setEnabled(enabled);
        passButton.getAction().setEnabled(enabled);
        failButton.getAction().setEnabled(enabled);
    }
}
