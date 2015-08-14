package io.breen.socrates.immutable.test;

import io.breen.socrates.immutable.file.FileType;

public enum TestType {

    REVIEW_PLAIN(FileType.PLAIN, "review"),
    REVIEW_PYTHON(FileType.PYTHON, "review");

    private FileType forFileType;
    private String type;

    TestType(FileType forFileType, String testType) {
        this.forFileType = forFileType;
        this.type = testType;
    }

    public static TestType fromTypeAndID(FileType type, String testType) {
        for (TestType t : TestType.values())
            if (t.forFileType == type && t.type.equals(testType))
                return t;

        return null;
    }
}
