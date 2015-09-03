package io.breen.socrates;

import io.breen.socrates.constructor.InvalidCriteriaException;
import io.breen.socrates.controller.MainController;
import io.breen.socrates.controller.SetupController;
import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.submission.*;
import org.apache.commons.cli.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        /*
         * Set up System properties. These are ugly, platform-specific options.
         */
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");

        /*
         * Set up default Socrates properties. These properties are the ones saved
         * to socrates.properties.
         */
        setDefaultProperties();

        /*
         * We parse the command-line arguments first, in case the user specifies a
         * different path to the .properties file.
         */

        Options opts = createOptions();

        CommandLine cmd = null;
        try {
            cmd = new DefaultParser().parse(opts, args);
        } catch (UnrecognizedOptionException e) {
            System.err.println("error: unrecognized option: " + e.getOption());
            System.exit(1);
        } catch (ParseException e) {
            logger.info("got ParseException for command-line args: " + e.toString());
            System.err.println("error parsing command-line arguments");
            System.exit(2);
        }

        if (cmd.hasOption("help")) {
            logger.info("got --help command-line option");
            new HelpFormatter().printHelp("socrates", opts);
            System.exit(0);
        }

        final Path defaultPropPath = Paths.get(
                System.getProperty("user.home"), "socrates.properties"
        );

        Path propPath;
        if (cmd.hasOption("properties")) {
            String value = cmd.getOptionValue("properties");
            logger.config("using command-line argument for properties: " + value);
            propPath = Paths.get(value);
        } else {
            logger.config("using default properties path: " + defaultPropPath);
            propPath = defaultPropPath;
        }

        if (Files.exists(propPath)) {
            try {
                loadPropertiesFrom(propPath);
            } catch (IOException e) {
                logger.severe("could not load .properties file: " + e);
                System.exit(3);
            }
        } else if (cmd.hasOption("properties")) {
            logger.severe("specified .properties file does not exist");
            System.exit(4);
        }

        /*
         * Create the MainController. It will wait for the SetupController to send it
         * a message indicating that the criteria and initial submissions have been
         * loaded.
         */
        MainController main = new MainController();

        Path criteriaPath = null;
        Criteria criteria = null;
        if (cmd.hasOption("criteria")) {
            try {
                criteriaPath = Paths.get(cmd.getOptionValue("criteria"));
                criteria = Criteria.loadFromPath(criteriaPath);
            } catch (InvalidPathException x) {
                logger.warning("command-line option for criteria path was invalid");
            } catch (IOException | InvalidCriteriaException x) {
                logger.warning(criteriaPath + " specified an invalid criteria");
            }
        }

        List<Submission> submissions = null;
        if (cmd.hasOption("submissions")) {
            String[] paths = cmd.getOptionValues("submissions");
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
                    logger.warning("I/O exception occurred adding submission: " + x);
                } catch (ReceiptFormatException x) {
                    logger.warning("invalid receipt for submission '" + p + "'");
                } catch (AlreadyGradedException x) {
                    logger.warning("skipping submission: already has a grade file: '" + p + "'");
                }
            }

            if (submissions.size() == 0) {
                submissions = null;
            }
        }

        /*
         * Start the SetupController.
         * If the --criteria command line option was specified and a Criteria object
         * could be created, that step of the setup will be skipped.
         * If the --submissions command line option was specified, and at least one
         * submission could be added, that step of the setup will be skipped.
         */
        SetupController setup = new SetupController(main);
        setup.start(criteriaPath, criteria, submissions);
    }

    private static void setDefaultProperties() {
        Properties defaults = new Properties();
        // no default properties
        Globals.properties = new Properties(defaults);
        logger.config("setting default properties: " + defaults.toString());
    }

    private static void storeProperties(Path path) throws IOException {
        logger.config("storing properties to: " + path);
        BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset());
        Globals.properties.store(writer, null);
    }

    private static void loadPropertiesFrom(Path path) throws IOException {
        logger.config("loading properties from: " + path);
        BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset());
        Globals.properties.load(reader);
        logger.config("loaded these properties: " + Globals.properties.toString());
    }

    private static Options createOptions() {
        Options opts = new Options();

        opts.addOption(
                Option.builder("p")
                      .longOpt("properties")
                      .hasArg()
                      .argName("path")
                      .desc("path to socrates.properties file")
                      .build()
        );

        opts.addOption(
                Option.builder("c")
                      .longOpt("criteria")
                      .hasArg()
                      .argName("path")
                      .desc("path to a criteria (.yml) file")
                      .build()
        );

        opts.addOption(
                Option.builder("s")
                      .longOpt("submissions")
                      .hasArgs()
                      .argName("paths")
                      .desc("space-separated paths to submission directories")
                      .build()
        );

        opts.addOption("h", "help", false, "print this message");

        return opts;
    }
}
