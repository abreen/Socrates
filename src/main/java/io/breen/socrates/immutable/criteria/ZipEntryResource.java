package io.breen.socrates.immutable.criteria;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Instances of this class are used to represent resources inside a ZIP file. An instance
 * of this class corresponds with some ZipEntry object inside of a ZipFile. This type of
 * resource is efficient, since the underlying file will not be opened unless
 * getContents() is called.
 */
public class ZipEntryResource extends Resource {

    protected final ZipFile parent;

    protected final ZipEntry entry;

    public ZipEntryResource(String fileName, ZipFile parent, ZipEntry entry) {
        super(fileName);
        this.parent = parent;
        this.entry = entry;
    }

    @Override
    public String toString() {
        return "ZipEntryResource(" +
                "fileName=" + fileName + ", " +
                "parent=" + parent + ", " +
                "entry=" + entry + ")";
    }

    @Override
    public String getContents() throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(newInputStream())
        );

        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append('\n');
        }

        reader.close();
        return builder.toString();
    }

    @Override
    public void copyTo(Path path) throws IOException {
        Files.createFile(path);
        Files.copy(newInputStream(), path);
    }

    @Override
    public void createLink(Path path) throws IOException {
        Path tempPath = Files.createTempFile(null, fileName);
        Files.copy(newInputStream(), tempPath);
        Files.createSymbolicLink(path, tempPath);
    }

    @Override
    public Path getPath() throws IOException {
        Path tempPath = Files.createTempFile(null, fileName);
        Files.copy(newInputStream(), tempPath);
        return tempPath;
    }

    private InputStream newInputStream() throws IOException {
        return parent.getInputStream(entry);
    }
}
