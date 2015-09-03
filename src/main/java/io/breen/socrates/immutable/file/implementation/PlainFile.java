package io.breen.socrates.immutable.file.implementation;

import io.breen.socrates.immutable.file.File;

import java.util.*;

/**
 * The simplest kind of non-abstract File subclass. A PlainFile is any file that can be opened by a
 * simple text editor (for example, vim or Notepad). A PlainFile instance is an immutable
 * representation of a file expected to be part of a student submission.
 *
 * @see File
 */
public final class PlainFile extends File {

    public PlainFile() {}

    public PlainFile(String path, double pointValue, Map<Date, Double> dueDates,
                     List<Object> tests)
    {
        super(path, pointValue, "text/plain", dueDates, tests);
    }

    @Override
    public String getFileTypeName() {
        return "plain text file";
    }
}
