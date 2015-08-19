package io.breen.socrates.immutable.criteria;

import io.breen.socrates.constructor.InvalidCriteriaException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CriteriaTest {

    private Path[] createdFiles;

    private enum InputFile {
        NONEXISTENT(Paths.get("nonexistent.cf")),
        EMPTY_FILE(Paths.get("empty.cf")),
        INVALID_TOP(Paths.get("invalid_top.cf"));

        Path p;

        InputFile(Path p) {
            this.p = p;
        }
    }

    @Before
    public void setUp() throws Exception {
        {
            // nonexistent: don't create the file
        }

        {
            // empty: create an empty file
            Files.createFile(InputFile.EMPTY_FILE.p);
        }

        {
            // invalid top-level
            Files.createFile(InputFile.INVALID_TOP.p);
            Files.newBufferedWriter(InputFile.INVALID_TOP.p).append("1234").close();
        }
    }

    @After
    public void tearDown() throws Exception {
        for (InputFile inputFile : InputFile.values())
            if (inputFile != inputFile.NONEXISTENT)
                Files.delete(inputFile.p);
    }

    @Test(expected = InvalidCriteriaException.class)
    public void emptyFileShouldThrowException() throws Exception {
        Criteria.loadFromYAML(InputFile.EMPTY_FILE.p);
    }

    @Test(expected = InvalidCriteriaException.class)
    public void invalidTopLevelShouldThrowException() throws Exception {
        Criteria.loadFromYAML(InputFile.INVALID_TOP.p);
    }
}