package io.breen.socrates;

import io.breen.pyfinder.PythonInterpreter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;


public class Globals {

    public enum OS {
        WINDOWS, OSX, LINUX, OTHER
    }

    public static final Color GRAY = new Color(140, 140, 140);
    public static final Color LIGHT_GRAY = new Color(186, 186, 186);
    public static final Color YELLOW = new Color(205, 174, 0);
    public static final Color RED = new Color(189, 12, 13);
    public static final Color GREEN = new Color(49, 141, 34);
    public static final Color BLUE = new Color(37, 123, 210);

    public static final SimpleDateFormat ISO8601_UTC;
    public static final SimpleDateFormat ISO8601;

    public static final String DEFAULT_GRADE_FILE_NAME = "grade.txt";

    public static final Properties defaultProperties;

    public static Properties properties;
    public static OS operatingSystem;

    public static PythonInterpreter interpreter = null;
    private static final Path SOCRATES_TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "socrates");

    static {
        ISO8601_UTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        ISO8601_UTC.setTimeZone(TimeZone.getTimeZone("UTC"));

        ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);

        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            operatingSystem = OS.OSX;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            operatingSystem = OS.WINDOWS;
        } else if (SystemUtils.IS_OS_LINUX) {
            operatingSystem = OS.LINUX;
        } else {
            operatingSystem = OS.OTHER;
        }

        defaultProperties = new Properties();
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

    public static String toHex(Color c) {
        String hex = Integer.toHexString(c.getRGB());
        return "#" + hex.substring(2);
    }

    /**
     * Given a relative path to a file ("resource") stored in this package, this method returns the absolute
     * path to the extracted file on the actual file system (i.e., outside of a JAR, if this code is running from such
     * a JAR). If the file can't be found, this method returns null. If the file exists, but hasn't been extracted to
     * the file system, this method extracts it.
     */
    public static Path extractOrGetFile(Path modulePath) throws IOException {
        InputStream is = Globals.class.getClassLoader().getResourceAsStream(modulePath.toString());

        if (is == null)
            return null;

        Path dest = Paths.get(SOCRATES_TEMP_DIR.toString(), modulePath.toString());

        if (Files.exists(dest))
            return dest;

        FileUtils.copyInputStreamToFile(is, dest.toFile());
        return dest;
    }
}
