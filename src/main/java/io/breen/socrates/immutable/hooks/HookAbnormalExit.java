package io.breen.socrates.immutable.hooks;

import io.breen.socrates.immutable.criteria.Resource;

public class HookAbnormalExit extends Exception {

    public HookAbnormalExit(Resource script, int exitCode) {
        super("script " + script + " exited with code " + exitCode);
    }
}
