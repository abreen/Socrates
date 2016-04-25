package io.breen.socrates.util;

/**
 * The exception that is thrown when an object is frozen, but a setter method is called on the object.
 */
public class FrozenObjectModificationException extends RuntimeException {}
