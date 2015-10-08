package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.controller.MainController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainView extends JFrame {

    private final MainController controller;
    private final MenuBarManager menuBar;

    public SubmissionTree submissionTree;
    public FileView fileView;
    public FileInfo fileInfo;
    public TestTree testTree;
    public TestControls testControls;
    private JPanel rootPanel;
    private boolean allSaved;

    public MainView(MainController controller, MenuBarManager menuBar) {
        super("Socrates");

        this.controller = controller;
        this.menuBar = menuBar;

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setContentPane(rootPanel);

        setMinimumSize(new Dimension(800, 600));
        setSize(new Dimension(1200, 750));
        setLocationRelativeTo(null);

        if (Globals.operatingSystem == Globals.OS.OSX) Globals.enableFullScreen(this);

        allSaved = true;

        addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        if (!allSaved) {
                            int rv = JOptionPane.showConfirmDialog(
                                    MainView.this,
                                    "There are completed submissions whose grade reports are\n" +
                                            "not saved. Do you want to discard those reports?",
                                    "Discard Unsaved Grade Reports?",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE
                            );

                            if (rv != JOptionPane.YES_OPTION) return;
                        }

                        dispose();
                        System.exit(0);
                    }
                }
        );
    }

    private void createUIComponents() {
        submissionTree = new SubmissionTree(menuBar, controller, this);
        fileView = new FileView(menuBar, submissionTree);
        fileInfo = new FileInfo(menuBar, submissionTree);
        testTree = new TestTree(menuBar, submissionTree);

        /*
         * It is important that we construct the TestControls after the TestTree, since the
         * TestTree sets up test navigation actions and binds them to the menu items in the
         * MenuBarManager. Then the TestControls uses the action on the menu items to set the
         * action of the buttons on the controls.
         */
        testControls = new TestControls(menuBar, testTree, submissionTree);
    }

    public void setAllSaved(boolean saved) {
        allSaved = saved;

        if (Globals.operatingSystem == Globals.OS.OSX) {
            JRootPane root = getRootPane();
            if (saved) {
                root.putClientProperty("Window.documentModified", Boolean.FALSE);
            } else {
                root.putClientProperty("Window.documentModified", Boolean.TRUE);
            }
        }
    }
}
