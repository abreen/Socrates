package io.breen.socrates.immutable.file.implementation.python;

/**
 * An exception thrown when a PythonInspector attempts to make an XML-RPC method call and the server
 * returns a Python interpreter error. Note that this is *not* the exception thrown when there is an
 * error at the XML-RPC level (e.g., if a bad method name was specified for when the parameters do
 * not match); that exception indicates an error with our code, not a student's (hopefully).
 */
public class PythonError extends Exception {

    public final String errorType;
    public final String errorMessage;

    public PythonError(String errorType, String errorMessage) {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }
}
