package io.breen.socrates.immutable.file;

import io.breen.socrates.constructor.SocratesConstructor;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.util.Either;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FileFactory {
    public static File buildFile(FileType type,
                                 Map map,
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
                    throw new InvalidFileException(type, "'due_dates' value is not a double");
                }

                Instant i = date.toInstant();
                LocalDateTime ldt = LocalDateTime.ofInstant(i, ZoneOffset.UTC);
                dueDates.put(ldt, deduction);
            }
        }

        String path;
        try {
            path = (String)map.get("path");
            if (path == null) throw new NullPointerException();
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
            throw new InvalidFileException(type, "'point_value' field: " + e.getMessage());
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
