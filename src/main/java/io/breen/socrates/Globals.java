package io.breen.socrates;

import org.apache.commons.lang.SystemUtils;

import java.io.IOException;
import java.nio.file.*;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * The single location for a small number (as little as possible!) of runtime-set variables that are
 * needed anywhere.
 */
public class Globals {

    public enum OS {
        WINDOWS, OSX, LINUX, OTHER
    }
    public static final int NORMAL_EXIT_CODE = 0;
    private static final Pattern PYTHON3_VERSION_PATTERN = Pattern.compile("Python 3.*");
    public static Properties properties;
    public static OS operatingSystem;
    /**
     * A working path to the Python 3 interpreter, usable by (for example) ProcessBuilder to run
     * Python 3 programs.
     */
    public static Path python3Command;
    private static Logger logger = Logger.getLogger(Globals.class.getName());

    static {
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
        paths.add(Paths.get("python"));
        paths.add(Paths.get("python3"));

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
            } catch (IOException x) {
                logger.warning("I/O error trying to test Python command " + p + ": " + x);
            }
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
        ProcessBuilder builder = new ProcessBuilder(pathStr, "--version");

        Path temp = Files.createTempFile(null, null);
        builder.redirectOutput(temp.toFile());

        Process process = builder.start();
        if (process.waitFor() != NORMAL_EXIT_CODE) {
            Files.delete(temp);
            return false;
        }

        String versionString = Files.newBufferedReader(temp).readLine();

        if (versionString == null)
            // file was empty
            return false;
        else return PYTHON3_VERSION_PATTERN.matcher(versionString).matches();
    }

    public static ZoneId getZoneId() {
        String zoneString = properties.getProperty("timezone");
        return ZoneId.of(zoneString);
    }
}
