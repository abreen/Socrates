package io.breen.socrates.view;


import io.breen.socrates.Globals;
import io.breen.socrates.session.Session;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.nio.file.Path;

public class View {
    private Stage stage;

    private static void setSimpleShortcut(MenuItem item, KeyCode c) {
        item.setAccelerator(new KeyCodeCombination(c, KeyCombination.SHORTCUT_DOWN));
    }

    private static void setComplexShortcut(MenuItem item, KeyCode c) {
        item.setAccelerator(new KeyCodeCombination(c, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
    }

    private final MenuItem saveSession;
    private Path lastSaveLocation;

    public View() {
        stage = new Stage();
        stage.setTitle("Socrates");

        VBox root = new VBox();

        stage.setScene(new Scene(root, 260, 230));

        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);

        Menu menuSession = new Menu("Session");
        {
            MenuItem newSession = new MenuItem("New Session...");
            setSimpleShortcut(newSession, KeyCode.N);

            saveSession = new MenuItem("Save Session...");
            setSimpleShortcut(saveSession, KeyCode.S);

            MenuItem open = new MenuItem("Open Session...");
            setSimpleShortcut(open, KeyCode.O);

            menuSession.getItems().addAll(newSession, saveSession, open);
        }

        Menu menuSubmission = new Menu("Submission");
        {
            MenuItem next = new MenuItem("Next Submission");
            setComplexShortcut(next, KeyCode.RIGHT);

            next.setOnAction(e -> System.out.println("boop!"));

            MenuItem prev = new MenuItem("Previous Submission");
            MenuItem reveal;
            switch (Globals.operatingSystem) {
                case OSX:
                    reveal = new MenuItem("Reveal Submission in Finder");
                    break;
                case WINDOWS:
                    reveal = new MenuItem("Reveal Submission in Explorer");
                    break;
                default:
                    reveal = new MenuItem("Reveal Submission in File Manager");
            }

            menuSubmission.getItems().addAll(
                    next,
                    prev,
                    new SeparatorMenuItem(),
                    reveal
            );
        }

        Menu menuReport = new Menu("Report");
        {
            MenuItem save = new MenuItem("Save Grade Report");
            MenuItem saveAs = new MenuItem("Save Grade Report As...");
            MenuItem delete = new MenuItem("Delete Grade Report");

            MenuItem open;
            switch (Globals.operatingSystem) {
                case OSX:
                    open = new MenuItem("Open Report in Default Application");
                    break;
                default:
                    open = new MenuItem("Open Report in Default Program");
            }

            menuReport.getItems().addAll(
                    save,
                    saveAs,
                    new SeparatorMenuItem(),
                    delete,
                    new SeparatorMenuItem(),
                    open
            );
        }

        Menu menuFile = new Menu("File");
        {
            MenuItem next = new MenuItem("Next Submitted File");
            MenuItem prev = new MenuItem("Previous Submitted File");
            MenuItem open;
            switch (Globals.operatingSystem) {
                case OSX:
                    open = new MenuItem("Open File in Default Application");
                    break;
                default:
                    open = new MenuItem("Open File in Default Program");
            }

            menuFile.getItems().addAll(
                    next,
                    prev,
                    new SeparatorMenuItem(),
                    open
            );
        }

        Menu menuTest = new Menu("Test");
        {
            MenuItem pass = new MenuItem("Pass Test");
            MenuItem passNonAutomated = new MenuItem("Pass All Non-Automated");
            MenuItem fail = new MenuItem("Fail Test");
            MenuItem reset = new MenuItem("Reset Test");
            MenuItem resetAll = new MenuItem("Reset All Tests");
            MenuItem next = new MenuItem("Next Test");
            MenuItem prev = new MenuItem("Previous Test");
            MenuItem focus = new MenuItem("Focus on Notes");
            MenuItem clear = new MenuItem("Clear Notes");

            menuTest.getItems().addAll(
                    pass,
                    passNonAutomated,
                    fail,
                    reset,
                    resetAll,
                    new SeparatorMenuItem(),
                    next,
                    prev,
                    new SeparatorMenuItem(),
                    clear,
                    focus
            );
        }

        Menu menuWindow = new Menu("Window");
        {
            MenuItem credits = new MenuItem("Credits");
            MenuItem transcript = new MenuItem("Transcript");
            MenuItem console = new MenuItem("Console");

            menuWindow.getItems().addAll(
                    credits,
                    transcript,
                    new SeparatorMenuItem(),
                    console
            );
        }

        menuBar.getMenus().addAll(
                menuSession,
                menuReport,
                menuSubmission,
                menuFile,
                menuTest,
                menuWindow
        );

        root.getChildren().add(menuBar);
    }

    public View(Session s) {
        this();
        // TODO "thaw" out the session by setting state of UI components
    }

    public void showStage() {
        stage.show();
    }
}
