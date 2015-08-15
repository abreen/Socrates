package io.breen.socrates.immutable.submission;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;

/**
 * Class representing a receipt file designed to store information about the submission
 * date and time of a file. This class assumes receipt files are plain text files with
 * newline-separated dates in ISO 8601 format.
 */
public final class Receipt {

    private final List<ZonedDateTime> dates;

    public Receipt(List<ZonedDateTime> dates) {
        this.dates = dates;
    }

    public String toString() {
        return "Receipt(" +
                "dates=" + dates +
                ")";
    }

    /**
     * @throws IOException
     * @throws ReceiptFormatException
     */
    public static Receipt fromReceiptFile(Path path)
            throws IOException, ReceiptFormatException
    {
        List<ZonedDateTime> list = new LinkedList<>();
        BufferedReader reader = Files.newBufferedReader(path);

        String line;
        while ((line = reader.readLine()) != null) {
            ZonedDateTime zdt;
            try {
                zdt = ZonedDateTime.parse(line, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new ReceiptFormatException(
                        "receipt " + path + " has invalid timestamp: " + line
                );
            }

            list.add(zdt);
        }

        reader.close();
        return new Receipt(list);
    }
}
