package io.breen.socrates.immutable.hooks;

public class HookRuntimeException extends RuntimeException {

    public final Exception e;

    public HookRuntimeException(Exception e) {
        this.e = e;
    }
}
