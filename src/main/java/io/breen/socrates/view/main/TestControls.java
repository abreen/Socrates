package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.test.Automatable;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.model.TestNode;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.DecimalFormat;

public class TestControls {

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
    private JButton failsButton;
    private JButton passesButton;
    private JTextArea notes;
    private PropertiesList properties;
    private JTextArea description;
    private JScrollPane notesScrollPane;

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

    public void update(TestNode testNode) {
        Test test = testNode.test;
        DecimalFormat fmt = new DecimalFormat("#.#");

        description.setText(test.description);

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
    }
}
