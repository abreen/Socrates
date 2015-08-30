package io.breen.socrates.immutable;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.*;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.model.TestResult;
import io.breen.socrates.model.wrapper.*;

import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * A formatter class that formats a grade report into plain text.
 */
public class TextGradeReportFormatter extends GradeReportFormatter {

    private final static DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern(
            "EEEE, MMMM d, yyyy h:mm:ss a"
    );

    private final static DecimalFormat decFmt = new DecimalFormat("#.#");

    public TextGradeReportFormatter(Criteria criteria) {
        super(criteria);
    }

    protected void format(SubmissionWrapperNode node, Writer w) throws IOException {
        Submission submission = (Submission)node.getUserObject();
        LocalDateTime now = LocalDateTime.now();

        Map<File, SubmittedFileWrapperNode> map = new HashMap<>(criteria.files.size());
        Enumeration<DefaultMutableTreeNode> children = node.children();
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
        double earnedPoints = 0.0;
        for (File file : criteria.files.values()) {
            totalPoints += file.pointValue;
            earnedPoints += file.pointValue;

            w.append(file.localPath.toString());
            w.append(" (");
            w.append(decFmt.format(file.pointValue));
            if (file.pointValue == 1) w.append(" point)");
            else w.append(" points)");
            line(w);

            SubmittedFileWrapperNode sfwn = map.get(file);
            if (sfwn == null) {
                earnedPoints -= file.pointValue;
                w.append("not submitted");
                line(w);
                line(w);
                line(w);
                continue;
            }

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
                earnedPoints -= d.points;
                w.append(d.toString());
                line(w);
            }

            line(w);
            line(w);
        }

        w.append("total: ");
        w.append(decFmt.format(earnedPoints));
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

        Enumeration<DefaultMutableTreeNode> dfs = root.depthFirstEnumeration();
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
            this.notes = notes;
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

            if (notes.length() > 0) {
                builder.append("\n\n\tGrader notes: ");
                builder.append(notes);
            }

            return builder.toString();
        }
    }
}
