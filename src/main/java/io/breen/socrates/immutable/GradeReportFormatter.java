package io.breen.socrates.immutable;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.model.wrapper.SubmissionWrapperNode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Abstract class representing a utility class that can "format" a SubmissionWrapperNode
 * representing a completely graded submission.
 */
public abstract class GradeReportFormatter {

    protected final Criteria criteria;

    public GradeReportFormatter(Criteria criteria) {
        this.criteria = criteria;
    }

    public String toString(SubmissionWrapperNode node) {
        StringWriter writer = new StringWriter();
        try {
            format(node, writer);
        } catch (IOException ignored) {}
        return writer.getBuffer().toString();
    }

    public Path toFile(SubmissionWrapperNode node) throws IOException {
        Path temp = Files.createTempFile(null, "grade.txt");
        toFile(node, temp);
        return temp;
    }

    public void toFile(SubmissionWrapperNode node, Path dest) throws IOException {
        format(node, new FileWriter(dest.toFile()));
    }

    protected abstract void format(SubmissionWrapperNode node, Writer writer) throws IOException;
}
