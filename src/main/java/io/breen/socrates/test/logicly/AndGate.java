package io.breen.socrates.test.logicly;

import java.util.Arrays;
import java.util.UUID;


public class AndGate extends Evaluatable {

    public AndGate(UUID uuid, int numInputs) {
        super(uuid, numInputs, 1);
        this.outputs[0] = this;
    }

    @Override
    public boolean[] evaluate() throws UndeterminedStateException {
        boolean foundInput = false;

        for (Evaluatable e : this.inputs) {
            if (e != null) {
                foundInput = true;

                if (!e.evaluate()[0]) return new boolean[] {false};
            }
        }

        if (!foundInput) throw new UndeterminedStateException();

        return new boolean[] {true};
    }

    @Override
    public String toString() {
        return "AndGate(inputs=" + Arrays.deepToString(inputs) + ")";
    }
}
