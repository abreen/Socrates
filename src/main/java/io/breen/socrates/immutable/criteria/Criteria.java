package io.breen.socrates.immutable.criteria;

import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.file.PDFFile;
import io.breen.socrates.immutable.file.java.JavaFile;
import io.breen.socrates.immutable.file.plain.PlainFile;
import io.breen.socrates.immutable.file.python.PythonFile;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.immutable.test.implementation.any.ReviewTest;
import io.breen.socrates.immutable.test.implementation.python.FunctionEvalTest;
import io.breen.socrates.immutable.test.implementation.python.VariableEvalTest;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * An immutable class representing a criteria file (or package) containing specifications of parts
 * of the assignment and the tests that should be run. Instances of this class created from criteria
 * packages may also contain other resources that are needed to execute the tests specified in a
 * criteria file (e.g., hooks, scripts, or static files).
 */
public final class Criteria {

    public static final String[] CRITERIA_FILE_EXTENSIONS = {"scf", "yml"};
    public static final String[] CRITERIA_PACKAGE_EXTENSIONS = {"scp", "zip"};
    private static Logger logger = Logger.getLogger(Criteria.class.getName());
    /**
     * Human-readable assignment name (e.g., "Problem Set 1"). Cannot be null.
     */
    public String assignmentName;

    /*
     * Other resources provided by a criteria package
     */
    /**
     * File objects created from the criteria file. These will all be instances of subclasses of
     * File, since File is abstract. Each File's path Path is used as the key in this map.
     *
     * @see File
     */
    public List<File> files;

    public Map<String, Resource> staticResources = new HashMap<>();
    public Map<String, Resource> scriptResources = new HashMap<>();
    public Map<String, Resource> hookResources = new HashMap<>();

    public Criteria() {}

    public Criteria(String assignmentName, List<File> files) {
        this.assignmentName = assignmentName;
        this.files = files;
    }

    public static Criteria loadFromPath(Path path) throws IOException, InvalidCriteriaException {
        Criteria c = null;
        String fileName = path.getFileName().toString();

        if (looksLikeCriteriaFile(fileName)) {
            return loadCriteriaFileFromPath(path);
        } else if (looksLikeCriteriaPackage(fileName)) {
            ZipFile zip = new ZipFile(path.toFile(), ZipFile.OPEN_READ);
            Enumeration<? extends ZipEntry> entries = zip.entries();

            InputStream criteriaFile = null;
            Map<String, Resource> staticResources = new HashMap<>();
            Map<String, Resource> scriptResources = new HashMap<>();
            Map<String, Resource> hookResources = new HashMap<>();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;

                String entryPath = entry.getName();
                String entryFileName = Paths.get(entryPath).getFileName().toString();

                if (looksLikeCriteriaFile(entryPath)) {
                    criteriaFile = zip.getInputStream(entry);
                } else if (looksLikeStaticResource(entryPath)) {
                    staticResources.put(
                            entryFileName, new ZipEntryResource(entryFileName, zip, entry)
                    );
                } else if (looksLikeScriptResource(entryPath)) {
                    scriptResources.put(
                            entryFileName, new ZipEntryResource(entryFileName, zip, entry)
                    );
                } else if (looksLikeHookResource(entryPath)) {
                    hookResources.put(
                            entryFileName, new ZipEntryResource(entryFileName, zip, entry)
                    );
                }
            }

            c = loadCriteriaFileFromReader(new InputStreamReader(criteriaFile));
        } else {
            logger.warning("unable to determine criteria type from extension");
            c = loadCriteriaFileFromPath(path);
        }

        checkCriteriaObject(c);
        return c;
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

    private static void checkCriteriaObject(Criteria c) throws InvalidCriteriaException {
        if (c == null) throw new InvalidCriteriaException("criteria file is empty");
    }

    private static Criteria loadCriteriaFileFromPath(Path path)
            throws IOException, InvalidCriteriaException
    {
        return loadCriteriaFileFromReader(Files.newBufferedReader(path, Charset.defaultCharset()));
    }

    private static Criteria loadCriteriaFileFromReader(Reader reader)
            throws IOException, InvalidCriteriaException
    {
        Constructor cons = new Constructor(Criteria.class);

        cons.addTypeDescription(new TypeDescription(Criteria.class, "!criteria"));
        cons.addTypeDescription(new TypeDescription(TestGroup.class, "!group"));

        cons.addTypeDescription(new TypeDescription(ReviewTest.class, "!test:review"));

        /*
         * Plain text file type and supported tests
         */
        cons.addTypeDescription(new TypeDescription(PlainFile.class, "!file:plain"));

        /*
         * Python source code file type and supported tests
         */
        cons.addTypeDescription(new TypeDescription(PythonFile.class, "!file:python"));
        cons.addTypeDescription(
                new TypeDescription(
                        VariableEvalTest.class, "!test:python:eval:variable"
                )
        );
        cons.addTypeDescription(
                new TypeDescription(
                        FunctionEvalTest.class, "!test:python:eval:function"
                )
        );

        /*
         * Java source code file type and supported tests
         */
        cons.addTypeDescription(new TypeDescription(JavaFile.class, "!file:java"));

        /*
         * PDF file type
         */
        cons.addTypeDescription(new TypeDescription(PDFFile.class, "!file:pdf"));

        Yaml yaml = new Yaml(cons);
        Criteria c = null;
        try {
            c = yaml.loadAs(reader, Criteria.class);
            if (c == null) throw new InvalidCriteriaException("criteria file is empty");

        } catch (YAMLException x) {
            throw new InvalidCriteriaException(x.toString());
        }

        for (File f : c.files)
            f.afterConstruction();

        return c;
    }

    public File getFileByLocalPath(Path path) {
        for (File f : files) {
            Path p = Paths.get(f.path);
            if (p.equals(path)) return f;
        }

        return null;
    }

    public String toString() {
        return "Criteria\n" +
                "\tassignment_name=" + assignmentName + "\n" +
                "\tstaticResources=" + staticResources + "\n" +
                "\tscriptResources=" + scriptResources + "\n" +
                "\thookResources=" + hookResources + "\n" +
                "\tfiles=" + files + ")";
    }
}
