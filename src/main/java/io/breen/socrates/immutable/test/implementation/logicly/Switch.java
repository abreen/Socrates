package io.breen.socrates.immutable.test.implementation.logicly;

import java.util.Arrays;
import java.util.UUID;


public class Switch extends Pin {

    public boolean state;

    public Switch(UUID uuid, String exportName) {
        super(uuid, exportName, 1, 1);
        this.inputs[0] = null;              // only set if this switch is part of a subcircuit
        this.outputs[0] = this;
    }

    @Override
    public String toString() {
        return "Switch(exportName=" + exportName + ", " +
                "inputs=" + Arrays.deepToString(inputs) + ")";
    }

    @Override
    public boolean[] evaluate() throws UndeterminedStateException {
        if (this.inputs[0] != null) {
            return this.inputs[0].evaluate();
        } else {
            return new boolean[] {this.state};
        }
    }
}
