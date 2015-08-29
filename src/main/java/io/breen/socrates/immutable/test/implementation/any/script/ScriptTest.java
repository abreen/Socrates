package io.breen.socrates.immutable.test.implementation.any.script;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.criteria.Resource;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.Submission;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.*;
import io.breen.socrates.python.PythonProcessBuilder;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class ScriptTest extends Test implements Automatable {

    public static final String PASSED_OUTPUT = "passed";
    public static final String FAILED_OUTPUT = "failed";

    private static Logger logger = Logger.getLogger(ScriptTest.class.getName());

    protected final Resource script;

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
    public boolean shouldPass(File parent, SubmittedFile target, Submission submission)
            throws CannotBeAutomatedException
    {
        logger.info("running ScriptTest: " + this);

        try {
            Path output = Files.createTempFile(null, null);

            PythonProcessBuilder builder = new PythonProcessBuilder(script.getPath());
            builder.redirectOutputTo(ProcessBuilder.Redirect.to(output.toFile()));

            Process process = builder.start();

            int exitCode = process.waitFor();
            if (exitCode != Globals.NORMAL_EXIT_CODE)
                throw new ScriptTestAbnormalExit(this, exitCode);

            BufferedReader reader = Files.newBufferedReader(output);
            String lastLine = reader.lines().reduce((prev, current) -> current).get();

            if (lastLine == null) throw new ScriptTestInvalidResult(this, null);

            switch (lastLine) {
            case PASSED_OUTPUT:
                return true;
            case FAILED_OUTPUT:
                return false;
            default:
                throw new ScriptTestInvalidResult(this, lastLine);
            }

        } catch (Exception e) {
            throw new ScriptTestRuntimeException(e);
        }
    }
}
