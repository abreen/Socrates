package io.breen.socrates.python;

import io.breen.socrates.Globals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class PythonProcessBuilderTest {

    private static final String SIMPLE_OUTPUT_STRING = "Hello, world!";

    private enum PythonModule {
        SIMPLE(Paths.get("simple.py")),
        SIMPLE_OUTPUT(Paths.get("output.py"));

        Path p;

        PythonModule(Path p) {
            this.p = p;
        }
    }

    @Before
    public void setUp() throws Exception {
        {
            // simple program
            Files.createFile(PythonModule.SIMPLE.p);
            Files.newBufferedWriter(PythonModule.SIMPLE.p)
                 .append("foo = 3\n")
                 .append("bar = 5\n")
                 .close();
        }

        {
            // simple program sending bytes to the standard out
            Files.createFile(PythonModule.SIMPLE_OUTPUT.p);
            Files.newBufferedWriter(PythonModule.SIMPLE_OUTPUT.p)
                 .append("print('" + SIMPLE_OUTPUT_STRING + "')\n")
                 .close();
        }
    }

    @After
    public void tearDown() throws Exception {
        for (PythonModule file : PythonModule.values()) {
            try {
                Files.delete(file.p);
            } catch (IOException ignored) {}
        }
    }

    @Test
    public void shouldRunSimpleProgram() throws Exception {
        PythonProcessBuilder builder = new PythonProcessBuilder(PythonModule.SIMPLE.p);
        Process process = builder.start();
        assertEquals(Globals.NORMAL_EXIT_CODE, process.waitFor());
    }

    @Test
    public void shouldRunSimpleOutputProgram() throws Exception {
        PythonProcessBuilder builder = new PythonProcessBuilder(
                PythonModule.SIMPLE_OUTPUT.p
        );
        Path temp = Files.createTempFile(null, null);
        builder.redirectOutputTo(ProcessBuilder.Redirect.to(temp.toFile()));
        Process process = builder.start();
        assertEquals(Globals.NORMAL_EXIT_CODE, process.waitFor());
        String line = Files.newBufferedReader(temp).readLine();
        Files.delete(temp);
        assertEquals(SIMPLE_OUTPUT_STRING, line);
    }
}
