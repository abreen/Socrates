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

    private String id;

    FileType(String id) {
        this.id = id;
    }

    public static FileType fromID(String id) {
        for (FileType t : FileType.values())
            if (t.id.equals(id))
                return t;

        return null;
    }
}
