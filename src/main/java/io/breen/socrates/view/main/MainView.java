package io.breen.socrates.view.main;

import io.breen.socrates.Globals;

import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {

    private final MenuBarManager menuBar;

    public SubmissionTree submissionTree;
    public FileView fileView;
    public FileInfo fileInfo;
    public TestTree testTree;
    public TestControls testControls;
    private JPanel rootPanel;

    public MainView(MenuBarManager menuBar) {
        super("Socrates");

        this.menuBar = menuBar;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setContentPane(rootPanel);

        setMinimumSize(new Dimension(800, 600));
        setSize(new Dimension(1200, 750));
        setLocationRelativeTo(null);

        if (Globals.operatingSystem == Globals.OS.OSX) Globals.enableFullScreen(this);
    }

    private void createUIComponents() {
        submissionTree = new SubmissionTree(menuBar);
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
}
