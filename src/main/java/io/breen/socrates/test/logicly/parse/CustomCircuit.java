package io.breen.socrates.test.logicly.parse;


import java.util.Arrays;
import java.util.UUID;

public class CustomCircuit extends Evaluatable {

    public final String name;

    public CustomCircuit(UUID uuid, String name, Switch[] switches, LightBulb[] lightBulbs) {
        super(uuid, switches.length, lightBulbs.length);
        this.name = name;
        System.arraycopy(switches, 0, inputs, 0, switches.length);
        System.arraycopy(lightBulbs, 0, outputs, 0, lightBulbs.length);
    }

    @Override
    public boolean[] evaluate() throws UndeterminedStateException {
        boolean[] vals = new boolean[outputs.length];

        for (int i = 0; i < outputs.length; i++)
            if (inputs[i] == null) throw new UndeterminedStateException();
            else vals[i] = inputs[i].evaluate()[0];

        return vals;
    }

    @Override
    public String toString() {
        return "CustomCircuit(name=" + name + ", " +
                "inputs=" + Arrays.deepToString(inputs) + ", " +
                "outputs=" + Arrays.deepToString(outputs) + ")";
    }
}
