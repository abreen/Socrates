package io.breen.socrates.test;


import io.breen.socrates.util.Freezable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;


public abstract class Node extends Freezable {

    private List<Node> children;

    public Node() {
        children = new ArrayList<>();
    }

    /**
     * Freezes this node and the entire subtree below this node, recursively.
     */
    public void freezeAll() {
        freeze();
        getChildren().forEach(Node::freeze);
    }

    public List<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Used by SnakeYAML.
     */
    public void setChildren(List<Node> children) {
        checkFrozen();
        this.children = children;
    }

    public void addChild(Node child) {
        checkFrozen();
        children.add(child);
    }

    public void addAllChildren(List<Node> children) {
        checkFrozen();
        children.addAll(children);
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void forEach(Consumer<Node> f) {
        f.accept(this);

        for (Node child : getChildren())
            child.forEach(f);
    }
}
