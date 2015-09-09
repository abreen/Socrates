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
    public static Path XMLRPCServer;
    public static Path socratesModule;
    private static Logger logger = Logger.getLogger(PythonManager.class.getName());
    private static Path tempDir;

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
            paths.add(Paths.get("C:\\Python34\\python.exe"));
            paths.add(Paths.get("C:\\Python33\\python.exe"));
            paths.add(Paths.get("C:\\Python32\\python.exe"));
            paths.add(Paths.get("C:\\Python31\\python.exe"));
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

        try {
            tempDir = Files.createTempDirectory("python");
        } catch (IOException e) {
            logger.severe("could not set up Python source directory:" + e);
        }

        logger.info("set up Python source directory: " + tempDir);

        /*
         * Get the necessary resources as streams and write them to the temporary directory.
         */
        try {
            InputStream stream = PythonManager.class.getResourceAsStream("server.py");
            Path tempFile = Paths.get(tempDir.toString(), "server.py");
            Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            XMLRPCServer = tempFile;

            stream = PythonManager.class.getResourceAsStream("socrates.py");
            tempFile = Paths.get(tempDir.toString(), "socrates.py");
            Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            socratesModule = tempFile;

        } catch (IOException x) {
            logger.severe("I/O error copying Python source to temporary directory: " + x);
        }
    }

    private PythonManager() {}

    public static Path getPythonPathDirectory() {
        return tempDir;
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
