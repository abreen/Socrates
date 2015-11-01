package io.breen.socrates.test;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Class representing a single test specified by the criteria. Instances of non-abstract subclasses
 * of this class are immutable, and are created when a Criteria object is created.
 *
 * All tests are associated with a "target" file. This target file is created after instances of its
 * tests are created.
 *
 * Minimally, a test must define a deduction: the points deducted from a student's score if the test
 * fails during grading.
 *
 * By default, tests are marked as passing or failing by a human grader. However, some tests support
 * automation. Those tests subclass Test but also implement the Automatable interface.
 *
 * @see io.breen.socrates.file.File
 * @see io.breen.socrates.criteria.Criteria
 * @see io.breen.socrates.test.Automatable
 */
public abstract class Test {

    public double deduction;
    public String description;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public Test() {}

    public Test(double deduction, String description) {
        this.deduction = deduction;
        this.description = description;
    }

    /**
     * A utility function for appending a string to a Document object. Useful for appending strings
     * to the notes of a test, or the transcript document.
     */
    public static void appendToDocument(Document doc, String s) {
        if (!s.endsWith("\n")) s += "\n";

        try {
            doc.insertString(doc.getLength(), s, null);
        } catch (BadLocationException ignored) {}
    }

    public String toString() {
        return "Test(deduction=" + deduction + ")";
    }

    /**
     * Returns the human-readable, user-friendly string representing the type of the test. This is
     * used by the GUI.
     */
    public abstract String getTestTypeName();
}
