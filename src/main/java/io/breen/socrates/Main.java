package io.breen.socrates;

import io.breen.socrates.controller.MainController;
import io.breen.socrates.controller.SetupController;
import org.apache.commons.cli.*;

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

        String home = System.getProperty("user.home");
        String sep = java.io.File.separator;

        String propPath = cmd.getOptionValue(
                "properties", home + sep + "socrates.properties"
        );

        try {
            loadPropertiesFrom(propPath);
        } catch (java.io.FileNotFoundException e) {
            if (cmd.hasOption("properties")) {
                System.err.println("specified .properties file does not exist");
                System.exit(2);
            } else {
                logger.info("creating new socrates.properties file at " + propPath);
                String comment = "automatically created by Socrates";
                try {
                    Globals.properties.store(
                            new java.io.FileOutputStream(propPath),
                            comment
                    );
                } catch (java.io.IOException e2) {
                    logger.warning("could not create a new .properties file");
                }
            }
        }

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

    private static void storeProperties(String path) {
        logger.fine("storing properties at " + path);
        logger.fine(Globals.properties.toString());

        try {
            java.io.FileWriter f = new java.io.FileWriter(path);
            Globals.properties.store(f, null);
        } catch (java.io.IOException e) {
            logger.severe("error storing .properties file: " + e);
            System.err.println("error storing .properties file");
            System.exit(4);
        }
    }

    private static void loadPropertiesFrom(String path)
            throws java.io.FileNotFoundException
    {
        java.io.FileInputStream f = new java.io.FileInputStream(new java.io.File(path));

        try {
            Globals.properties.load(f);
        } catch (java.io.IOException e) {
            logger.severe("error loading .properties file: " + e);
            System.err.println("error loading .properties file");
            System.exit(3);
        }

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
