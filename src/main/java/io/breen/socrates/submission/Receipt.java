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

    public Receipt(List<Date> dates) {
        this.dates = new PriorityQueue<>(6, Collections.reverseOrder());
        this.dates.addAll(dates);
    }

    /**
     * @throws IOException
     * @throws ReceiptFormatException
     */
    public static Receipt fromReceiptFile(Path path) throws IOException, ReceiptFormatException
    {
        List<Date> list = new LinkedList<>();
        BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset());

        String line;
        while ((line = reader.readLine()) != null) {
            Date ldt;
            try {
                ldt = Globals.ISO8601.parse(line);
            } catch (ParseException e) {
                throw new ReceiptFormatException(
                        "receipt has invalid timestamp: " + line
                );
            }

            list.add(ldt);
        }

        if (list.isEmpty()) throw new ReceiptFormatException("receipt file is empty");

        reader.close();
        return new Receipt(list);
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
