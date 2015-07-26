package io.breen.socrates;

import java.util.*;

/**
 * Class representing a criteria file containing required parts of the
 * assignment and other data (e.g., due dates, point values, and tests).
 */
public class Criteria {

    /**
     * Human-readable assignment name (e.g., "Problem Set 1").
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
     * Due dates for this assignment. If no due dates are specified (if this is
     * null), submission times will not be checked.
     */
    private final Map<Calendar, Double> dueDates;

    private final List<File> files;

    public Criteria(Map<String, Object> map) {
        name = (String)map.get("name");
        id = (String)map.get("id");
        group = (String)map.get("group");

        Map<Date, Double> datesMap = (Map<Date, Double>)map.get("due_dates");

        dueDates = new TreeMap<Calendar, Double>();
        for (Map.Entry<Date, Double> entry : datesMap.entrySet()) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(entry.getKey());

            dueDates.put(cal, entry.getValue());
        }

        files = (List)map.get("files");
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
        return dueDates != null && dueDates.size() == 0;
    }
}