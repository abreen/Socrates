package io.breen.socrates.file;

import io.breen.socrates.immutable.test.TestGroup;

import java.util.List;

public class PlainFile extends File {

    protected List<TestGroup> testGroups;

    public PlainFile(String path, double pointValue, List<TestGroup> testGroups) {
        super(path, pointValue);
        this.testGroups = testGroups;
    }

    public List<TestGroup> getTestGroups() {
        return testGroups;
    }
}
