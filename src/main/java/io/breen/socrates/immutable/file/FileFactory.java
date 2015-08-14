package io.breen.socrates.immutable.file;

import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.util.Either;

import java.util.List;
import java.util.Map;

public class FileFactory {
    public static File buildFile(FileType type, String path, double pointValue, Map map,
                                 List<Either<Test, TestGroup>> tests)
    {
        switch (type) {
        case PLAIN:
            return new PlainFile(path, pointValue, tests);
        case PYTHON:
            return new PythonFile(path, pointValue, map, tests);
        default:
            return null;
        }
    }
}
