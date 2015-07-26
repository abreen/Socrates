package io.breen.socrates;

import org.yaml.snakeyaml.nodes.Node;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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