package io.breen.socrates.immutable.test.implementation.logicly;


import java.util.Arrays;
import java.util.UUID;

public class NotGate extends Evaluatable {

    public NotGate(UUID uuid) {
        super(uuid, 1, 1);
        this.outputs[0] = this;
    }

    @Override
    public boolean[] evaluate() throws UndeterminedStateException {
        if (this.inputs[0] == null) throw new UndeterminedStateException();

        return new boolean[] {!this.inputs[0].evaluate()[0]};
    }

    @Override
    public String toString() {
        return "NotGate(inputs=" + Arrays.deepToString(inputs) + ")";
    }
}
