package io.breen.socrates.python;

import io.breen.socrates.Globals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Like ProcessBuilder for general-purpose creation of subprocesses, but for Python
 * processes specifically. This class delegates its functionality to an underlying
 * ProcessBuilder. Its start() method always produces instances of Python subprocesses.
 *
 * The API of this class does not exactly match that of ProcessBuilder. Some methods have
 * been "updated" to more contemporary names.
 *
 * @see java.lang.ProcessBuilder
 */
public class PythonProcessBuilder {

    private final ProcessBuilder pb;

    /**
     * Create a new PythonProcessBuilder that will attempt to run the Python module
     * specified. If the module needs to take command line arguments, these may be
     * supplied after the path to the module.
     */
    public PythonProcessBuilder(Path pythonModule, String... args) {
        List<String> cmd = new LinkedList<>();
        cmd.add(Globals.python3Command.toString());
        cmd.add("-B");                      // turns off writing bytecode files (.py[co])
        cmd.add(pythonModule.toAbsolutePath().toString());
        cmd.addAll(Arrays.asList(args));

        pb = new ProcessBuilder(cmd);

        Map<String, String> env = pb.environment();
        env.put(
                "PYTHONPATH",
                PythonAPIManager.getPythonPathDirectory().toAbsolutePath().toString()
        );

        switch (Globals.operatingSystem) {
        case WINDOWS:
            pb.redirectInput(new java.io.File("NUL"));
            break;
        case OSX:
        case LINUX:
            pb.redirectInput(new java.io.File("/dev/null"));
        }
    }

    public Map<String, String> getEnvironment() {
        return pb.environment();
    }

    public Path getDirectory() {
        return pb.directory().toPath();
    }

    public void setDirectory(Path dir) {
        pb.directory(dir.toFile());
    }

    public void redirectOutputTo(ProcessBuilder.Redirect destination) {
        pb.redirectOutput(destination);
    }

    public void redirectInputTo(ProcessBuilder.Redirect destination) {
        pb.redirectInput(destination);
    }

    public void redirectErrorTo(ProcessBuilder.Redirect destination) {
        pb.redirectError(destination);
    }

    public Process start() throws IOException {
        return pb.start();
    }
}
