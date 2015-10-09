package io.breen.socrates.view.main;

import io.breen.socrates.Globals;

import javax.swing.*;

public class MenuBarManager {

    public final JMenuBar menuBar;

    //public final JMenu sessionMenu;
    public final JMenu submissionMenu;
    public final JMenu fileMenu;
    public final JMenu testMenu;

    /*
    public final JMenuItem openSession;         // TODO
    public final JMenuItem saveSession;         // TODO
    */

    public final JMenuItem nextSubmission;
    public final JMenuItem previousSubmission;
    public final JMenuItem revealSubmission;
    public final JMenuItem saveGradeReport;
    public final JMenuItem saveGradeReportAs;

    public final JMenuItem nextFile;
    public final JMenuItem previousFile;
    //    public final JMenuItem renameFile;          // TODO
    public final JMenuItem openFile;

    public final JMenuItem passTest;
    public final JMenuItem failTest;
    public final JMenuItem resetTest;
    public final JMenuItem resetAllTests;
    public final JMenuItem nextTest;
    public final JMenuItem previousTest;
    public final JMenuItem clearNotes;
    public final JMenuItem focusOnNotes;


    public MenuBarManager() {
        menuBar = new JMenuBar();

        /*
         * The Session menu contains options for saving the current session or
         * opening a previously saved session.
         */
        /*
        sessionMenu = new JMenu("Session");

        openSession = new JMenuItem("Open Saved Session...");
        saveSession = new JMenuItem("Save Current Session...");

        sessionMenu.add(openSession);
        sessionMenu.add(saveSession);
        */


        /*
         * The Submission menu contains items for moving between submissions,
         * marking submissions as skipped, un-marking submissions as graded, and
         * revealing submission files in the file system.
         */
        submissionMenu = new JMenu("Submission");

        nextSubmission = new JMenuItem("Next Submission");
        previousSubmission = new JMenuItem("Previous Submission");

        revealSubmission = new JMenuItem();
        switch (Globals.operatingSystem) {
        case OSX:
            revealSubmission.setText("Reveal in Finder");
            break;
        case WINDOWS:
            revealSubmission.setText("Reveal in Explorer");
            break;
        default:
            revealSubmission.setText("Reveal in File System");
        }

        saveGradeReport = new JMenuItem("Save Grade Report");
        saveGradeReportAs = new JMenuItem("Save Grade Report As...");

        submissionMenu.add(nextSubmission);
        submissionMenu.add(previousSubmission);
        submissionMenu.addSeparator();
        submissionMenu.add(revealSubmission);
        submissionMenu.addSeparator();
        submissionMenu.add(saveGradeReport);
        submissionMenu.add(saveGradeReportAs);


        /*
         * The File menu contains options for the currently selected file in the
         * currently selected submission.
         */
        fileMenu = new JMenu("File");

        nextFile = new JMenuItem("Next File");
        previousFile = new JMenuItem("Previous File");

        //        renameFile = new JMenuItem("Rename File...");

        openFile = new JMenuItem();
        switch (Globals.operatingSystem) {
        case OSX:
            openFile.setText("Open in Default Application");
            break;
        default:
            openFile.setText("Open with Default Program");
        }

        fileMenu.add(nextFile);
        fileMenu.add(previousFile);
        fileMenu.addSeparator();
        //        fileMenu.add(renameFile);
        //        fileMenu.addSeparator();
        fileMenu.add(openFile);


        /*
         * The Test menu contains options for the currently selected test in the
         * test tree.
         */
        testMenu = new JMenu("Test");

        passTest = new JMenuItem("Pass Test");
        failTest = new JMenuItem("Fail Test");
        resetTest = new JMenuItem("Reset Test");
        resetAllTests = new JMenuItem("Reset All Tests");

        nextTest = new JMenuItem("Next Test");
        previousTest = new JMenuItem("Previous Test");

        clearNotes = new JMenuItem("Clear Notes");
        focusOnNotes = new JMenuItem("Focus on Notes");

        testMenu.add(passTest);
        testMenu.add(failTest);
        testMenu.add(resetTest);
        testMenu.add(resetAllTests);
        testMenu.addSeparator();
        testMenu.add(nextTest);
        testMenu.add(previousTest);
        testMenu.addSeparator();
        testMenu.add(clearNotes);
        testMenu.add(focusOnNotes);
    }

    public void setView(MainView view) {
        //menuBar.add(sessionMenu);
        menuBar.add(submissionMenu);
        menuBar.add(fileMenu);
        menuBar.add(testMenu);

        view.setJMenuBar(menuBar);
    }
}
