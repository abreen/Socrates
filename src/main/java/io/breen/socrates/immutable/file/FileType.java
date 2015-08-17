package io.breen.socrates.immutable.file;

public enum FileType {

    /**
     * @see PlainFile
     */
    PLAIN("plain"),

    PYTHON("python"),

    JAVA("java"),

    PICOBOT("picobot"),

    HMMM("hmmm");

    private String type;

    FileType(String type) {
        this.type = type;
    }

    public String toString() {
        return type;
    }

    public static FileType fromID(String type) {
        for (FileType t : FileType.values())
            if (t.type.equals(type))
                return t;

        return null;
    }
}
