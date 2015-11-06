package io.breen.socrates.test.any;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.breen.socrates.Globals;
import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.File;
import io.breen.socrates.python.PythonManager;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.*;

import javax.swing.text.Document;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class ScriptTest extends Test implements Automatable {

    /**
     * The string specifying the path to this script, relative to the "scripts" directory in a
     * criteria package. (This comes directly from the criteria file.)
     */
    public String path;

    /**
     * A map containing parameters needed to run the test. (This comes directly from the criteria
     * file.) These parameters are optional, and are sent to the script when it starts.
     */
    public Map<String, Object> parameters = Collections.emptyMap();

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
                              Criteria criteria, Document transcript, Document notes)
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

        ObjectMapper mapper = new ObjectMapper();
        Process process;

        int exitCode;
        try {
            process = builder.start();

            Map<String, Object> params = new HashMap<>(parameters);
            params.put("target_full_path", target.fullPath.toString());
            params.put("target_local_path", target.localPath.toString());
            params.put("student_name", submission.studentName);

            params.put("static_path", criteria.getStaticDir().toString());

            mapper.writeValue(process.getOutputStream(), params);

            exitCode = process.waitFor();

        } catch (IOException | InterruptedException x) {
            throw new AutomationFailureException(x);
        }

        if (exitCode != Globals.NORMAL_EXIT_CODE)
            throw new AutomationFailureException("script exited abnormally");

        Map<String, Object> response;
        try {
            response = mapper.readValue(process.getInputStream(), Map.class);
        } catch (IOException x) {
            throw new AutomationFailureException(x);
        }

        if (isErrorResponse(response)) {
            String errorType = (String)response.get("error_type");
            String errorMessage = (String)response.get("error_message");

            throw new CannotBeAutomatedException(
                    "script raised error: " + errorType + ": " + errorMessage
            );
        }

        String transcriptStr = (String)response.get("transcript");
        if (transcriptStr != null) {
            if (!transcriptStr.endsWith("\n")) transcriptStr += "\n";

            appendToDocument(transcript, transcriptStr);
        }

        String notesStr = (String)response.get("notes");
        if (notesStr != null) {
            appendToDocument(notes, notesStr);
        }

        return (boolean)response.get("should_pass");
    }

    private boolean isErrorResponse(Map<String, Object> response) {
        return response.containsKey("error") && (boolean)response.get("error");
    }
}
