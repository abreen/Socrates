package io.breen.socrates.immutable.test.implementation.any.script;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.criteria.Resource;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.*;
import io.breen.socrates.python.PythonManager;

import javax.swing.text.Document;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

public class ScriptTest extends Test implements Automatable {

    public static final int PASSED_EXIT_CODE = 1;
    public static final int FAILED_EXIT_CODE = 2;

    private static Logger logger = Logger.getLogger(ScriptTest.class.getName());

    public Resource script;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public ScriptTest() {}

    public ScriptTest(double deduction, String description, Resource script) {
        super(deduction, description);
        this.script = script;
    }

    @Override
    public String toString() {
        return "ScriptTest(deduction=" + deduction + ", " +
                "description=" + description + ", " +
                "script=" + script +
                ")";
    }

    @Override
    public String getTestTypeName() {
        return "script-based test";
    }

    /**
     * @throws CannotBeAutomatedException
     * @throws ScriptTestRuntimeException
     */
    @Override
    public boolean shouldPass(File parent, SubmittedFile target, Submission submission,
                              Criteria criteria, Document transcript)
            throws CannotBeAutomatedException
    {
        logger.info("running ScriptTest: " + this);

        try {
            ProcessBuilder builder = new ProcessBuilder(
                    PythonManager.python3Command.toString(), "-B", script.getPath().toString()
            );

            builder.redirectError(ProcessBuilder.Redirect.INHERIT);

            /*
             * This allows scripts to import modules directly from student code. It adds the
             * current working directory of the Python interpreter to the import search path list.
             */
            Map<String, String> env = builder.environment();
            env.put("PYTHONPATH", System.getProperty("path.separator"));

            Path parentDir = target.fullPath.getParent();
            builder.directory(parentDir.toFile());

            Process process = builder.start();

            int exitCode = process.waitFor();
            if (exitCode != PASSED_EXIT_CODE && exitCode != FAILED_EXIT_CODE)
                throw new ScriptTestAbnormalExit(this, exitCode);

            return exitCode == PASSED_EXIT_CODE;

        } catch (Exception e) {
            throw new ScriptTestRuntimeException(e);
        }
    }
}
