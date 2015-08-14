package io.breen.socrates.immutable.file;

import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.util.Either;

import java.util.List;
import java.util.Map;

/**
 * A PythonFile is a representation of a Python module (a file ending in .py containing
 * valid Python code) containing source code.
 */
public final class PythonFile extends File {

    /**
     * @throws io.breen.socrates.constructor.InvalidCriteriaException
     */
    public PythonFile(String path, double pointValue, Map map, List<Either<Test, TestGroup>> tests) {
        super(path, pointValue, tests);
        // TODO set up other fields using map
    }
}
