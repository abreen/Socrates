package io.breen.socrates.immutable.file;

public class InvalidFileException extends Exception {

    public InvalidFileException(FileType type, String msg) {
        super(type + ": " + msg);
    }
}
