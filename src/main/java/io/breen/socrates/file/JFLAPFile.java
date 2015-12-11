package io.breen.socrates.file;

/**
 * A JFLAPFile is a representation of a JFLAP input file. Instances of this class do not support
 * automation and their contents will not be shown in the file view (see the constructor).
 */
public class JFLAPFile extends File {

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public JFLAPFile() {
        contentsArePlainText = false;
    }

    @Override
    public String getFileTypeName() {
        return "JFLAP file";
    }
}
