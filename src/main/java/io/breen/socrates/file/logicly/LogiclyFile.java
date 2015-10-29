package io.breen.socrates.file.logicly;

import io.breen.socrates.PostConstructionAction;
import io.breen.socrates.file.File;

import java.util.*;


public final class LogiclyFile extends File implements PostConstructionAction {

    /**
     * The deduction taken when the Logicly file cannot be parsed (e.g., if the XML is malformed or
     * the file cannot be decompressed).
     */
    public double loadFailureDeduction;

    /**
     * The export names of the switches on the circuit.
     */
    public List<String> switches;

    /**
     * The export names of the light bulbs on the circuit.
     */
    public List<String> lightBulbs;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public LogiclyFile() {
        language = null;
        contentsArePlainText = false;
    }

    public LogiclyFile(String path, double pointValue, Map<Date, Double> dueDates,
                       List<Object> tests)
    {
        super(path, pointValue, dueDates, tests);
    }

    @Override
    public void afterConstruction() {
        super.afterConstruction();
    }

    @Override
    public String getFileTypeName() {
        return "Logicly file";
    }
}
