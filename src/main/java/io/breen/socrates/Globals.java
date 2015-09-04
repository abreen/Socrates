package io.breen.socrates;

import org.apache.commons.lang.SystemUtils;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * The single location for a small number (as little as possible!) of global variables and
 * OS-specific methods that are needed anywhere and don't belong anywhere else.
 */
public class Globals {

    public enum OS {
        WINDOWS, OSX, LINUX, OTHER
    }

    public static final int NORMAL_EXIT_CODE = 0;
    public static final Color GRAY = new Color(140, 140, 140);
    public static final Color LIGHT_GRAY = new Color(186, 186, 186);
    public static final Color YELLOW = new Color(205, 174, 0);
    public static final Color RED = new Color(189, 12, 13);
    public static final Color GREEN = new Color(49, 141, 34);
    public static final Color BLUE = new Color(37, 123, 210);
    public static final SimpleDateFormat ISO8601_UTC;
    public static final SimpleDateFormat ISO8601;
    public static final String DEFAULT_GRADE_FILE_NAME = "grade.txt";
    public static Properties properties;
    public static OS operatingSystem;
    /**
     * A working path to the Python 3 interpreter, usable by (for example) ProcessBuilder to run
     * Python 3 programs.
     */
    public static Path python3Command;
    private static Logger logger = Logger.getLogger(Globals.class.getName());

    static {
        ISO8601_UTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        ISO8601_UTC.setTimeZone(TimeZone.getTimeZone("UTC"));

        ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US);

        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            operatingSystem = OS.OSX;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            operatingSystem = OS.WINDOWS;
        } else if (SystemUtils.IS_OS_LINUX) {
            operatingSystem = OS.LINUX;
        } else {
            operatingSystem = OS.OTHER;
        }

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
                logger.warning(
                        "interrupted trying to test Python command " + p + ": " + x
                );
            } catch (IOException ignored) {}
        }

        if (python3Command == null) {
            logger.severe("cannot find the Python 3 interpreter");
            System.exit(5);
        }
    }

    private static boolean isValidPython3InterpreterPath(Path path)
            throws IOException, InterruptedException
    {
        String pathStr = path.toString();

        ProcessBuilder builder = new ProcessBuilder(
                pathStr, "-c", "def f(): pass"
        );

        if (builder.start().waitFor() == NORMAL_EXIT_CODE) {
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

    public static void enableFullScreen(Window window) {
        String className = "com.apple.eawt.FullScreenUtilities";
        String methodName = "setWindowCanFullScreen";

        try {
            Class<?> clazz = Class.forName(className);
            java.lang.reflect.Method method = clazz.getMethod(
                    methodName, Window.class, boolean.class
            );
            method.invoke(null, window, true);
        } catch (Throwable ignored) {}
    }
}
