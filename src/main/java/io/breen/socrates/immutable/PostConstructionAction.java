package io.breen.socrates.immutable;

/**
 * An interface that specifies a method that must be called when an object is considered to be
 * constructed.
 *
 * This interface is used mostly by classes being constructed by SnakeYAML. It allows SnakeYAML to
 * call the default constructor of an object and assign to fields at any time, but still allow us to
 * have custom construction code run after this occurs.
 */
public interface PostConstructionAction {

    void afterConstruction();
}
