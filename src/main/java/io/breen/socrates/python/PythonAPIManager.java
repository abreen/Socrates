package io.breen.socrates.python;

import io.breen.socrates.immutable.test.implementation.any.script.ScriptTest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Static singleton class managing the Python API (that is, the "socrates" module that
 * can be imported by Python scripts and hooks).
 *
 * At runtime, this class generates the "socrates" module and writes it to a temporary
 * location in the file system. Each time PythonProcessBuilder creates a Python 3
 * subprocess, it will add this temporary location to its PYTHONPATH.
 *
 * @see io.breen.socrates.python.PythonProcessBuilder
 * @see io.breen.socrates.immutable.hooks.HookManager
 * @see io.breen.socrates.immutable.test.implementation.any.script.ScriptTest
 */
public final class PythonAPIManager {

    private static Logger logger = Logger.getLogger(PythonAPIManager.class.getName());

    private static Path tempDir;

    /**
     * The Python 3 source of the "socrates" module.
     */
    private static String[] moduleSource = {
            "def pass_test():",
            "\treturn '" + ScriptTest.PASSED_OUTPUT + "'",
            "def fail_test():",
            "\treturn '" + ScriptTest.FAILED_OUTPUT + "'"
    };

    static {
        try {
            tempDir = Files.createTempDirectory("python");
            generateSocratesModule();
        } catch (IOException e) {
            logger.severe("could not set up Python API:" + e);
            System.exit(7);
        }

        logger.info("set up Python API in " + tempDir);
    }

    private PythonAPIManager() {}

    public static Path getPythonPathDirectory() {
        return tempDir;
    }

    private static void generateSocratesModule() throws IOException {
        Path module = Paths.get(tempDir.toString(), "socrates.py");
        Files.createFile(module);

        BufferedWriter writer = Files.newBufferedWriter(module);
        for (String line : moduleSource) {
            writer.write(line, 0, line.length());
            writer.newLine();
        }

        writer.close();
    }
}
