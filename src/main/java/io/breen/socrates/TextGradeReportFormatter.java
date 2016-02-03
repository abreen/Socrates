package io.breen.socrates;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.File;
import io.breen.socrates.model.TestResult;
import io.breen.socrates.model.wrapper.*;
import io.breen.socrates.submission.*;
import io.breen.socrates.test.Test;

import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A formatter class that formats a grade report into plain text.
 */
public class TextGradeReportFormatter extends GradeReportFormatter {

    private final static SimpleDateFormat dateFmt = new SimpleDateFormat(
            "EEEE, MMMM d, yyyy h:mm:ss a"
    );

    private final static DecimalFormat decFmt = new DecimalFormat("#.#");

    public TextGradeReportFormatter(Criteria criteria) {
        super(criteria);
    }

    protected void format(SubmissionWrapperNode node, Writer w) throws IOException {
        Submission submission = (Submission)node.getUserObject();
        Date now = new Date();

        Map<File, SubmittedFileWrapperNode> map = new HashMap<>(criteria.files.size());
        @SuppressWarnings("unchecked") Enumeration<DefaultMutableTreeNode> children = node
                .children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode n = children.nextElement();

            if (n instanceof SubmittedFileWrapperNode) {
                SubmittedFileWrapperNode sfwn = (SubmittedFileWrapperNode)n;
                map.put(sfwn.matchingFile, sfwn);
            }
        }

        w.append(criteria.assignmentName);
        w.append(" Grade Report");
        line(w);

        w.append("Student: ");
        w.append(submission.studentName);
        line(w);

        w.append("Date: ");
        w.append(dateFmt.format(now));
        line(w);

        line(w);

        double totalPoints = 0.0;
        for (File file : criteria.files)
            totalPoints += file.pointValue;

        double deductedPoints = 0.0;

        for (File file : criteria.files) {
            w.append(file.path);
            w.append(" (");
            w.append(decFmt.format(file.pointValue));
            w.append(file.pointValue == 1 ? " point)" : " points)");
            line(w);

            SubmittedFileWrapperNode sfwn = map.get(file);
            if (sfwn == null) {
                deductedPoints += file.pointValue;

                w.append("not submitted");
                line(w);
                line(w);
                line(w);
                continue;
            }

            double deductedThisFile = 0.0;

            SubmittedFile submittedFile = (SubmittedFile)sfwn.getUserObject();

            Receipt receipt = submittedFile.receipt;
            if (receipt != null) {
                w.append("submitted on ");
                w.append(dateFmt.format(receipt.getLatestDate()));
                line(w);
            }

            line(w);

            List<Deduction> ds = getDeductions((TestGroupWrapperNode)sfwn.treeModel.getRoot());
            for (Deduction d : ds) {
                StringBuilder builder = new StringBuilder();
                builder.append("-");

                double displayedDeduction;
                boolean displayActual = false;

                if (deductedThisFile == file.pointValue) {
                    // we previously reached the deduction limit

                    displayedDeduction = 0.0;
                    displayActual = true;

                } else if (deductedThisFile + d.points <= file.pointValue) {
                    // subtracting this deduction would not cause us to go over the limit

                    displayedDeduction = d.points;

                } else {
                    // deductedThisFile != file.pointValue &&
                    // deductedThisFile + d.points > file.pointValue

                    // subtracting this deduction will cause us to go over the limit;
                    // display the rest of the points being taken away
                    displayedDeduction = file.pointValue - deductedThisFile;
                    displayActual = true;
                }

                builder.append(decFmt.format(displayedDeduction));
                builder.append("\t");
                builder.append(d.description);

                if (displayActual) {
                    builder.append(" [");
                    builder.append(decFmt.format(d.points));
                    builder.append("-point deduction]");
                }

                if (!d.notes.isEmpty()) {
                    builder.append("\n\n\tGrader notes: ");
                    builder.append(d.notes);
                    builder.append("\n");
                }

                w.append(builder.toString());
                line(w);

                deductedThisFile += d.points;
                if (deductedThisFile > file.pointValue) deductedThisFile = file.pointValue;
            }

            if (deductedThisFile == 0) {
                w.append("(no deductions)");
            }

            deductedPoints += deductedThisFile;

            line(w);
            line(w);
        }

        w.append("total: ");
        w.append(decFmt.format(totalPoints - deductedPoints));
        w.append("/");
        w.append(decFmt.format(totalPoints));
        line(w);

        w.close();
    }

    private void line(Writer writer) throws IOException {
        writer.append("\n");
    }

    private List<Deduction> getDeductions(TestGroupWrapperNode root) {
        List<Deduction> deductions = new LinkedList<>();

        @SuppressWarnings("unchecked") Enumeration<DefaultMutableTreeNode> dfs = root
                .depthFirstEnumeration();
        while (dfs.hasMoreElements()) {
            DefaultMutableTreeNode n = dfs.nextElement();

            if (n instanceof TestGroupWrapperNode) continue;
            if (n instanceof TestWrapperNode) {
                TestWrapperNode node = (TestWrapperNode)n;
                String notes = "";
                try {
                    notes = node.notes.getText(0, node.notes.getLength());
                } catch (BadLocationException ignored) {}

                Test test = (Test)node.getUserObject();
                if (node.getResult() == TestResult.FAILED)
                    deductions.add(new Deduction(test.deduction, test.description, notes));
                if (node.getResult() == TestResult.PASSED && !notes.isEmpty())
                    deductions.add(new Deduction(0, test.description, notes));
            }
        }

        return deductions;
    }

    private class Deduction {

        public final double points;
        public final String description;
        public final String notes;

        public Deduction(double points, String description, String notes) {
            this.points = points;
            this.description = description;
            this.notes = notes.trim();
        }

        public Deduction(double points, String description) {
            this(points, description, "");
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("-");
            builder.append(points);
            builder.append("\t");
            builder.append(description);

            if (!notes.isEmpty()) {
                builder.append("\n\n\tGrader notes: ");
                builder.append(notes);
                builder.append("\n");
            }

            return builder.toString();
        }
    }
}
