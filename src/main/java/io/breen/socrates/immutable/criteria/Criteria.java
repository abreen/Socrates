package io.breen.socrates.immutable.criteria;

import io.breen.socrates.constructor.SocratesConstructor;
import io.breen.socrates.immutable.file.File;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * An immutable class representing a criteria file containing required parts of the
 * assignment and other data (e.g., due dates, point values, and tests).
 */
public final class Criteria {

    /**
     * Human-readable assignment name (e.g., "Problem Set 1"). Cannot be null.
     */
    private final String assignmentName;

    private List<File> files;

    public Criteria(String name, List<File> files) {
        this.assignmentName = name;
        this.files = files;

        logger.fine("constructed a criteria object: " + this);
    }

    public String toString() {
        return "Criteria\n" +
                "\tassignment_name=" + assignmentName + "\n" +
                "\tfiles=" + files.stream()
                                  .map(f -> f.toString())
                                  .collect(Collectors.joining("\n"));
    }

    /**
     * @throws io.breen.socrates.constructor.InvalidCriteriaException
     * @throws IOException
     */
    public static Criteria loadFromYAML(Path path) throws IOException {
        Yaml y = new Yaml(new SocratesConstructor());
        return (Criteria)y.load(Files.newBufferedReader(path));
    }

    private static Logger logger = Logger.getLogger(Criteria.class.getName());
}
