package io.breen.socrates.immutable.hooks;

public class HookTaskRuntimeException extends RuntimeException {
    public final Exception e;

    public HookTaskRuntimeException(Exception e) {
        this.e = e;
    }
}
