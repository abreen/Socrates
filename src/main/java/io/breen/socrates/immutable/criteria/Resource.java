package io.breen.socrates.immutable.criteria;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A class representing any resource included in a criteria package. A resource is a
 * file that is required by a test in order to function. For example, a test may need
 * to open a plain text file to perform a "diff" comparison between the student's file
 * and the "solution" file. In this case, the file is included in the criteria package
 * under the "static" directory.
 */
public abstract class Resource {

    protected final String fileName;

    public Resource(String fileName) {
        this.fileName = fileName;
    }

    public abstract String getContents() throws IOException;

    public abstract void copyTo(Path path) throws IOException;

    public abstract void createLink(Path path) throws IOException;
}
