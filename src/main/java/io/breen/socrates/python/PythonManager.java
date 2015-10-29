package io.breen.socrates.python;

import io.breen.socrates.Globals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Static singleton class managing Python-related operations. Among other tasks, this class
 * determines a path to a Python 3 interpreter on the current system, and creates a temporary
 * directory in which Python source modules are copied (e.g., the "socrates" module used by
 * hooks).
 */
public final class PythonManager {

    /**
     * A working path to the Python 3 interpreter, usable by (for example) ProcessBuilder to run
     * Python 3 programs.
     */
    public static Path python3Command;

    private static Path socratesTmpDir;

    private static Logger logger = Logger.getLogger(PythonManager.class.getName());

    static {
        List<Path> paths = new LinkedList<>();

        Map<String, String> env = System.getenv();
        if (env.containsKey("PATH")) {
            String path = env.get("PATH");

            String pathSep = System.getProperty("path.separator");
            String[] dirs = path.split(pathSep);

            for (String dir : dirs) {
                paths.add(Paths.get(dir, "python"));
                paths.add(Paths.get(dir, "python3"));
            }
        }

        switch (Globals.operatingSystem) {
        case WINDOWS:
            paths.add(Paths.get("C:\\Python35\\python.exe"));
            paths.add(Paths.get("C:\\Python34\\python.exe"));
            paths.add(Paths.get("C:\\Python33\\python.exe"));
            paths.add(Paths.get("C:\\Python32\\python.exe"));
            paths.add(Paths.get("C:\\Python31\\python.exe"));
            paths.add(Paths.get("C:\\Python30\\python.exe"));
            break;
        case OSX:
        case LINUX:
            paths.add(Paths.get("/usr/local/bin/python"));
            paths.add(Paths.get("/usr/local/bin/python3"));
            paths.add(Paths.get("/usr/bin/python"));
            paths.add(Paths.get("/usr/bin/python3"));
        }

        for (Path p : paths) {
            try {
                if (isValidPython3InterpreterPath(p)) {
                    python3Command = p;
                    break;
                }
            } catch (InterruptedException x) {
                logger.warning("interrupted trying to test Python command " + p + ": " + x);
            } catch (IOException ignored) {}
        }

        if (python3Command == null) {
            logger.severe("cannot find the Python 3 interpreter");
        }

        /*
         * Create a "socrates" directory in the system temporary directory, get the necessary
         * resources as streams and copy them to the directory.
         */
        try {
            socratesTmpDir = Files.createTempDirectory("socrates");

            for (String f : Arrays.asList("tester.py", "socrates.py")) {
                InputStream stream = PythonManager.class.getResourceAsStream(f);
                Path tempFile = Paths.get(socratesTmpDir.toString(), f);
                Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException x) {
            logger.severe("I/O error copying Python source to Socrates temporary directory: " + x);
        }
    }

    private PythonManager() {}

    /**
     * Given the name of a Python source code file (e.g., "socrates.py", the Socrates Python API
     * module), this method returns a Path object representing the current location of that
     * file in the system temporary directory.
     */
    public static Path getPathToSource(String scriptName) {
        return Paths.get(socratesTmpDir.toString(), scriptName);
    }

    private static boolean isValidPython3InterpreterPath(Path path)
            throws IOException, InterruptedException
    {
        String pathStr = path.toString();

        ProcessBuilder builder = new ProcessBuilder(
                pathStr, "-c", "def f(): pass"
        );

        if (builder.start().waitFor() == Globals.NORMAL_EXIT_CODE) {
            // this path is probably valid for a Python interpreter

            // but is it Python >= 3.3?
            ProcessBuilder builder2 = new ProcessBuilder(
                    pathStr,
                    "-c",
                    "import sys; v = sys.version_info; sys.exit(v.major * 10 + v.minor)"
            );

            int exitCode = builder2.start().waitFor();
            return exitCode >= 33 && exitCode < 40;
        } else {
            return false;
        }
    }
}
