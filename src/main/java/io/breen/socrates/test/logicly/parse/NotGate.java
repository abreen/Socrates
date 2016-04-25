package io.breen.socrates.test.logicly.parse;


import java.util.Arrays;
import java.util.UUID;

public class NotGate extends Evaluatable {

    public NotGate(UUID uuid) {
        super(uuid, 1, 1);
        outputs[0] = this;
    }

    @Override
    public boolean[] evaluate() throws UndeterminedStateException {
        if (inputs[0] == null) throw new UndeterminedStateException();

        return new boolean[] {!inputs[0].evaluate()[0]};
    }

    @Override
    public String toString() {
        return "NotGate(inputs=" + Arrays.deepToString(inputs) + ")";
    }
}
