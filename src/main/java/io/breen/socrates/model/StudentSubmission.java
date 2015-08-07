package io.breen.socrates.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a single student submission. GradeReport objects are built using the
 * requirements read from the criteria and the contents of these objects.
 */
public class StudentSubmission {

    private List<File> files;

    public StudentSubmission() {
        files = new LinkedList<>();
    }

    public StudentSubmission(List<File> files) {
        files = new LinkedList<>(files);
    }
}
