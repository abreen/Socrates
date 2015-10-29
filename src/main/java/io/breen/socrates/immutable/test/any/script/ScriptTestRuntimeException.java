package io.breen.socrates.immutable.test.any.script;

/**
 * When a ScriptTest's shouldPass method throws this exception, a "fatal" error occurred running the
 * Python script referenced by the ScriptTest. This error could have been caused by any exception
 * that occurs at runtime: for example, if the script exits abnormally, or if there was an I/O
 * error.
 */
public class ScriptTestRuntimeException extends RuntimeException {

    public final Exception e;

    public ScriptTestRuntimeException(Exception e) {
        this.e = e;
    }
}
