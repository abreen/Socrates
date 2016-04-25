package io.breen.socrates.test;

import io.breen.socrates.util.FrozenObjectModificationException;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Class representing a single test specified by the criteria. Instances of non-abstract subclasses of this class are
 * created when a Criteria object is created. All such instances should be immutable, since these objects represent the
 * test tree specified in the criteria file, and should be kept in correspondence with the file.
 *
 * TestNode objects represent leaves in the test tree. See GroupNode for the class that represents interior nodes.
 *
 * By default, tests are marked as passing or failing by a human grader. However, some tests support automation. Those
 * tests subclass TestNode but also implement the Automatable interface.
 *
 * @see io.breen.socrates.file.File
 * @see io.breen.socrates.criteria.Criteria
 * @see io.breen.socrates.test.Automatable
 */
public abstract class TestNode extends Node {

    private double deduction;

    private String description;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public TestNode() {}

    public TestNode(double deduction, String description) {
        this.deduction = deduction;
        this.description = description;
    }

    /**
     * A utility function for appending a string to a Document object. Useful for appending strings to the notes of a
     * test, or the transcript document.
     */
    public static void appendToDocument(final Document doc, final String s) {
        // TODO remove this
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        int length = doc.getLength();
                        try {
                            doc.insertString(length, s, null);
                        } catch (BadLocationException ignored) {
                        }
                    }
                }
        );
    }

    public double getDeduction() {
        return deduction;
    }

    public void setDeduction(double deduction) {
        checkFrozen();
        this.deduction = deduction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        checkFrozen();
        this.description = description;
    }

    public String toString() {
        return "TestNode(deduction=" + deduction + ")";
    }
}
