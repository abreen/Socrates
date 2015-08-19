package io.breen.socrates.immutable.test;

import io.breen.socrates.immutable.file.FileType;

/**
 * An enumeration used to provide a lightweight representation of all known subclasses
 * of Test. Each enumeration value contains the string used in criteria files to refer
 * to a test type, and type of the file under which a test is specified. (This is
 * necessary because we allow different file types to specify a test with the same type.)
 *
 * This enumeration is used by SocratesConstructor, since it must map strings from the
 * criteria file to an actual class in the source code to instantiate.
 * Then TestFactory does the instantiation.
 *
 * @see io.breen.socrates.constructor.SocratesConstructor
 * @see io.breen.socrates.immutable.test.TestFactory
 */
public enum TestType {

    ALWAYS_PASSING_PLAIN(FileType.PLAIN, "alwayspassing"),
    REVIEW_PLAIN(FileType.PLAIN, "review"),

    REVIEW_PYTHON(FileType.PYTHON, "review");

    public final FileType forFileType;
    public final String type;

    TestType(FileType forFileType, String testType) {
        this.forFileType = forFileType;
        this.type = testType;
    }

    @Override
    public String toString() {
        return type + " for " + forFileType;
    }

    public static TestType fromTypeAndID(FileType type, String testType) {
        for (TestType t : TestType.values())
            if (t.forFileType == type && t.type.equals(testType))
                return t;

        return null;
    }
}
