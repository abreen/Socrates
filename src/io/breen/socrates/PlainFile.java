package io.breen.socrates;

import org.yaml.snakeyaml.nodes.Node;

import java.util.ArrayList;
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
