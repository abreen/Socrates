package io.breen.socrates;

import org.apache.commons.lang.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * The single location for a small number (as little as possible!) of runtime-set
 * variables that are needed anywhere.
 */
public class Globals {

    public static final int NORMAL_EXIT_CODE = 0;

    private static final Pattern PYTHON3_VERSION_PATTERN = Pattern.compile("Python 3.*");

    private static Logger logger = Logger.getLogger(Globals.class.getName());

    public static Properties properties;

    public enum OS {
        WINDOWS, OSX, LINUX, OTHER
    }

    public static OS operatingSystem;

    /**
     * A working path to the Python 3 interpreter, usable by (for example) ProcessBuilder
     * to run Python 3 programs.
     */
    public static Path python3Command;

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
        paths.add(Paths.get("python3"));
        paths.add(Paths.get("python"));

        switch (Globals.operatingSystem) {
        case WINDOWS:
            paths.add(Paths.get("C:\\Python\\python.exe"));
            paths.add(Paths.get("C:\\Program Files\\Python\\python.exe"));
            break;
        case OSX:
        case LINUX:
            paths.add(Paths.get("/usr/local/bin/python3"));
            paths.add(Paths.get("/usr/local/bin/python"));
        }

        for (Path p : paths) {
            try {
                if (isValidPython3InterpreterPath(p)) {
                    python3Command = p;
                    break;
                }
            } catch (InterruptedException x) {
                logger.severe("interrupted trying to test Python command: " + p);
                System.exit(6);
            } catch (IOException x) {
                logger.severe("I/O error trying to test Python command: " + p);
                System.exit(6);
            }
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
        return PYTHON3_VERSION_PATTERN.matcher(versionString).matches();
    }
}
