package io.breen.socrates.immutable.test.implementation.python;

import org.junit.*;

import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PythonInspectorTest {

    public Path parentDir;
    public List<Path> paths;

    @Before
    public void setUp() throws Exception {
        parentDir = Paths.get("parent");

        paths = Arrays.asList(
                Paths.get(parentDir.toString(), "zero.py"),
                Paths.get(parentDir.toString(), "one.py"),
                Paths.get(parentDir.toString(), "two.py"),
                Paths.get(parentDir.toString(), "three.py")
        );

        Files.createDirectory(parentDir);

        for (Path p : paths)
            Files.createFile(p);

        {
            // zero.py: an empty file
        }

        {
            // one.py: a Python file with a syntax error
            Files.newBufferedWriter(paths.get(1), Charset.defaultCharset())
                 .append("foo((3\n")
                 .close();
        }

        {
            // two.py: a valid Python file with some variables
            Files.newBufferedWriter(paths.get(2), Charset.defaultCharset())
                 .append("foo = 3\n")
                 .append("bar = True\n")
                 .append("baz = [1, 2, 3]\n")
                 .close();
        }

        {
            // three.py: a valid Python file with some functions
            Files.newBufferedWriter(paths.get(3), Charset.defaultCharset())
                 .append("def ten():\n")
                 .append("    return 10\n")
                 .append("\n")
                 .append("def twice(n):\n")
                 .append("    return 2 * n\n")
                 .append("\n")
                 .append("def odds(n):\n")
                 .append("    return [2 * x + 1 for x in range(n)]\n")
                 .append("\n")
                 .close();
        }
    }

    @After
    public void tearDown() throws Exception {
        for (Path p : paths)
            Files.delete(p);

        Files.delete(parentDir);
    }

    @Test
    public void shouldFindVariable() throws Exception {
        assertTrue(new PythonInspector(paths.get(2)).variableExists("foo"));
    }

    @Test
    public void shouldFailWhenMissingVariable() throws Exception {
        assertFalse(new PythonInspector(paths.get(2)).variableExists("goo"));
    }

    @Test
    public void shouldEquateMatchingIntVariables() throws Exception {
        assertTrue(new PythonInspector(paths.get(2)).variableEquals("foo", 3));
    }

    @Test
    public void shouldRejectMismatchingIntVariables() throws Exception {
        assertFalse(new PythonInspector(paths.get(2)).variableEquals("foo", 6));
    }

    @Test
    public void shouldEquateMatchingBoolVariables() throws Exception {
        assertTrue(new PythonInspector(paths.get(2)).variableEquals("bar", true));
    }

    @Test
    public void shouldRejectMismatchingBoolVariables() throws Exception {
        assertFalse(new PythonInspector(paths.get(2)).variableEquals("bar", false));
    }

    @Test
    public void shouldEquateMatchingIntListVariablesList() throws Exception {
        List<Integer> list = new ArrayList<>(3);
        list.add(1);
        list.add(2);
        list.add(3);
        assertTrue(new PythonInspector(paths.get(2)).variableEquals("baz", list));
    }

    @Test
    public void shouldEquateMatchingIntListVariablesArray() throws Exception {
        Integer[] arr = {1, 2, 3};
        assertTrue(new PythonInspector(paths.get(2)).variableEquals("baz", arr));
    }

    @Test
    public void shouldRejectMismatchingIntListVariablesList() throws Exception {
        List<Integer> list = Arrays.asList(4, 5, 6);
        assertFalse(new PythonInspector(paths.get(2)).variableEquals("baz", list));
    }

    @Test
    public void shouldRejectMismatchingIntListVariablesArray() throws Exception {
        Integer[] arr = {6, 7, 8};
        assertFalse(new PythonInspector(paths.get(2)).variableEquals("baz", arr));
    }

    @Test
    public void shouldFindFunction() throws Exception {
        assertTrue(new PythonInspector(paths.get(3)).functionExists("ten"));
    }

    @Test
    public void shouldFailWhenMissingFunction() throws Exception {
        assertFalse(new PythonInspector(paths.get(3)).variableExists("zzz"));
    }

    @Test
    public void shouldEquateCorrectFunctionReturnValue() throws Exception {
        assertTrue(
                new PythonInspector(paths.get(3)).functionProduces(
                        "ten", null, null, null, 10, null
                )
        );
    }

    @Test
    public void shouldRejectIncorrectFunctionReturnValue() throws Exception {
        assertFalse(
                new PythonInspector(paths.get(3)).functionProduces(
                        "ten", null, null, null, 99, null
                )
        );
    }

    @Test
    public void shouldEquateCorrectFunctionReturnValueWithArgs() throws Exception {
        assertTrue(
                new PythonInspector(paths.get(3)).functionProduces(
                        "twice", Collections.singletonList((Object)20), null, null, 40, null
                )
        );
    }

    @Test
    public void shouldRejectIncorrectFunctionReturnValueWithArgs() throws Exception {
        assertFalse(
                new PythonInspector(paths.get(3)).functionProduces(
                        "twice", Collections.singletonList((Object)25), null, null, 99, null
                )
        );
    }

    @Test
    public void shouldEquateCorrectFunctionReturnValueListWithArgs() throws Exception {
        assertTrue(
                new PythonInspector(paths.get(3)).functionProduces(
                        "odds",
                        Collections.singletonList((Object)3),
                        null,
                        null,
                        Arrays.asList(1, 3, 5),
                        null
                )
        );
    }
}
