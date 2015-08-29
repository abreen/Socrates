package io.breen.socrates.immutable.submission;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Class representing a receipt file designed to store information about the submission date and
 * time of a file. This class assumes receipt files are plain text files with newline-separated
 * dates in ISO 8601 format (without offsets). For the precise expected format, see
 * DateTimeFormatter.ISO_LOCAL_DATE_TIME.
 *
 * @see DateTimeFormatter
 */
public final class Receipt {

    private final PriorityQueue<LocalDateTime> dates;

    public Receipt(List<LocalDateTime> dates) {
        this.dates = new PriorityQueue<>(Collections.reverseOrder());
        this.dates.addAll(dates);
    }

    /**
     * @throws IOException
     * @throws ReceiptFormatException
     */
    public static Receipt fromReceiptFile(Path path) throws IOException, ReceiptFormatException
    {
        List<LocalDateTime> list = new LinkedList<>();
        BufferedReader reader = Files.newBufferedReader(path);

        String line;
        while ((line = reader.readLine()) != null) {
            LocalDateTime ldt;
            try {
                ldt = LocalDateTime.parse(line, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new ReceiptFormatException(
                        "receipt " + path + " has invalid timestamp: " + line
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

    public LocalDateTime getLatestDate() {
        return dates.peek();
    }
}
