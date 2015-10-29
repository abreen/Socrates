package io.breen.socrates.immutable.test.logicly;


import java.util.UUID;

public class LowConstant extends Evaluatable {

    public LowConstant(UUID uuid) {
        super(uuid, 0, 1);
        this.outputs[0] = this;
    }

    @Override
    public boolean[] evaluate() throws UndeterminedStateException {
        return new boolean[] {false};
    }
}
