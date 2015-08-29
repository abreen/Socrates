package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.test.Automatable;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.model.TestResult;
import io.breen.socrates.model.TestWrapperNode;
import io.breen.socrates.util.Observer;
import io.breen.socrates.view.icon.DefaultTestIcon;
import io.breen.socrates.view.icon.FailedTestIcon;
import io.breen.socrates.view.icon.NoResultTestIcon;
import io.breen.socrates.view.icon.PassedTestIcon;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.DecimalFormat;

public class TestControls implements Observer<TestResult> {

    private static final String NO_TEST_SELECTED_DESC = "(no test selected)";

    static final Icon ICON_NORESULT = new NoResultTestIcon();
    static final Icon ICON_PASSED = new PassedTestIcon();
    static final Icon ICON_FAILED = new FailedTestIcon();

    private static final Icon LARGE_ICON_DEFAULT = new DefaultTestIcon(24, 24);
    private static final Icon LARGE_ICON_NORESULT = new NoResultTestIcon(24, 24);
    private static final Icon LARGE_ICON_PASSED = new PassedTestIcon(24, 24);
    private static final Icon LARGE_ICON_FAILED = new FailedTestIcon(24, 24);

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

    private TestWrapperNode currentNode;

    public TestControls() {
        description.setText(NO_TEST_SELECTED_DESC);
    }

    private void createUIComponents() {
        rootPanel = new JPanel();

        /*
         * Make center pane inset on OS X
         */
        if (Globals.operatingSystem == Globals.OS.OSX) {
            rootPanel.setBorder(UIManager.getBorder("InsetBorder.aquaVariant"));
        }

        icon = new JLabel(LARGE_ICON_DEFAULT);

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
            if (currentNode != null)
                currentNode.resetObserver();

            currentNode = null;
            description.setText(NO_TEST_SELECTED_DESC);
            icon.setIcon(LARGE_ICON_DEFAULT);
            properties.resetAll();
            return;
        }

        testNode.setObserver(this);

        Test test = (Test)testNode.getUserObject();
        DecimalFormat fmt = new DecimalFormat("#.#");

        description.setText(test.description);

        changeIcon(testNode.getResult());

        properties.set(
                TestProperty.TEST_TYPE.index,
                test.getClass().getSimpleName()
        );

        properties.set(
                TestProperty.DEDUCTION.index,
                test.deduction == 1.0 ? "1 point" : fmt.format(test.deduction) + " points"
        );

        properties.set(
                TestProperty.IS_AUTOMATED.index,
                test instanceof Automatable ? "Yes" : "No"
        );

        notes.setDocument(testNode.getNotesDocument());

        currentNode = testNode;
    }

    public void setPassTestAction(Action a) {
        passButton.setAction(a);
    }

    public void setFailTestAction(Action a) {
        failButton.setAction(a);
    }

    private void changeIcon(TestResult result) {
        Icon resultIcon;
        switch (result) {
        case NONE:
            resultIcon = LARGE_ICON_NORESULT;
            break;
        case PASSED:
            resultIcon = LARGE_ICON_PASSED;
            break;
        case FAILED:
            resultIcon = LARGE_ICON_FAILED;
            break;
        default:
            resultIcon = LARGE_ICON_DEFAULT;
        }
        icon.setIcon(resultIcon);
    }

    public void objectChanged(TestResult object) {
        changeIcon(object);
    }
}
