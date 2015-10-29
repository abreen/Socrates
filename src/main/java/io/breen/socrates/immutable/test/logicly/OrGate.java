package io.breen.socrates.immutable.test.logicly;

import java.util.Arrays;
import java.util.UUID;


public class OrGate extends Evaluatable {

    public OrGate(UUID uuid, int numInputs) {
        super(uuid, numInputs, 1);
        this.outputs[0] = this;
    }

    @Override
    public boolean[] evaluate() throws UndeterminedStateException {
        boolean foundInput = false;
        UndeterminedStateException x = null;

        /*
         * Matching Logicly's behavior, we allow this OR gate to output 1 even if
         * there are undetermined inputs, as long as at least one input is determined
         * and 1.
         */

        for (Evaluatable e : this.inputs) {
            if (e != null) {
                foundInput = true;

                try {
                    if (e.evaluate()[0]) return new boolean[] {true};
                } catch (UndeterminedStateException exc) {
                    x = exc;
                }

            }
        }

        if (!foundInput) throw new UndeterminedStateException();

        if (x != null) {
            /*
             * If we got here, no inputs were determined, or at least one input was undetermined
             * and all of the other inputs were 0.
             */
            throw new UndeterminedStateException();
        }

        return new boolean[] {false};
    }

    @Override
    public String toString() {
        return "OrGate(inputs=" + Arrays.deepToString(inputs) + ")";
    }
}
