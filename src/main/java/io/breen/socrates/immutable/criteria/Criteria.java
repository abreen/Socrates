package io.breen.socrates.immutable.criteria;

import io.breen.socrates.constructor.InvalidCriteriaException;
import io.breen.socrates.constructor.SocratesConstructor;
import io.breen.socrates.immutable.file.File;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * An immutable class representing a criteria file (or package) containing specifications
 * of parts of the assignment and the tests that should be run. Instances of this class
 * created from criteria packages may also contain other resources that are needed to
 * execute the tests specified in a criteria file (e.g., hooks, scripts, or static
 * files).
 */
public final class Criteria {

    public static final String[] CRITERIA_FILE_EXTENSIONS = {"scf", "yml"};
    public static final String[] CRITERIA_PACKAGE_EXTENSIONS = {"scp", "zip"};

    /**
     * Human-readable assignment name (e.g., "Problem Set 1"). Cannot be null.
     */
    public final String assignmentName;

    /**
     * List of File objects created from the criteria file. These will all be instances of
     * subclasses of File, since File is abstract.
     *
     * @see File
     */
    public final List<File> files;

    /*
     * Other resources provided by a criteria package
     */

    public final List<Resource> staticResources;

    public final List<Resource> scripts;

    public final List<Resource> hooks;

    public Criteria(String name, List<File> files, List<Resource> staticResources,
                    List<Resource> scripts, List<Resource> hooks)
    {
        if (name == null) throw new IllegalArgumentException("'name' cannot be null");
        if (files == null) throw new IllegalArgumentException("'files' cannot be null");

        if (staticResources == null)
            throw new IllegalArgumentException("'staticResources' cannot be null");
        if (scripts == null)
            throw new IllegalArgumentException("'scripts' cannot be null");
        if (hooks == null)
            throw new IllegalArgumentException("'hooks' cannot be null");

        this.assignmentName = name;
        this.files = files;

        this.staticResources = staticResources;
        this.scripts = scripts;
        this.hooks = hooks;

        logger.info("constructed a criteria object: " + this);
    }

    public Criteria(String name, List<File> files) {
        this(name, files, new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
    }

    public String toString() {
        return "Criteria\n" +
                "\tassignment_name=" + assignmentName + "\n" +
                "\tstaticResources=" + staticResources.stream()
                                                      .map(f -> f.toString())
                                                      .collect(Collectors.joining("\n")) +
                "\tscripts=" + scripts.stream()
                                      .map(f -> f.toString())
                                      .collect(Collectors.joining("\n")) +
                "\thooks=" + hooks.stream()
                                  .map(f -> f.toString())
                                  .collect(Collectors.joining("\n")) +
                "\tfiles=" + files.stream()
                                  .map(f -> f.toString())
                                  .collect(Collectors.joining("\n"));
    }

    /**
     * @throws io.breen.socrates.constructor.InvalidCriteriaException
     */
    public static Criteria loadFromPath(Path path) throws IOException {
        String fileName = path.getFileName().toString();
        if (looksLikeCriteriaFile(fileName)) {
            return loadCriteriaFileFromPath(path);
        } else if (looksLikeCriteriaPackage(fileName)) {
            ZipFile zip = new ZipFile(path.toFile(), ZipFile.OPEN_READ);
            Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>)zip.entries();

            InputStream criteriaFile = null;
            List<Resource> staticResources = new LinkedList<>();
            List<Resource> scripts = new LinkedList<>();
            List<Resource> hooks = new LinkedList<>();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;

                String entryName = entry.getName();
                String entryFileName = Paths.get(entryName).getFileName().toString();
                if (looksLikeCriteriaFile(entryName)) {
                    criteriaFile = zip.getInputStream(entry);
                } else if (looksLikeStaticResource(entryName)) {
                    staticResources.add(new ZipEntryResource(entryFileName, zip, entry));
                } else if (looksLikeScriptResource(entryName)) {
                    scripts.add(new ZipEntryResource(entryFileName, zip, entry));
                } else if (looksLikeHookResource(entryName)) {
                    hooks.add(new ZipEntryResource(entryFileName, zip, entry));
                }
            }

            Yaml y = new Yaml(new SocratesConstructor(staticResources, scripts, hooks));
            Criteria c = (Criteria)y.load(criteriaFile);
            checkCriteriaObject(c);
            return c;
        } else {
            logger.warning("unable to determine criteria type from extension");
            return loadCriteriaFileFromPath(path);
        }
    }

    private static boolean looksLikeStaticResource(String fileName) {
        return Paths.get(fileName).getParent().getFileName().toString().equals("static");
    }

    private static boolean looksLikeScriptResource(String fileName) {
        return Paths.get(fileName).getParent().getFileName().toString().equals("scripts");
    }

    private static boolean looksLikeHookResource(String fileName) {
        return Paths.get(fileName).getParent().getFileName().toString().equals("hooks");
    }

    private static boolean looksLikeCriteriaFile(String fileName) {
        for (String ext : CRITERIA_FILE_EXTENSIONS)
            if (fileName.endsWith("." + ext)) return true;
        return false;
    }

    private static boolean looksLikeCriteriaPackage(String fileName) {
        for (String ext : CRITERIA_PACKAGE_EXTENSIONS)
            if (fileName.endsWith("." + ext)) return true;
        return false;
    }

    private static void checkCriteriaObject(Criteria c) {
        if (c == null) throw new InvalidCriteriaException("criteria file is empty");
    }

    private static Criteria loadCriteriaFileFromPath(Path path) throws IOException {
        Yaml y = new Yaml(new SocratesConstructor());
        Criteria c = (Criteria)y.load(Files.newBufferedReader(path));
        checkCriteriaObject(c);
        return c;
    }

    private static Logger logger = Logger.getLogger(Criteria.class.getName());
}
