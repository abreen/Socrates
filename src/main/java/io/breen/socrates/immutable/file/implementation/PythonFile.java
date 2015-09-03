package io.breen.socrates.immutable.file.implementation;

import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.util.Either;

import java.nio.file.Path;
import java.util.*;

/**
 * A PythonFile is a representation of a Python module (a file ending in .py containing valid Python
 * code) containing source code.
 */
public final class PythonFile extends File {

    /**
     * @throws io.breen.socrates.constructor.InvalidCriteriaException
     */
    public PythonFile(Path path, double pointValue, Map<Date, Double> dueDates,
                      List<Either<Test, TestGroup>> tests)
    {
        super(path, pointValue, "text/python", dueDates, tests);
        // TODO add more parameters and fields
    }

    @Override
    public String getFileTypeName() {
        return "Python source code";
    }
}
