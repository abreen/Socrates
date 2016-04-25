package io.breen.socrates.submission;

import io.breen.socrates.Globals;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;

/**
 * Class representing a receipt file designed to store information about the submission date and
 * time of a file. This class assumes receipt files are plain text files with newline-separated
 * dates in ISO 8601 format (without offsets).
 */
public final class Receipt {

    private final PriorityQueue<Date> dates;

    private Receipt(List<Date> dates) {
        this.dates = new PriorityQueue<>(6, Collections.reverseOrder());
        this.dates.addAll(dates);
    }

    /**
     * Given a path to a submitted file, this method attempts to locate and open the receipt for the file, returning
     * an instance of Receipt with the loaded dates, or null if a receipt file doesn't exist, or if it is empty.
     *
     * @throws ParseException if a receipt file is present, but it has an invalid date format
     */
    public static Receipt forFile(Path path) throws IOException, ParseException {
        if (!Files.exists(path))
            return null;

        List<Date> list = new LinkedList<>();
        BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset());

        String line;
        while ((line = reader.readLine()) != null) {
            list.add(Globals.ISO8601.parse(line));
        }

        reader.close();

        if (list.isEmpty()) {
            return null;
        } else {
            return new Receipt(list);
        }
    }

    public String toString() {
        return "Receipt(" +
                "dates=" + dates +
                ")";
    }

    public Date getLatestDate() {
        return dates.peek();
    }
}
