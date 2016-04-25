package io.breen.socrates.file.plain;

import io.breen.socrates.file.File;
import io.breen.socrates.test.Node;

import java.util.*;

/**
 * The simplest kind of non-abstract File subclass. A PlainFile is any file that can be opened by a
 * simple text editor (for example, vim or Notepad). A PlainFile instance is an immutable
 * representation of a file expected to be part of a student submission.
 *
 * @see File
 */
public class PlainFile extends File {

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public PlainFile() {}

    public PlainFile(String path, double pointValue, Map<Date, Double> dueDates, List<Node> tests) {
        super(path, pointValue, dueDates, tests);
    }
}
