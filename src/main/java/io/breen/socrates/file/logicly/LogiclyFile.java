package io.breen.socrates.file.logicly;

import io.breen.socrates.file.File;
import io.breen.socrates.test.Node;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class LogiclyFile extends File {

    /**
     * The deduction taken when the Logicly file cannot be parsed (e.g., if the XML is malformed or
     * the file cannot be decompressed).
     */
    private double loadFailureDeduction;

    /**
     * The export names of the switches on the circuit.
     */
    private List<String> switches;

    /**
     * The export names of the light bulbs on the circuit.
     */
    private List<String> lightBulbs;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public LogiclyFile() {
    }

    public LogiclyFile(String path, double pointValue, Map<Date, Double> dueDates, List<Node> tests) {
        super(path, pointValue, dueDates, tests);
    }

    public List<String> getLightBulbs() {
        return Collections.unmodifiableList(lightBulbs);
    }

    public void setLightBulbs(List<String> lightBulbs) {
        checkFrozen();
        this.lightBulbs = lightBulbs;
    }

    public double getLoadFailureDeduction() {
        return loadFailureDeduction;
    }

    public void setLoadFailureDeduction(double loadFailureDeduction) {
        checkFrozen();
        this.loadFailureDeduction = loadFailureDeduction;
    }

    public List<String> getSwitches() {
        return Collections.unmodifiableList(switches);
    }

    public void setSwitches(List<String> switches) {
        checkFrozen();
        this.switches = switches;
    }
}
