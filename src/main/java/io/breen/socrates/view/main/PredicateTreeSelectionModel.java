package io.breen.socrates.view.main;

import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import java.util.Arrays;

/**
 * A simple TreeSelectionModel based on DefaultTreeSelectionModel that only allows selection of a
 * TreePath if the path satisfies a property given by a predicate.
 */
public abstract class PredicateTreeSelectionModel extends DefaultTreeSelectionModel {

    public abstract boolean predicate(TreePath path);

    @Override
    public void setSelectionPath(TreePath path) {
        if (predicate(path)) super.setSelectionPath(path);
    }

    @Override
    public void setSelectionPaths(TreePath[] paths) {
        TreePath[] filteredPaths = Arrays.copyOf(paths, paths.length);
        for (int i = 0; i < filteredPaths.length; i++)
            if (!predicate(filteredPaths[i])) filteredPaths[i] = null;

        super.setSelectionPaths(filteredPaths);
    }

    @Override
    public void addSelectionPath(TreePath path) {
        if (predicate(path)) super.addSelectionPath(path);
    }

    @Override
    public void addSelectionPaths(TreePath[] paths) {
        TreePath[] filteredPaths = Arrays.copyOf(paths, paths.length);
        for (int i = 0; i < filteredPaths.length; i++)
            if (!predicate(filteredPaths[i])) filteredPaths[i] = null;

        super.addSelectionPaths(filteredPaths);
    }
}
