package io.breen.socrates.immutable.test;

public class InvalidTestException extends Exception {

    public InvalidTestException(TestType type, String msg) {
        super(type + ": " + msg);
    }
}
