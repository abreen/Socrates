package io.breen.socrates;

import java.util.Properties;

/**
 * The single location for a small number (as little as possible!) of runtime-set
 * variables that are needed anywhere.
 */
public class Globals {
    public static Properties properties;

    public enum OS {
        WINDOWS, OSX, LINUX, OTHER
    }

    public static OS operatingSystem;

    static {
        String os = System.getProperty("os.name");

        if (os.startsWith("Windows")) {
            Globals.operatingSystem = OS.WINDOWS;
        } else if (os.startsWith("Mac")) {
            Globals.operatingSystem = OS.OSX;
        } else if (os.contains("Linux")) {
            Globals.operatingSystem = OS.LINUX;
        } else {
            Globals.operatingSystem = OS.OTHER;
        }
    }
}
