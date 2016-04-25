package io.breen.socrates.test.logicly.parse;

import java.util.UUID;


public abstract class Pin extends Evaluatable {

    public final String exportName;

    public Pin(UUID uuid, String exportName, int numInputs, int numOutputs) {
        super(uuid, numInputs, numOutputs);
        this.exportName = exportName;
    }
}
