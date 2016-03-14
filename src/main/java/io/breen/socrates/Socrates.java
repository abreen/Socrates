package io.breen.socrates;

import io.breen.pyfinder.PythonFinder;
import io.breen.pyfinder.PythonInterpreter;
import io.breen.pyfinder.PythonVersion;
import io.breen.socrates.controller.MainController;
import io.breen.socrates.controller.SetupController;
import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.criteria.InvalidCriteriaException;
import io.breen.socrates.submission.AlreadyGradedException;
import io.breen.socrates.submission.ReceiptFormatException;
import io.breen.socrates.submission.Submission;
import io.breen.socrates.view.View;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class Socrates extends Application {

    private static Logger logger = Logger.getLogger(Socrates.class.getName());

    @Override
    public void start(Stage stage) {
        String userHome = System.getProperty("user.home");
        Path localPropPath = null;

        Properties props = null;

        if (userHome != null) {
            localPropPath = Paths.get(userHome, ".socrates.properties");
            if (Files.exists(localPropPath) && Files.isRegularFile(localPropPath)) {
                try {
                    props = new Properties();
                    props.load(Files.newInputStream(localPropPath));
                } catch (IOException x) {
                    logger.warning("IOException loading .socrates.properties: " + x);
                }
            }
        }

        if (props == null) {
            props = new Properties(Globals.defaultProperties);
        }

        PythonInterpreter interp = null;

        String interpPathStr = props.getProperty("pythonInterpreter");
        if (interpPathStr != null) {
            try {
                interp = PythonInterpreter.fromPath(Paths.get(interpPathStr));
                logger.info("found Python interpreter again from path in .socrates.properties: " + interp);
            } catch (IOException | InterruptedException x) {
                logger.warning("exception checking Python interpreter path from .socrates.properties: " + x);
            }
        }

        if (interp == null) {
            /*
             * The properties file doesn't have a path to a valid Python interpreter, or that path is invalid;
             * we need to search for a Python interpreter now.
             */

            PythonFinder finder = new PythonFinder();
            try {
                List<PythonInterpreter> all = finder.findOrNewer(new PythonVersion(3, 2));
                if (!all.isEmpty()) {
                    interp = all.get(0);
                    logger.info("located Python interpreter: " + interp);
                    props.setProperty("pythonInterpreter", interp.path.toString());

                } else {
                    logger.warning("could not find a Python interpreter");

                    // TODO prompt user
                    return;
                }

            } catch (InterruptedException | IOException x) {
                logger.severe("encountered exception finding Python interpreter: " + x);
                return;
            }
        }

        if (localPropPath != null) {
            try {
                props.store(Files.newOutputStream(localPropPath), null);
                if (Globals.operatingSystem == Globals.OS.WINDOWS)
                    Files.setAttribute(localPropPath, "dos:hidden", true);

            } catch (IOException x) {
                logger.warning("IOException storing .socrates.properties: " + x);
            }
        }

        Parameters params = getParameters();
        Map<String, String> namedParams = params.getNamed();

        logger.info("command line arguments: " + namedParams);

        Path criteriaPath = null;
        Criteria criteria = null;
        if (namedParams.containsKey("criteria")) {
            try {
                criteriaPath = Paths.get(namedParams.get("criteria"));
                criteria = Criteria.loadFromPath(criteriaPath);
                logger.info("successfully loaded criteria from " + criteriaPath);
            } catch (InvalidPathException x) {
                logger.warning("command-line option for criteria path was invalid");
            } catch (IOException | InvalidCriteriaException x) {
                logger.warning("IOException or invalid criteria: " + x);
            }
        }

        List<Submission> submissions = null;
        if (namedParams.containsKey("submissions")) {
            String pathsStr = namedParams.get("submissions");
            String[] paths = pathsStr.split("\\s+");

            submissions = new ArrayList<>(paths.length);
            for (String str : paths) {
                Path p = null;
                try {
                    p = Paths.get(str);
                    submissions.add(Submission.fromDirectory(p));
                } catch (InvalidPathException x) {
                    logger.warning("invalid submission: '" + str + "' is not a valid path");
                } catch (IllegalArgumentException x) {
                    logger.warning("invalid submission: '" + p + "' " + x);
                } catch (IOException x) {
                    logger.warning("IOException occurred adding submission: " + x);
                } catch (ReceiptFormatException x) {
                    logger.warning("invalid receipt for submission '" + p + "'");
                } catch (AlreadyGradedException x) {
                    logger.warning("skipping submission: already has a grade file: '" + p + "'");
                }
            }

            if (submissions.size() == 0) {
                submissions = null;
            }
        } else {
            logger.info("no submissions specified in command line arguments");
        }

        //MainController main = new MainController();
        //SetupController setup = new SetupController(main);
        //setup.start(criteriaPath, criteria, submissions);

        try {
            new View().showStage();

        } catch (Exception x) {
            ButtonType exitButton;
            if (Globals.operatingSystem == Globals.OS.OSX)
                exitButton = new ButtonType("Quit");
            else
                exitButton = new ButtonType("Exit");

            Alert alert = new Alert(
                    AlertType.ERROR,
                    "An internal error occurred, and Socrates must be closed. Click Show Details for detailed " +
                            "information about the error.",
                    exitButton
            );

            alert.getDialogPane().setPrefSize(500, 300);

            StringBuilder sb = new StringBuilder();
            for (StackTraceElement el : x.getStackTrace()) {
                sb.append(el);
                sb.append("\n");
            }

            String stackTrace = sb.toString();

            String exceptionStr = x.getClass().getSimpleName();

            if (Arrays.asList('A', 'E', 'I', 'O', 'U').indexOf(exceptionStr.charAt(0)) != -1) {
                exceptionStr = "An " + exceptionStr;
            } else {
                exceptionStr = "A " + exceptionStr;
            }

            Label label = new Label(exceptionStr + " exception was thrown.");

            TextArea textArea = new TextArea(stackTrace);
            textArea.setEditable(false);
            textArea.setStyle(
                    "-fx-font-family: monospace"
            );

            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane pane = new GridPane();
            pane.setVgap(10);

            pane.add(label, 0, 0);
            pane.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(pane);

            alert.showAndWait();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
