package io.breen.socrates.test.logicly.parse;

import java.util.Arrays;
import java.util.UUID;


public class LightBulb extends Pin {

    public LightBulb(UUID uuid, String exportName) {
        super(uuid, exportName, 1, 1);
        outputs[0] = this;
    }

    @Override
    public String toString() {
        return "LightBulb(" +
                "exportName=" + exportName + "," +
                "inputs=" + Arrays.deepToString(inputs) +
                ")";
    }

    @Override
    public boolean[] evaluate() throws UndeterminedStateException {
        if (inputs[0] == null) throw new UndeterminedStateException();

        return inputs[0].evaluate();
    }
}
