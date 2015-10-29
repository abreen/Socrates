package io.breen.socrates.immutable.test.any.script;

/**
 * Thrown when a script test executes successfully, but the test did not properly report the results
 * of the test.
 */
public class ScriptTestInvalidResult extends Exception {

    public ScriptTestInvalidResult(ScriptTest test, String result) {
        super("ScriptTest " + test + " exited with invalid result: " + result);
    }
}
