package io.breen.socrates.immutable.submission;

import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.file.plain.PlainFile;
import org.junit.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SubmissionTest {

    private enum SubDir {
        ALPHA(0), BETA(1), GAMMA(2), DELTA(3), EPSILON(4);

        int i;

        SubDir(int i) {
            this.i = i;
        }
    }
    private Criteria criteria;
    private Path submissionDir;
    private Path[] children;

    @Before
    public void setUp() throws Exception {
        List<File> files = new ArrayList<>(2);
        files.add(new PlainFile("ps0pr1.txt", 20.0, null, null));
        files.add(new PlainFile("ps0pr2.txt", 25.0, null, null));

        criteria = new Criteria("Problem Set 0", files);

        submissionDir = Paths.get("submissions");
        Files.createDirectory(submissionDir);

        children = new Path[SubDir.values().length];
        children[SubDir.ALPHA.i] = submissionDir.resolve("alpha");
        children[SubDir.BETA.i] = submissionDir.resolve("beta");
        children[SubDir.GAMMA.i] = submissionDir.resolve("gamma");
        children[SubDir.DELTA.i] = submissionDir.resolve("delta");
        children[SubDir.EPSILON.i] = submissionDir.resolve("epsilon");

        for (Path p : children)
            Files.createDirectory(p);

        {
            // alpha: directory with both files, with valid receipts for both
            Files.createFile(children[SubDir.ALPHA.i].resolve("ps0pr1.txt"));
            Files.createFile(children[SubDir.ALPHA.i].resolve("ps0pr2.txt"));

            Path receipt1 = children[SubDir.ALPHA.i].resolve("ps0pr1.txt.receipt");
            Path receipt2 = children[SubDir.ALPHA.i].resolve("ps0pr2.txt.receipt");
            Files.createFile(receipt1);
            Files.createFile(receipt2);

            Files.newBufferedWriter(receipt1, StandardCharsets.UTF_8)
                 .append("2015-08-14T22:46:00Z")
                 .close();
            Files.newBufferedWriter(receipt2, StandardCharsets.UTF_8)
                 .append("2015-08-14T20:12:00Z")
                 .close();
        }

        {
            // beta: directory with both files, with one invalid receipt
            Files.createFile(children[SubDir.BETA.i].resolve("ps0pr1.txt"));
            Files.createFile(children[SubDir.BETA.i].resolve("ps0pr2.txt"));

            Path receipt1 = children[SubDir.BETA.i].resolve("ps0pr1.txt.receipt");
            Path receipt2 = children[SubDir.BETA.i].resolve("ps0pr2.txt.receipt");
            Files.createFile(receipt1);
            Files.createFile(receipt2);

            Files.newBufferedWriter(receipt1, StandardCharsets.UTF_8)
                 .append("2015-08-14T22:46:00")
                 .close();
            Files.newBufferedWriter(receipt2, StandardCharsets.UTF_8).append("foo").close();
        }

        {
            // gamma: directory with one file, and no receipt
            Files.createFile(children[SubDir.GAMMA.i].resolve("ps0pr1.txt"));
        }

        {
            // delta: directory with no files, but with an invalid receipt
            Path badReceipt = children[SubDir.DELTA.i].resolve("ps0pr1.txt.receipt");
            Files.createFile(badReceipt);
            Files.newBufferedWriter(badReceipt, StandardCharsets.UTF_8).append("boo").close();
        }

        // epsilon: directory with no files
        {

        }
    }

    /**
     * Recursively deletes the file tree starting at submissionDir.
     */
    @After
    public void tearDown() throws Exception {
        Files.walkFileTree(
                submissionDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException
                    {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc)
                            throws IOException
                    {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException
                    {
                        if (exc == null) {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        } else {
                            throw exc;
                        }
                    }
                }
        );
    }

    @Test
    public void alphaShouldWork() throws Exception {
        Submission s = Submission.fromDirectory(children[SubDir.ALPHA.i]);
        assertNotNull(s);
        assertNotNull(s.files.get(0).receipt);
        assertNotNull(s.files.get(1).receipt);
    }

    @Test(expected = ReceiptFormatException.class)
    public void betaShouldThrowReceiptFormatException() throws Exception {
        Submission.fromDirectory(children[SubDir.BETA.i]);
    }

    @Test
    public void gammaShouldWorkAndHaveNoReceipt() throws Exception {
        Submission s = Submission.fromDirectory(children[SubDir.GAMMA.i]);
        assertNull(s.files.get(0).receipt);
    }

    @Test
    public void deltaShouldNotThrowReceiptException() throws Exception {
        Submission s = Submission.fromDirectory(children[SubDir.DELTA.i]);
        assertNotNull(s);
    }

    @Test
    public void epsilonShouldWorkAndHaveNoFiles() throws Exception {
        Submission s = Submission.fromDirectory(children[SubDir.EPSILON.i]);
        assertNotNull(s);
        assertEquals(0, s.files.size());
    }
}
