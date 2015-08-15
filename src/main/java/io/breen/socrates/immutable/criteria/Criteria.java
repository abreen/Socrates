package io.breen.socrates.immutable.criteria;

import io.breen.socrates.constructor.SocratesConstructor;
import io.breen.socrates.immutable.file.File;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final String name;

    /**
     * A short string used to refer to the assignment uniquely, usually used
     * as the criteria file's name on the file system (e.g., "ps1" or "hw1").
     */
    private final String id;

    /**
     * The assignment's group, if this criteria file specifies it.
     */
    private final String group;

    /**
     * Due dates for this assignment. May be null if the criteria file does not
     * specify any due dates.
     */
    private Map<LocalDateTime, Double> dueDates;

    private List<File> files;

    public Criteria(String name, String id, String group,
                    Map<LocalDateTime, Double> dueDates,
                    List<File> files)
    {
        this.name = name;
        this.id = id;
        this.group = group;
        this.dueDates = dueDates;
        this.files = files;

        logger.fine("constructed a criteria object: " + this);
    }

    public String toString() {
        return "Criteria\n" +
                "\tname=" + name + "\n" +
                "\tid=" + id + "\n" +
                "\tgroup=" + group + "\n" +
                "\tdueDates=" + dueDates + "\n" +
                "\tfiles=" + files.stream()
                                  .map(f -> f.toString())
                                  .collect(Collectors.joining("\n"));
    }

    public boolean hasDueDates() {
        return dueDates != null;
    }

    public boolean hasGroup() {
        return group != null;
    }

    public String getName() {
        return name;
    }

    public String getID() {
        return id;
    }

    public String getGroup() {
        return group;
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
