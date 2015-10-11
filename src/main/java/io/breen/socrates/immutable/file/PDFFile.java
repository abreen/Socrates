package io.breen.socrates.immutable.file;

/**
 * A PDFFile is a representation of a PDF file. Instances of this class do not support automation;
 * they simply support expecting PDF files with certain names, and specify that Socrates should not
 * attempt to display their contents in the FileView.
 */
public class PDFFile extends File {

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public PDFFile() {
        contentsArePlainText = false;
    }

    @Override
    public String getFileTypeName() {
        return "PDF file";
    }
}
