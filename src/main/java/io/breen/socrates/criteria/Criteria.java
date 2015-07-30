package io.breen.socrates.criteria;

import io.breen.socrates.file.File;

import java.util.*;
import java.util.logging.Logger;

/**
 * An immutable class representing a criteria file containing required parts of the
 * assignment and other data (e.g., due dates, point values, and tests).
 */
public class Criteria {

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
    private Map<DueDate, Double> dueDates;

    private List<File> files;

    public Criteria(String name, String id, String group,
                    Map<DueDate, Double> dueDates,
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
        return "Criteria(" +
                "name=" + name + ", " +
                "id=" + id + ", " +
                "group=" + group + ", " +
                "dueDates=" + dueDates + ", " +
                "files=" + files +
                ")";
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

    private static Logger logger = Logger.getLogger(Criteria.class.getName());
}
