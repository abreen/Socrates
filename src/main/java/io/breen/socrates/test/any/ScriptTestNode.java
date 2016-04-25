package io.breen.socrates.test.any;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.breen.socrates.Globals;
import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.file.File;
import io.breen.socrates.submission.SubmittedFile;
import io.breen.socrates.test.Automatable;
import io.breen.socrates.test.AutomationFailureException;
import io.breen.socrates.test.CannotBeAutomatedException;
import io.breen.socrates.test.TestNode;

import javax.swing.text.Document;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ScriptTestNode extends TestNode implements Automatable {

    /**
     * The string specifying the path to this script, relative to the "scripts" directory in a
     * criteria package. (This comes directly from the criteria file.)
     */
    private String path;

    /**
     * A map containing parameters needed to run the test. (This comes directly from the criteria
     * file.) These parameters are optional, and are sent to the script when it starts.
     */
    private Map<String, Object> parameters = Collections.emptyMap();

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public ScriptTestNode() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        checkFrozen();
        this.path = path;
    }

    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void setParameters(Map<String, Object> parameters) {
        checkFrozen();
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "ScriptTestNode(deduction=" + getDeduction() + ", " +
                "description=" + getDescription() +
                ")";
    }

    @Override
    public boolean shouldPass(File parent, SubmittedFile target, Path submissionDir,
                              Criteria criteria, Document transcript, Document notes)
            throws CannotBeAutomatedException, AutomationFailureException {
        Path scriptPath = criteria.scripts.get(path);
        if (scriptPath == null)
            throw new AutomationFailureException("could not find script: " + path);

        Path socratesPyPath;
        try {
            socratesPyPath = Globals.extractOrGetFile(Paths.get("socrates.py"));
        } catch (IOException x) {
            throw new AutomationFailureException("could not locate socrates.py");
        }

        ProcessBuilder builder = new ProcessBuilder(
                Globals.interpreter.path.toString(),
                // turns off writing bytecode files (.py[co])
                "-B",
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
                System.getProperty("path.separator") + socratesPyPath.toString()
        );

        Path parentDir = target.getPath().getParent();
        builder.directory(parentDir.toFile());

        ObjectMapper mapper = new ObjectMapper();
        Process process;

        int exitCode;
        try {
            process = builder.start();

            Map<String, Object> params = new HashMap<>(parameters);
            params.put("target_full_path", target.getPath().toString());
            params.put("student_name", submissionDir.getFileName().toString());

            params.put("static_path", criteria.getStaticDir().toString());

            mapper.writeValue(process.getOutputStream(), params);

            exitCode = process.waitFor();

        } catch (IOException | InterruptedException x) {
            throw new AutomationFailureException(x);
        }

        if (exitCode != 0)
            throw new AutomationFailureException("script exited abnormally");

        Map<String, Object> response;
        try {
            response = mapper.readValue(process.getInputStream(), Map.class);
        } catch (IOException x) {
            throw new AutomationFailureException(x);
        }

        if (isErrorResponse(response)) {
            String errorType = (String) response.get("error_type");
            String errorMessage = (String) response.get("error_message");

            String reason = "script raised error: " + errorType + ": " + errorMessage;
            appendToDocument(transcript, reason + "\n");
            throw new CannotBeAutomatedException(reason);
        }

        String transcriptStr = (String) response.get("transcript");
        if (transcriptStr != null) {
            if (!transcriptStr.endsWith("\n")) transcriptStr += "\n";

            appendToDocument(transcript, transcriptStr);
        }

        String notesStr = (String) response.get("notes");
        if (notesStr != null) {
            appendToDocument(notes, notesStr);
        }

        return (boolean) response.get("should_pass");
    }

    private boolean isErrorResponse(Map<String, Object> response) {
        return response.containsKey("error") && (boolean) response.get("error");
    }
}
