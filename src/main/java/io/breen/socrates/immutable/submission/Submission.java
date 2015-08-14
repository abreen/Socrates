package io.breen.socrates.immutable.submission;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Class representing immutable objects that store the contents of a single student's
 * submission. For each actual file found on the file system, an immutable SubmittedFile
 * object is created and referenced by instances of this class.
 *
 * GradeReport objects are built using the requirements read from the criteria and the
 * contents of these objects.
 */
public class Submission {

    /**
     * The name of the student who made this submission. This could be a proper name or a
     * user name.
     */
    private final String studentName;

    private final List<SubmittedFile> files;

    public Submission(String studentName) {
        this.studentName = studentName;
        this.files = new LinkedList<>();
    }

    public Submission(String studentName, List<SubmittedFile> files) {
        this.studentName = studentName;
        this.files = new LinkedList<>(files);
    }

    public static Submission fromDirectory(java.io.File directory) {
        if (!directory.isDirectory())
            throw new IllegalArgumentException("not a directory");

        List<SubmittedFile> submittedFiles = new LinkedList<>();

        createSubmittedFiles(submittedFiles, directory, "");

        return new Submission(directory.getName(), submittedFiles);
    }

    /**
     * TODO this should not be recursive (just in case)
     * @throws StackOverflowError
     */
    private static void createSubmittedFiles(List<SubmittedFile> list, java.io.File dir,
                                             String pathPrefix)
    {
        for (java.io.File file : dir.listFiles()) {
            if (file.isFile()) {
                // check for receipt
                String name = file.getName();

                Path receiptPath = Paths.get(name + ".receipt");
                if (Files.exists(receiptPath)) {
                    // TODO
                }

                SubmittedFile sf = new SubmittedFile(file, pathPrefix + name);
                list.add(sf);

            } else if (file.isDirectory()) {
                createSubmittedFiles(list, file, pathPrefix + file.getName() + java.io.File.separator);
            }
        }
    }
}
