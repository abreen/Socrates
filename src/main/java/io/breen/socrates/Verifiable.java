package io.breen.socrates;

/**
 * An interface that specifies a general method that can be called to check that it was constructed
 * with the appropriate values.
 *
 * This interface is used mostly by classes being constructed by SnakeYAML.
 */
public interface Verifiable {

    void verify() throws IllegalArgumentException;
}
