package io.breen.socrates.test.logicly.parse;


import java.util.UUID;

public abstract class Evaluatable extends LogiclyObject {

    public final Evaluatable inputs[];
    public final Evaluatable outputs[];

    public Evaluatable(UUID uuid, int numInputs, int numOutputs) {
        super(uuid);
        inputs = new Evaluatable[numInputs];
        outputs = new Evaluatable[numOutputs];
    }

    /**
     * @return an array of Boolean values describing the output of this Evaluatable, precisely the
     * same size as this object's outputs array
     *
     * @throws UndeterminedStateException
     */
    public abstract boolean[] evaluate() throws UndeterminedStateException;
}
