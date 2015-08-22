package io.breen.socrates.immutable.hooks;

public class HookTaskAbnormalExit extends Exception {
    public HookTaskAbnormalExit(HookTask task, int exitCode) {
        super("HookTask " + task + " exited with code " + exitCode);
    }
}
