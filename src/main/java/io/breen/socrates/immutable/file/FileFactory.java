package io.breen.socrates.immutable.file;

import io.breen.socrates.constructor.SocratesConstructor;
import io.breen.socrates.immutable.file.implementation.PlainFile;
import io.breen.socrates.immutable.file.implementation.PythonFile;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.util.Either;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This factory's single static method takes a FileType, a map of key-value pairs from a
 * criteria file, and a list of tests (previously created by TestFactory). The list of
 * tests is actually a list of tests or TestGroups (but a TestGroup is simply a container
 * for more tests, or more TestGroups, recursively).
 *
 * @see io.breen.socrates.immutable.test.Test
 * @see io.breen.socrates.immutable.test.TestGroup
 */
public class FileFactory {

    /**
     * @throws InvalidFileException If the map passed in does not contain valid data
     * enough to create an instance of the desired FileType
     */
    public static File buildFile(FileType type, Map map,
                                 List<Either<Test, TestGroup>> tests)
            throws InvalidFileException
    {
        /*
         * Parse due dates for this file, if they exist.
         */
        Map<Date, Double> datesMap = (Map<Date, Double>)map.get("due_dates");

        Map<LocalDateTime, Double> dueDates = null;
        if (datesMap != null) {
            dueDates = new TreeMap<>();

            for (Map.Entry<Date, Double> entry : datesMap.entrySet()) {
                Date date;
                try {
                    date = entry.getKey();
                } catch (ClassCastException e) {
                    throw new InvalidFileException(type, "'due_dates' key is not a date");
                }

                Double deduction;
                try {
                    deduction = SocratesConstructor.coerceToDouble(entry.getValue());
                } catch (ClassCastException e) {
                    throw new InvalidFileException(
                            type,
                            "'due_dates' value is not a double"
                    );
                }

                Instant i = date.toInstant();
                LocalDateTime ldt = LocalDateTime.ofInstant(i, ZoneOffset.UTC);
                dueDates.put(ldt, deduction);
            }
        }

        String pathStr;
        Path path;
        try {
            pathStr = (String)map.get("path");
            if (pathStr == null) throw new NullPointerException();
            path = Paths.get(pathStr);
        } catch (ClassCastException e) {
            throw new InvalidFileException(type, "'path' field: " + e.getMessage());
        } catch (NullPointerException e) {
            throw new InvalidFileException(type, "missing 'path' field");
        }

        Double pointValue;
        try {
            pointValue = SocratesConstructor.coerceToDouble(map.get("point_value"));
            if (pointValue == null) throw new NullPointerException();
        } catch (ClassCastException e) {
            throw new InvalidFileException(
                    type,
                    "'point_value' field: " + e.getMessage()
            );
        } catch (NullPointerException e) {
            throw new InvalidFileException(type, "missing 'point_value' field");
        }

        switch (type) {
        case PLAIN:
            return new PlainFile(path, pointValue, dueDates, tests);
        case PYTHON:
            return new PythonFile(path, pointValue, dueDates, tests);
        default:
            return null;
        }
    }
}
