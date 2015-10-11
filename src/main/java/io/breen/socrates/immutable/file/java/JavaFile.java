package io.breen.socrates.immutable.file.java;

import io.breen.socrates.immutable.PostConstructionAction;
import io.breen.socrates.immutable.file.File;

import java.util.*;

/**
 * A JavaFile is a representation of a Java file containing source code. The file should contain at
 * least one class.
 */
public final class JavaFile extends File implements PostConstructionAction {

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public JavaFile() {
        language = "java";
        contentsArePlainText = true;
    }

    public JavaFile(String path, double pointValue, Map<Date, Double> dueDates, List<Object> tests)
    {
        super(path, pointValue, dueDates, tests);
    }

    @Override
    public void afterConstruction() {
        super.afterConstruction();
    }

    @Override
    public String getFileTypeName() {
        return "Java source code";
    }
}
