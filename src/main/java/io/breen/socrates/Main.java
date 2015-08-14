package io.breen.socrates;

import io.breen.socrates.controller.MainController;
import io.breen.socrates.controller.SetupController;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Properties;
import java.util.logging.Logger;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        /*
         * Set up System properties. These are ugly, platform-specific options.
         */
        System.setProperty("apple.laf.useScreenMenuBar", "true");

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
            logger.warning(e.toString());
            System.err.println("error parsing command-line arguments");
            System.exit(1);
        }

        if (cmd.hasOption("help")) {
            new HelpFormatter().printHelp("socrates", opts);
            System.exit(0);
        }

        final Path defaultPropPath = Paths.get(
                System.getProperty("user.home"), "socrates.properties"
        );

        Path propPath;
        if (cmd.hasOption("properties")) {
            propPath = Paths.get(cmd.getOptionValue("properties"));
        } else {
            propPath = defaultPropPath;
        }

        if (Files.exists(propPath)) {
            try {
                loadPropertiesFrom(propPath);
            } catch (IOException e) {
                logger.severe("could not load .properties file: " + e);
                System.exit(2);
            }
        } else if (cmd.hasOption("properties")) {
            logger.severe("specified .properties file does not exist");
            System.exit(2);
        } else {
            // create a new .properties file
            logger.info("creating new socrates.properties file at " + propPath);
            String comment = "automatically created by Socrates";
            try {
                Globals.properties.store(
                        Files.newBufferedWriter(propPath), comment
                );
            } catch (IOException e) {
                logger.warning("could not create a new .properties file");
            }
        }

        logger.fine("starting controllers");

        /*
         * Create the MainController. It will wait for the SetupController to send it
         * a message indicating that the criteria and initial submissions have been
         * loaded.
         */
        MainController main = new MainController();

        /*
         * Start the SetupController. If the --criteria command line option was
         * specified, the "Open a criteria file" step of the SetupView step might be
         * skipped, if the criteria path is valid and the criteria file is valid.
         */
        SetupController setup = new SetupController(main);
        setup.start(cmd.getOptionValue("criteria"));
    }

    private static void setDefaultProperties() {
        Properties defaults = new Properties();
        defaults.setProperty("timezone", ZoneId.systemDefault().getId());
        Globals.properties = new Properties(defaults);
        logger.fine(defaults.toString());
    }

    private static void storeProperties(Path path) throws IOException {
        logger.fine("storing properties to: " + path);
        BufferedWriter writer = Files.newBufferedWriter(path);
        Globals.properties.store(writer, null);
    }

    private static void loadPropertiesFrom(Path path) throws IOException {
        logger.fine("loading properties from: " + path);
        BufferedReader reader = Files.newBufferedReader(path);
        Globals.properties.load(reader);
        logger.fine("loaded properties from file: " + Globals.properties.toString());
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

        opts.addOption("h", "help", false, "print this message");

        return opts;
    }
}
