package io.breen.socrates.immutable.file.implementation;

import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.util.Either;

import java.nio.file.Path;
import java.util.*;

/**
 * The simplest kind of non-abstract File subclass. A PlainFile is any file that can be opened by a
 * simple text editor (for example, vim or Notepad). A PlainFile instance is an immutable
 * representation of a file expected to be part of a student submission.
 *
 * @see File
 */
public final class PlainFile extends File {

    public PlainFile(Path path, double pointValue, Map<Date, Double> dueDates,
                     List<Either<Test, TestGroup>> tests)
    {
        super(path, pointValue, "text/plain", dueDates, tests);
    }

    @Override
    public String getFileTypeName() {
        return "plain text file";
    }
}
