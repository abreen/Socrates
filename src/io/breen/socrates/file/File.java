package io.breen.socrates.file;

import io.breen.socrates.test.TestGroup;

import java.util.List;

public abstract class File {

    protected final String path;

    protected final double pointValue;

    public File(String path, double pointValue) {
        this.path = path;
        this.pointValue = pointValue;
    }

    public abstract List<TestGroup> getTestGroups();
}