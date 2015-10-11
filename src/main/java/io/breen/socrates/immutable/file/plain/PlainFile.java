package io.breen.socrates.immutable.file.plain;

import io.breen.socrates.immutable.PostConstructionAction;
import io.breen.socrates.immutable.file.File;

import java.util.*;

/**
 * The simplest kind of non-abstract File subclass. A PlainFile is any file that can be opened by a
 * simple text editor (for example, vim or Notepad). A PlainFile instance is an immutable
 * representation of a file expected to be part of a student submission.
 *
 * @see File
 */
public final class PlainFile extends File implements PostConstructionAction {

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public PlainFile() {
        language = null;
        contentsArePlainText = true;
    }

    public PlainFile(String path, double pointValue, Map<Date, Double> dueDates,
                     List<Object> tests)
    {
        super(path, pointValue, dueDates, tests);
    }

    @Override
    public void afterConstruction() {
        super.afterConstruction();
    }

    @Override
    public String getFileTypeName() {
        return "plain text file";
    }
}
