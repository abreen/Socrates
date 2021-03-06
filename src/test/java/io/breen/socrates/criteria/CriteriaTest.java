package io.breen.socrates.criteria;

import io.breen.pyfinder.PythonFinder;
import io.breen.pyfinder.PythonInterpreter;
import io.breen.pyfinder.PythonVersion;
import io.breen.socrates.Globals;
import org.junit.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CriteriaTest {

    private enum InputFile {
        NONEXISTENT(Paths.get("nonexistent." + Criteria.CRITERIA_FILE_EXTENSIONS[0])),
        EMPTY_FILE(Paths.get("empty." + Criteria.CRITERIA_FILE_EXTENSIONS[0])),
        INVALID_TOP(Paths.get("invalid_top." + Criteria.CRITERIA_FILE_EXTENSIONS[0])),
        BASIC(Paths.get("basic." + Criteria.CRITERIA_FILE_EXTENSIONS[0])),
        BASIC_UNRECOGNIZED(
                Paths.get(
                        "basic_unrecognized." + Criteria.CRITERIA_FILE_EXTENSIONS[0]
                )
        ),
        FILE_NONMAPPING(
                Paths.get(
                        "file_nonmapping." + Criteria.CRITERIA_FILE_EXTENSIONS[0]
                )
        );

        Path p;

        InputFile(Path p) {
            this.p = p;
        }
    }

    private String TEST_ASSIGNMENT_NAME = "Test Assignment";

    @Before
    public void setUp() throws Exception {
        Globals.interpreter = new PythonFinder().findOrNewer(new PythonVersion(3, 2)).get(0);

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
            Files.newBufferedWriter(InputFile.INVALID_TOP.p, StandardCharsets.UTF_8)
                 .append("1234")
                 .close();
        }

        {
            // basic working criteria file
            Files.createFile(InputFile.BASIC.p);
            BufferedWriter w = Files.newBufferedWriter(InputFile.BASIC.p, StandardCharsets.UTF_8);
            w.append("assignmentName: " + TEST_ASSIGNMENT_NAME + "\n");
            w.append("files: []");
            w.close();
        }

        {
            // basic criteria file, but with an unrecognized top-level mapping
            Files.createFile(InputFile.BASIC_UNRECOGNIZED.p);
            BufferedWriter w = Files.newBufferedWriter(
                    InputFile.BASIC_UNRECOGNIZED.p, StandardCharsets.UTF_8
            );
            w.append("assignmentName: " + TEST_ASSIGNMENT_NAME + "\n");
            w.append("foo: bar\n");
            w.append("files: []");
            w.close();
        }

        {
            // basic criteria file, but with a file specification that is not a mapping
            Files.createFile(InputFile.FILE_NONMAPPING.p);
            BufferedWriter w = Files.newBufferedWriter(
                    InputFile.FILE_NONMAPPING.p, StandardCharsets.UTF_8
            );
            w.append("assignmentName: " + TEST_ASSIGNMENT_NAME + "\n");
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
        Criteria.loadFromPath(InputFile.EMPTY_FILE.p);
    }

    @Test(expected = InvalidCriteriaException.class)
    public void invalidTopLevelShouldThrowException() throws Exception {
        Criteria.loadFromPath(InputFile.INVALID_TOP.p);
    }

    @Test
    public void basicShouldWork() throws Exception {
        Criteria c = Criteria.loadFromPath(InputFile.BASIC.p);
        assertEquals(TEST_ASSIGNMENT_NAME, c.assignmentName);
        assertEquals(0, c.files.size());
    }

    @Test(expected = InvalidCriteriaException.class)
    public void unrecognizedMappingShouldThrowException() throws Exception {
        Criteria.loadFromPath(InputFile.BASIC_UNRECOGNIZED.p);
    }

    @Test(expected = InvalidCriteriaException.class)
    public void fileNonMappingShouldThrowException() throws Exception {
        Criteria.loadFromPath(InputFile.FILE_NONMAPPING.p);
    }
}
