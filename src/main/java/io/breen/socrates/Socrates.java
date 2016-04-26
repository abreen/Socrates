package io.breen.socrates;

import io.breen.pyfinder.PythonFinder;
import io.breen.pyfinder.PythonInterpreter;
import io.breen.pyfinder.PythonVersion;
import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.criteria.InvalidCriteriaException;
import io.breen.socrates.submission.AlreadyGradedException;
import io.breen.socrates.submission.ReceiptFormatException;
import io.breen.socrates.util.Pair;
import io.breen.socrates.view.ExceptionAlert;
import io.breen.socrates.view.SessionStage;
import io.breen.socrates.view.SubmissionsErrorsAlert;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public class Socrates extends Application {

    private static Logger logger = Logger.getLogger(Socrates.class.getName());

    @Override
    public void start(Stage primaryStage) {
        Thread.currentThread().setUncaughtExceptionHandler(Socrates::crash);

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

                    new Alert(
                            AlertType.ERROR,
                            "A Python 3 interpreter could not be located. " +
                                    "Socrates requires Python 3.2 or newer.",
                            ButtonType.CLOSE
                    ).showAndWait();
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

        for (String s : params.getUnnamed()) {
            if (s.equals("-h") || s.equals("--help")) {
                System.out.println("usage: socrates [--criteria=/path/to/criteria.yml [--submissions=dir1,dir2,dir3]]");
                System.exit(0);
            }
        }

        Map<String, String> namedParams = params.getNamed();

        logger.info("command line arguments: " + namedParams);

        Path criteriaPath;
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

        List<Path> submissions = new ArrayList<>();
        if (namedParams.containsKey("submissions")) {
            String pathsStr = namedParams.get("submissions");
            String[] paths = pathsStr.split(",");

            for (String str : paths) {
                try {
                    submissions.add(Paths.get(str));
                } catch (InvalidPathException x) {
                    logger.warning("invalid submission: '" + str + "' is not a valid path");
                }
            }
        } else {
            logger.info("no submissions specified in command line arguments");
        }

        try {
            SessionStage stage = new SessionStage();

            if (criteria != null) {
                stage.getPresenter().setCriteria(criteria);
                stage.show();
            } else {
                stage.show();
                stage.getPresenter().openCriteria();
            }

            if (!submissions.isEmpty()) {
                List<Pair<Path, String>> errors = stage.getPresenter().addAllSubmissions(submissions);

                if (!errors.isEmpty()) {
                    int numAdded = stage.getPresenter().getAddedSubmissionsUnmodifiable().size();
                    SubmissionsErrorsAlert a = new SubmissionsErrorsAlert(numAdded, errors);
                    a.showAndWait();
                }
            }

        } catch (Throwable t) {
            crash(Thread.currentThread(), t);
        }
    }

    private static void crash(Thread t, Throwable x) {
        ExceptionAlert a = new ExceptionAlert(
                x,
                "An error occurred, and Socrates must be closed. For more information about the error, see the " +
                        "details below."
        );
        a.showAndWait();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
