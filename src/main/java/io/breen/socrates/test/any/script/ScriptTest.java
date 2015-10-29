package io.breen.socrates.test.any.script;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.File;
import io.breen.socrates.python.PythonManager;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;

import javax.swing.text.Document;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class ScriptTest extends Test implements Automatable {

    public static final int PASSED_EXIT_CODE = 10;
    public static final int FAILED_EXIT_CODE = 11;

    /**
     * The string specifying the path to this script, relative to the "scripts" directory in a
     * criteria package. (This comes directly from the criteria file.)
     */
    public String path;

    /**
     * A map containing parameters needed to run the test. (This comes directly from the criteria
     * file.) These parameters are optional, and are sent to the script when it starts.
     */
    public Map<String, Object> parameters;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public ScriptTest() {}

    @Override
    public String toString() {
        return "ScriptTest(deduction=" + deduction + ", " +
                "description=" + description +
                ")";
    }

    @Override
    public String getTestTypeName() {
        return "script-based test";
    }

    @Override
    public boolean shouldPass(File parent, SubmittedFile target, Submission submission,
                              Criteria criteria, Document transcript)
            throws CannotBeAutomatedException, AutomationFailureException
    {
        Path scriptPath = criteria.scripts.get(path);
        if (scriptPath == null)
            throw new AutomationFailureException("could not find script: " + path);

        ProcessBuilder builder = new ProcessBuilder(
                PythonManager.python3Command.toString(), "-B",
                // turns off writing bytecode files (.py[co])
                scriptPath.toString()
        );

        builder.redirectError(ProcessBuilder.Redirect.INHERIT);

        /*
         * This allows scripts to import modules directly from student code. It adds the
         * current working directory of the Python interpreter to the import search path list.
         * Note that we also add the temporary directory containing the "socrates.py" file.
         */
        Map<String, String> env = builder.environment();
        env.put(
                "PYTHONPATH",
                System.getProperty("path.separator") + PythonManager.getTempDirectory().toString()
        );

        Path parentDir = target.fullPath.getParent();
        builder.directory(parentDir.toFile());

        int exitCode;
        try {
            Process process = builder.start();

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(process.getOutputStream(), parameters);

            exitCode = process.waitFor();

        } catch (IOException | InterruptedException x) {
            throw new AutomationFailureException(x);
        }

        if (exitCode != PASSED_EXIT_CODE && exitCode != FAILED_EXIT_CODE)
            throw new AutomationFailureException("script exited abnormally");

        return exitCode == PASSED_EXIT_CODE;
    }
}
