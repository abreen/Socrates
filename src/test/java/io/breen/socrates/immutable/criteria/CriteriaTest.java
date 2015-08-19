package io.breen.socrates.immutable.criteria;

import io.breen.socrates.constructor.InvalidCriteriaException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class CriteriaTest {

    private Path[] createdFiles;

    private enum InputFile {
        NONEXISTENT(Paths.get("nonexistent.cf")),
        EMPTY_FILE(Paths.get("empty.cf")),
        INVALID_TOP(Paths.get("invalid_top.cf")),
        BASIC(Paths.get("basic.cf")),
        BASIC_UNRECOGNIZED(Paths.get("basic_unrecognized.cf")),
        FILE_NONMAPPING(Paths.get("file_nonmapping.cf"));

        Path p;

        InputFile(Path p) {
            this.p = p;
        }
    }

    private String TEST_ASSIGNMENT_NAME = "Test Assignment";

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

        {
            // basic working criteria file
            Files.createFile(InputFile.BASIC.p);
            BufferedWriter w = Files.newBufferedWriter(InputFile.BASIC.p);
            w.append("assignment_name: " + TEST_ASSIGNMENT_NAME + "\n");
            w.append("files: []");
            w.close();
        }

        {
            // basic criteria file, but with an unrecognized top-level mapping
            Files.createFile(InputFile.BASIC_UNRECOGNIZED.p);
            BufferedWriter w = Files.newBufferedWriter(InputFile.BASIC_UNRECOGNIZED.p);
            w.append("assignment_name: " + TEST_ASSIGNMENT_NAME + "\n");
            w.append("foo: bar\n");
            w.append("files: []");
            w.close();
        }

        {
            // basic criteria file, but with a file specification that is not a mapping
            Files.createFile(InputFile.FILE_NONMAPPING.p);
            BufferedWriter w = Files.newBufferedWriter(InputFile.FILE_NONMAPPING.p);
            w.append("assignment_name: " + TEST_ASSIGNMENT_NAME + "\n");
            w.append("files:\n");
            w.append("  - !file:plain 123\n");
            w.close();
        }
    }

    @After
    public void tearDown() throws Exception {
        for (InputFile file : InputFile.values()) {
            if (file != InputFile.NONEXISTENT) try {
                Files.delete(file.p);
            } catch (IOException ignored) {}
        }
    }

    @Test(expected = InvalidCriteriaException.class)
    public void emptyFileShouldThrowException() throws Exception {
        Criteria.loadFromYAML(InputFile.EMPTY_FILE.p);
    }

    @Test(expected = InvalidCriteriaException.class)
    public void invalidTopLevelShouldThrowException() throws Exception {
        Criteria.loadFromYAML(InputFile.INVALID_TOP.p);
    }

    @Test
    public void basicShouldWork() throws Exception {
        Criteria c = Criteria.loadFromYAML(InputFile.BASIC.p);
        assertEquals(c.assignmentName, TEST_ASSIGNMENT_NAME);
        assertEquals(c.files.size(), 0);
    }

    @Test(expected = InvalidCriteriaException.class)
    public void unrecognizedMappingShouldThrowException() throws Exception {
        Criteria.loadFromYAML(InputFile.BASIC_UNRECOGNIZED.p);
    }

    @Test(expected = InvalidCriteriaException.class)
    public void fileNonMappingShouldThrowException() throws Exception {
        Criteria.loadFromYAML(InputFile.FILE_NONMAPPING.p);
    }
}