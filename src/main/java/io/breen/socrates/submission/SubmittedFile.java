package io.breen.socrates.submission;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.logging.Logger;


public class SubmittedFile {

    /**
     * The absolute path to the file on the file system.
     */
    private Path path;

    /**
     * This file's receipt, storing the submission timestamps. If there was no receipt for this
     * file, this is null.
     */
    private Receipt receipt;


    public SubmittedFile(Path path, boolean ignoreReceipt) throws IOException, ParseException {
        this.path = path.toAbsolutePath();

        if (!ignoreReceipt)
            receipt = Receipt.forFile(this.path);
    }

    public SubmittedFile(Path path) throws IOException, ParseException {
        this(path, false);
    }

    public String toString() {
        return "SubmittedFile(" +
                "path=" + path + ", " +
                "receipt=" + receipt +
                ")";
    }

    public String getContentsMixedUTF8() throws IOException {
        FileInputStream inStream = new FileInputStream(path.toFile());
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
        BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);

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

    public Path getPath() {
        return path;
    }

    public Receipt getReceipt() {
        return receipt;
    }
}
