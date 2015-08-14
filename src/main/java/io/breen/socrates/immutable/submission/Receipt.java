package io.breen.socrates.immutable.submission;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

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

    /**
     * @throws java.io.FileNotFoundException
     * @throws DateTimeParseException If any line in the file is not in ISO 8601 format
     */
    public static Receipt fromReceiptFile(java.io.File f)
            throws java.io.FileNotFoundException, DateTimeParseException
    {
        List<ZonedDateTime> list = new LinkedList<>();

        Scanner s = new Scanner(f);
        while (s.hasNextLine()) {
            ZonedDateTime zdt = ZonedDateTime.parse(
                    s.nextLine(), DateTimeFormatter.ISO_OFFSET_DATE_TIME
            );
            list.add(zdt);
        }
        s.close();

        return new Receipt(list);
    }
}
