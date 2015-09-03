package io.breen.socrates.immutable.file.implementation;

import io.breen.socrates.immutable.file.File;

import java.util.*;

/**
 * A PythonFile is a representation of a Python module (a file ending in .py containing valid Python
 * code) containing source code.
 */
public final class PythonFile extends File {

    public PythonFile() {}

    public PythonFile(String path, double pointValue, Map<Date, Double> dueDates,
                      List<Object> tests)
    {
        super(path, pointValue, "text/python", dueDates, tests);
        // TODO add more parameters and fields
    }

    @Override
    public String getFileTypeName() {
        return "Python source code";
    }
}
