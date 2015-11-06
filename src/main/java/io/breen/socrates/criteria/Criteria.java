package io.breen.socrates.criteria;

import io.breen.socrates.file.File;
import io.breen.socrates.file.PDFFile;
import io.breen.socrates.file.java.*;
import io.breen.socrates.file.java.Object;
import io.breen.socrates.file.logicly.LogiclyFile;
import io.breen.socrates.file.plain.PlainFile;
import io.breen.socrates.file.python.PythonFile;
import io.breen.socrates.test.TestGroup;
import io.breen.socrates.test.any.ReviewTest;
import io.breen.socrates.test.any.ScriptTest;
import io.breen.socrates.test.java.MethodEvalTest;
import io.breen.socrates.test.logicly.CircuitEvalTest;
import io.breen.socrates.test.python.FunctionEvalTest;
import io.breen.socrates.test.python.VariableEvalTest;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A class representing a criteria file (or package) containing specifications of parts of the
 * assignment and the tests that should be run. Instances of this class created from criteria
 * packages may also contain other resources that are needed to execute the tests specified in a
 * criteria file (e.g., hooks, scripts, or static files).
 */
public class Criteria {

    public static final String[] CRITERIA_FILE_EXTENSIONS = {"scf", "yml"};
    public static final String[] CRITERIA_PACKAGE_EXTENSIONS = {"scp", "zip"};

    private static Logger logger = Logger.getLogger(Criteria.class.getName());

    /**
     * Human-readable assignment name (e.g., "Problem Set 1"). Cannot be null.
     */
    public String assignmentName;

    /**
     * File objects created from the criteria file. These will all be instances of subclasses of
     * File, since File is abstract. Each File's path Path is used as the key in this map.
     *
     * @see File
     */
    public List<File> files;

    public Map<String, Path> staticResources;
    public Map<String, Path> scripts;

    /**
     * If this criteria was created from a criteria package, this field stores the file system
     * location to which the archive was "unzipped".
     */
    private Path tempDir;

    /**
     * This empty constructor is used by SnakeYAML.
     */
    public Criteria() {}

    public Criteria(String assignmentName, List<File> files) {
        this.assignmentName = assignmentName;
        this.files = files;
    }

    public static Criteria loadFromPath(Path path) throws IOException, InvalidCriteriaException {
        String fileName = path.getFileName().toString();

        Criteria c;

        if (looksLikeCriteriaFile(fileName)) {
            return loadCriteriaFileFromPath(path);

        } else if (looksLikeCriteriaPackage(fileName)) {
            Path tempDir = Files.createTempDirectory(null);

            logger.info("using temporary directory for criteria package: " + tempDir);

            unzip(path, tempDir);

            // need this array because the variable has to be final...
            final Path[] criteriaPath = new Path[] {null};

            final Map<String, Path> staticResources = new HashMap<>();
            final Map<String, Path> scripts = new HashMap<>();

            Files.walkFileTree(
                    tempDir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException
                        {
                            String fileName = file.toString();
                            if (looksLikeCriteriaFile(fileName)) {
                                criteriaPath[0] = file;
                            } else if (looksLikeStaticResource(fileName)) {
                                staticResources.put(file.getFileName().toString(), file);
                            } else if (looksLikeScript(fileName)) {
                                scripts.put(file.getFileName().toString(), file);
                            }

                            return FileVisitResult.CONTINUE;
                        }
                    }
            );

            if (criteriaPath[0] == null)
                throw new InvalidCriteriaException("could not find criteria file in package");

            c = loadCriteriaFileFromPath(criteriaPath[0]);
            c.staticResources = staticResources;
            c.scripts = scripts;
            c.tempDir = tempDir;

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

    private static boolean looksLikeScript(String fileName) {
        return Paths.get(fileName).getParent().getFileName().toString().equals("scripts");
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

        cons.addTypeDescription(new TypeDescription(ScriptTest.class, "!test:script"));

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
         * Java source code file type, supported tests and supporting object definitions
         */
        cons.addTypeDescription(new TypeDescription(JavaFile.class, "!file:java"));
        cons.addTypeDescription(
                new TypeDescription(
                        MethodEvalTest.class, "!test:java:eval:method"
                )
        );
        cons.addTypeDescription(
                new TypeDescription(
                        Parameter.class, "!java:parameter"
                )
        );
        cons.addTypeDescription(
                new TypeDescription(
                        Type.class, "!java:type"
                )
        );
        cons.addTypeDescription(
                new TypeDescription(
                        Object.class, "!java:object"
                )
        );

        /*
         * PDF file type
         */
        cons.addTypeDescription(new TypeDescription(PDFFile.class, "!file:pdf"));

        /*
         * Logicly file type and supported tests
         */
        cons.addTypeDescription(new TypeDescription(LogiclyFile.class, "!file:logicly"));
        cons.addTypeDescription(
                new TypeDescription(
                        CircuitEvalTest.class, "!test:logicly:eval"
                )
        );

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

    private static void unzip(Path zipPath, Path destDir) throws IOException {
        ZipFile file = new ZipFile(zipPath.toFile());

        Enumeration<? extends ZipEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryName = entry.getName();
            Path entryDest = Paths.get(destDir.toString(), entryName);

            if (entry.isDirectory()) Files.createDirectory(entryDest);
            else Files.copy(file.getInputStream(entry), entryDest);
        }

        file.close();
    }

    public Path getStaticDir() {
        return Paths.get(tempDir.toString(), "static");
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
                "\tscripts=" + scripts + "\n" +
                "\tfiles=" + files + ")";
    }
}
