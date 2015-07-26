package io.breen.socrates.criteria;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DueDate implements Comparable<DueDate> {

    private static final DateTimeFormatter format =
        DateTimeFormatter.ofPattern("MMMM d, YYY h:mm:ss a");

    private ZonedDateTime dateTime;

    public DueDate(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String toString() {
        return dateTime.format(format);
    }

    public int compareTo(DueDate other) {
        return this.dateTime.compareTo(other.dateTime);
    }
}
