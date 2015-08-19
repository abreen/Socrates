package io.breen.socrates.immutable.file;

/**
 * An enumeration used to provide a lightweight representation of all known subclasses
 * of File. Each enumeration value contains the string used in criteria files to refer
 * to particular file types. This enumeration is used by SocratesConstructor, since it
 * must map strings from the criteria file to an actual class in the source code to
 * instantiate. Then FileFactory does the instantiation.
 *
 * @see io.breen.socrates.constructor.SocratesConstructor
 * @see io.breen.socrates.immutable.file.FileFactory
 */
public enum FileType {

    /**
     * @see PlainFile
     */
    PLAIN("plain"),

    /**
     * @see PythonFile
     */
    PYTHON("python"),

    JAVA("java"),

    PICOBOT("picobot"),

    HMMM("hmmm");

    public final String type;

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
