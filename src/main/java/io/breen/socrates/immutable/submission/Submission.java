package io.breen.socrates.immutable.submission;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class representing immutable objects that store the contents of a single student's submission.
 * For each actual file found on the file system, an immutable SubmittedFile object is created and
 * referenced by instances of this class.
 *
 * SubmittedFileWrapperNode objects are built using the requirements read from the criteria and
 * the contents of
 * these objects.
 */
public class Submission {

    /**
     * The name of the student who made this submission. This could be a proper name or a user name.
     * It also may simply have the same name as the submission directory.
     */
    public final String studentName;

    public final Path submissionDir;

    public final List<SubmittedFile> files;

    public Submission(String studentName, Path submissionDir) {
        this(studentName, submissionDir, new LinkedList<>());
    }

    public Submission(String studentName, Path submissionDir, List<SubmittedFile> files) {
        this.studentName = studentName;
        this.submissionDir = submissionDir;
        this.files = new LinkedList<>(files);
    }

    /**
     * Given a Path object representing a student submission directory, this method creates and
     * returns a complete Submission object representing the directory and any files inside the
     * directory.
     *
     * This method will make a complete traversal of the file tree below the specified path.
     * However, the only files that will be opened for reading are receipts, if they are present.
     *
     * @throws IOException If the traversal fails for any reason
     * @throws ReceiptFormatException If a receipt file has an invalid format
     * @see Submission
     */
    public static Submission fromDirectory(Path directory)
            throws IOException, ReceiptFormatException
    {
        if (Files.notExists(directory)) throw new IllegalArgumentException("does not exist");

        if (!Files.isDirectory(directory)) throw new IllegalArgumentException("not a directory");

        List<SubmittedFile> submittedFiles = new LinkedList<>();

        try {
            Files.walkFileTree(
                    directory, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path path, BasicFileAttributes attr)
                                throws IOException
                        {
                            if (!attr.isRegularFile() || Files.isHidden(path))
                                return FileVisitResult.CONTINUE;

                            Path localPath = directory.relativize(path);

                            if (path.getFileName().toString().endsWith(".receipt")) {
                                // only consider receipts when we look at a file
                                return FileVisitResult.CONTINUE;
                            } else {
                                // found a submitted file that is not a receipt
                                SubmittedFile submittedFile;

                                // TODO does this work on Windows?
                                Path receiptPath = Paths.get(
                                        path.toString() + ".receipt"
                                );

                                if (Files.exists(receiptPath)) {
                                    try {
                                        submittedFile = new SubmittedFile(
                                                path, localPath, receiptPath
                                        );
                                    } catch (IOException e) {
                                        throw e;
                                    } catch (ReceiptFormatException e) {
                                        throw new ReceiptFormatExceptionIO(e);
                                    }
                                } else {
                                    submittedFile = new SubmittedFile(path, localPath);
                                }

                                submittedFiles.add(submittedFile);
                            }

                            return FileVisitResult.CONTINUE;
                        }
                    }
            );
        } catch (ReceiptFormatExceptionIO exc) {
            throw exc.receiptFormatException;
        } catch (IOException exc) {
            throw exc;
        }

        return new Submission(
                directory.getFileName().toString(), directory, submittedFiles
        );
    }

    public String toString() {
        return "Submission\n" +
                "\tstudentName=" + studentName + "\n" +
                "\tsubmissionDir=" + submissionDir + "\n" +
                "\tfiles=" + files.stream()
                                  .map(SubmittedFile::toString)
                                  .collect(Collectors.joining("\n"));
    }

    /**
     * "Wrapper" exception class for ReceiptFormatException that extends IOException, so that the
     * visitFile() method used above can throw two types of exception: the standard IOException (if
     * Receipt.fromReceiptFile throws one) or ReceiptFormatException wrapped in an instance of this
     * class (if Receipt.fromReceiptFile throws a ReceiptFormatException).
     */
    private static class ReceiptFormatExceptionIO extends IOException {

        public final ReceiptFormatException receiptFormatException;

        public ReceiptFormatExceptionIO(ReceiptFormatException e) {
            this.receiptFormatException = e;
        }
    }
}
