package io.breen.socrates;

import org.apache.commons.lang.SystemUtils;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * The single location for a small number (as little as possible!) of global variables and
 * other methods that are needed anywhere and don't belong anywhere else.
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
    private static Logger logger = Logger.getLogger(Globals.class.getName());

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
}
