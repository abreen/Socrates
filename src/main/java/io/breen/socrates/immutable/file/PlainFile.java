package io.breen.socrates.immutable.file;

import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.util.Either;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * The simplest kind of non-abstract File subclass. A PlainFile is any file that can
 * be opened by a simple text editor (for example, vim or Notepad). A PlainFile instance
 * is an immutable representation of a file expected to be part of a student
 * submission.
 *
 * @see File
 */
public final class PlainFile extends File {
    public PlainFile(String path,
                     double pointValue,
                     Map<LocalDateTime, Double> dueDates,
                     List<Either<Test, TestGroup>> tests)
    {
        super(path, pointValue, "text/plain", dueDates, tests);
    }
}
