package io.breen.socrates.immutable.test.python;

/**
 * An exception thrown when a PythonInspector gets an error response, which indicates an
 * error or exception was thrown by the Python interpreter when running the student's code.
 */
public class PythonError extends Exception {

    public final String errorType;
    public final String errorMessage;

    public PythonError(String errorType, String errorMessage) {
        super("Python error: " + errorType + ": " + errorMessage);
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }
}
