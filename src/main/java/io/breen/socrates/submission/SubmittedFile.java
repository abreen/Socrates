package io.breen.socrates.submission;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Class representing immutable objects storing information about a single file found on the file
 * system that represents one part of a student's submission.
 */
public class SubmittedFile {

    private static Logger logger = Logger.getLogger(SubmittedFile.class.getName());

    /**
     * This file's location on the file system.
     */
    public final Path fullPath;

    /**
     * This file's location relative to the submission directory. This path should match the path
     * specified in a criteria file, if this SubmittedFile is indeed relevant to grading.
     */
    public final Path localPath;

    /**
     * This file's size in bytes.
     */
    public final long size;

    /**
     * This file's receipt, storing the submission timestamps. If there was no receipt for this
     * file, this is null.
     */
    public final Receipt receipt;

    public SubmittedFile(Path fullPath, Path localPath) throws IOException {
        this.fullPath = fullPath;
        this.localPath = localPath;
        this.size = Files.size(fullPath);
        this.receipt = null;
    }

    public SubmittedFile(Path fullPath, Path localPath, Path receipt)
            throws IOException, ReceiptFormatException
    {
        this.fullPath = fullPath;
        this.localPath = localPath;
        this.size = Files.size(fullPath);

        Receipt r = null;
        if (receipt != null) {
            r = Receipt.fromReceiptFile(receipt);
        }

        this.receipt = r;
    }

    public String toString() {
        return "SubmittedFile(" +
                "path=" + localPath + ", " +
                "receipt=" + receipt +
                ")";
    }

    public String getContentsMixedUTF8() throws IOException {
        FileInputStream inStream = new FileInputStream(fullPath.toFile());
        StringBuilder builder = new StringBuilder();

        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        byte[] buffer = new byte[1];
        ByteBuffer wrapped = ByteBuffer.wrap(buffer);

        while (true) {
            if (inStream.read(buffer) == -1) break;
            wrapped.rewind();

            try {
                CharBuffer cbuf = decoder.decode(wrapped);
                builder.append(cbuf.charAt(0));
            } catch (MalformedInputException | UnmappableCharacterException x) {
                builder.append("�");
            }
        }

        return builder.toString();
    }

    public String getContentsUTF8() throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = Files.newBufferedReader(fullPath, StandardCharsets.UTF_8);

        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append('\n');
        }

        return builder.toString();
    }

    public String getContents() throws IOException {
        try {
            return getContentsUTF8();
        } catch (IOException ignored) {}

        return getContentsMixedUTF8();
    }
}
