package io.breen.socrates.presenter;


import io.breen.socrates.Globals;
import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.criteria.InvalidCriteriaException;
import io.breen.socrates.view.cell.SubmissionListCell;
import io.breen.socrates.watcher.DynamicFileTreeItem;
import io.breen.socrates.session.SavedSession;
import io.breen.socrates.util.Pair;
import io.breen.socrates.view.*;
import io.breen.socrates.view.cell.FileTreeCell;
import io.breen.socrates.watcher.DynamicFileTreeWatcher;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.NavigationActions.SelectionPolicy;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;


public class SessionPresenter implements Initializable {

    private static Logger logger = Logger.getLogger(SessionPresenter.class.getName());

    /*
     * Menu items
     */
    @FXML private MenuItem newSession;
    @FXML private MenuItem saveSession;
    @FXML private MenuItem openSession;

    @FXML private MenuItem addSubmissions;
    @FXML private MenuItem addSubmissionsFromZIP;
    @FXML private MenuItem nextSubmission;
    @FXML private MenuItem previousSubmission;
    @FXML private MenuItem removeSubmission;
    @FXML private MenuItem revealSubmission;

    @FXML private CheckMenuItem autoSaveReports;
    @FXML private MenuItem changeReportName;
    @FXML private MenuItem saveReport;
    @FXML private MenuItem saveReportAs;
    @FXML private MenuItem openReport;

    @FXML private MenuItem nextFile;
    @FXML private MenuItem previousFile;
    @FXML private MenuItem openFile;

    @FXML private MenuItem passTest;
    @FXML private MenuItem failTest;
    @FXML private MenuItem resetTest;
    @FXML private MenuItem resetAllTests;
    @FXML private MenuItem nextTest;
    @FXML private MenuItem previousTest;
    @FXML private MenuItem clearNotes;

    @FXML private MenuItem about;
    @FXML private MenuItem console;


    /*
     * Containers
     */
    @FXML private BorderPane root;
    @FXML private SplitPane splitPane;
    @FXML private TitledPane submissionsTitledPane;


    /*
     * Main controls
     */
    @FXML private TreeView testTree;
    @FXML private CodeArea codeArea;
    @FXML private ListView<Path> submissionList;
    @FXML private TreeView<Path> fileTree;
    @FXML private ListView missingList;
    @FXML private TextArea transcriptArea;


    /*
     * Info tab
     */
    @FXML private RevertableLabel testKind;
    @FXML private RevertableLabel testPoints;
    @FXML private RevertableLabel testIsAutomated;


    /*
     * Notes tab
     */
    @FXML private TextArea notesArea;
    @FXML private Button notesClear;
    @FXML private Button notesSave;


    /*
     * Controls tab
     */
    @FXML private Button passButton;
    @FXML private Button failButton;
    @FXML private Button resetButton;


    /*
     * Version tab
     */
    @FXML private ListView versions;
    @FXML private Button openVersion;

    private String reportFileName = Globals.DEFAULT_REPORT_FILE_NAME;

    /**
     * The stage associated with this presenter, if a stage was created for it. This field may be null if an instance
     * of SessionPresenter was created, but not in the context of creating a SessionStage instance.
     */
    private SessionStage stage;

    private Criteria criteria;

    private ObservableList<Path> addedSubmissions;

    /**
     * The Path for the file currently being viewed in the CodeArea.
     */
    private Path currentFile;

    private DynamicFileTreeWatcher submissionWatcher;


    public SessionPresenter() {
        addedSubmissions = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        {
            Button[] buttons = {passButton, failButton, resetButton};
            MenuItem[] items = {passTest, failTest, resetTest};

            for (int i = 0; i < buttons.length; i++)
                buttons[i].setText(buttons[i].getText() + " (" + items[i].getAccelerator().getDisplayText() + ")");
        }

        try {
            submissionWatcher = new DynamicFileTreeWatcher();
        } catch (IOException x) {
            throw new RuntimeException("failed initializing DynamicFileTreeWatcher", x);
        }

        fileTree.setShowRoot(false);
        fileTree.setCellFactory(tree -> new FileTreeCell());

        fileTree.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        codeArea.clear();
                        currentFile = null;
                    } else {
                        Path p = newValue.getValue();
                        if (putFile(p, codeArea)) {
                            codeArea.start(SelectionPolicy.CLEAR);
                            currentFile = p;
                        } else {
                            codeArea.clear();
                            currentFile = null;
                        }
                    }
                }
        );

        submissionList.setItems(addedSubmissions);
        submissionList.setCellFactory(listView -> new SubmissionListCell());

        submissionList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    DynamicFileTreeItem item = submissionWatcher.getTreeItemFromPath(newValue);
                    fileTree.setRoot(item);
                }
        );

        addedSubmissions.addListener((ListChangeListener<Path>) c -> {
            // why, JavaFX, do I have to do this...?
            c.next();

            for (Path path : c.getRemoved())
                submissionWatcher.stopWatching(path);

            for (Path p : c.getAddedSubList()) {
                DynamicFileTreeItem root;
                try {
                    submissionWatcher.startWatching(p);
                } catch (IOException x) {
                    throw new RuntimeException("failed creating DynamicFileTreeItem", x);
                }
            }
        });

        addedSubmissions.addListener((ListChangeListener<Path>) c -> {
            c.next();
            int size = addedSubmissions.size();

            if (size != 0)
                submissionsTitledPane.setText("Submissions (" + addedSubmissions.size() + ")");
            else
                submissionsTitledPane.setText("Submissions");
        });
    }

    private static boolean putFile(Path file, CodeArea area) {
        try {
            String content = FileUtils.readFileToString(file.toFile());
            area.clear();
            area.appendText(content);
            return true;
        } catch (IOException x) {
            ExceptionAlert a = new ExceptionAlert(x, "The file could not be opened.");
            a.show();
            return false;
        }
    }

    @FXML
    private void showAbout(ActionEvent e) {
        AboutStage s = AboutStage.getInstance();
        s.show();
        s.toFront();
    }

    @FXML
    private void newSession(ActionEvent e) {
        SessionStage stage = new SessionStage();
        stage.show();
        stage.getPresenter().openCriteria();
    }

    public boolean openCriteria() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Criteria");

        setHomeOrDownloads(chooser);

        chooser.getExtensionFilters().addAll(
                new ExtensionFilter("Criteria file", "*.yml", "*.scf"),
                new ExtensionFilter("Criteria package", "*.zip", "*.scp")
        );

        File f = chooser.showOpenDialog(stage);
        if (f == null) {
            // dialog was cancelled
            return false;
        }

        try {
            setCriteria(Criteria.loadFromPath(f.toPath()));
        } catch (IOException | InvalidCriteriaException x) {
            ExceptionAlert a = new ExceptionAlert(x, "The criteria file or package could not be opened.");
            return false;
        }

        return true;
    }

    /**
     * Given a FileChooser or DirectoryChooser object, set the chooser's initial directory to the user's home
     * directory, or the user's Downloads directory, if it exists in the home directory.
     *
     * @param o A FileChooser or DirectoryChooser
     * @see javafx.stage.DirectoryChooser
     * @see javafx.stage.FileChooser
     */
    private static void setHomeOrDownloads(Object o) {
        String home = System.getProperty("user.home");
        if (home == null)
            return;

        File initial;
        Path downloads = Paths.get(home, "Downloads");

        if (Files.exists(downloads)) {
            initial = downloads.toFile();
        } else {
            initial = new File(home);
        }

        if (o instanceof FileChooser) {
            FileChooser chooser = (FileChooser) o;
            chooser.setInitialDirectory(initial);
        } else if (o instanceof DirectoryChooser) {
            DirectoryChooser chooser = (DirectoryChooser) o;
            chooser.setInitialDirectory(initial);
        }
    }

    public void enable() {
        splitPane.setDisable(false);
    }

    @FXML
    private void openSession(ActionEvent e) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Session");

        File f = chooser.showOpenDialog(null);
        if (f == null)
            return;

        try {
            SavedSession saved = SavedSession.load(f);
            SessionStage stage = new SessionStage();
            saved.apply(stage.getPresenter());
            stage.show();

        } catch (IOException | InvalidCriteriaException x) {
            Alert a = new Alert(AlertType.ERROR);
            a.setContentText("The session file you selected could not be opened.");
            a.showAndWait();
        }
    }

    @FXML
    public void saveSession(ActionEvent e) {
        final String fileName = "Socrates " + new Date() + ".sss";

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Session");
        chooser.setInitialFileName(fileName);

        File f = chooser.showSaveDialog(stage);
        if (f == null)
            // user cancelled save
            return;

        try {
            new SavedSession(this).save(f);
        } catch (IOException x) {
            f.delete();

            ExceptionAlert a = new ExceptionAlert(x, "The session could not be saved.");
            a.showAndWait();
            return;
        }
    }

    public BorderPane getRoot() {
        return root;
    }

    public SessionStage getStage() {
        return stage;
    }

    public void setStage(SessionStage stage) {
        this.stage = stage;
    }

    public boolean needsSave() {
        return false;
    }

    public void disable() {
        splitPane.setDisable(true);
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria c) {
        criteria = c;

        stage.setTitle(criteria.assignmentName);
        enable();
    }

    @FXML
    private void openCurrentFile(ActionEvent e) {
        if (currentFile != null)
            try {
                Desktop.getDesktop().open(currentFile.toFile());
            } catch (IOException ignored) {
            }
    }

    @FXML
    private void removeSubmission(ActionEvent e) {
        Path p = submissionList.getSelectionModel().getSelectedItem();
        if (p == null)
            return;

        addedSubmissions.remove(p);

        logger.info("removed submission: " + p);
    }

    @FXML
    private void addSubmissions(ActionEvent e) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Add Submissions");

        setHomeOrDownloads(chooser);

        File dir = chooser.showDialog(stage);
        if (dir == null) {
            // dialog was cancelled
            return;
        }

        Path enclosingDir = dir.toPath();

        DirectoryStream<Path> children;
        try {
            children = Files.newDirectoryStream(enclosingDir);
        } catch (IOException x) {
            ExceptionAlert a = new ExceptionAlert(x, "The directory you selected could not be opened.");
            a.showAndWait();
            return;
        }

        int sizeBefore = addedSubmissions.size();
        List<Pair<Path, String>> errors = addAllSubmissions(children);
        int sizeAfter = addedSubmissions.size();

        int numAdded = sizeAfter - sizeBefore;

        if (numAdded > 0) {
            // one or more submissions were added
            FXCollections.sort(addedSubmissions);
        }

        if (!errors.isEmpty()) {
            SubmissionsErrorsAlert a = new SubmissionsErrorsAlert(numAdded, errors);
            a.showAndWait();
        }
    }

    public List<Pair<Path, String>> addAllSubmissions(Iterable<Path> submissions) {
        List<Pair<Path, String>> errors = new LinkedList<>();

        for (Path p : submissions) {
            String issue;
            if ((issue = addSubmission(p)) != null) {
                errors.add(new Pair<>(p, issue));
                continue;
            }
        }

        return errors;
    }

    /**
     * Adds a single submission to the submissions list. The path must be a path to an existing directory. If
     * the path doesn't refer to an existing directory, or if the submissions list already contains the path,
     * a string containing an error message is returned. If adding the submission was a success, null is returned.
     *
     * @param p Path to a submission directory
     * @return null on success, or a string containing an error message
     */
    public String addSubmission(Path p) {
        if (!Files.exists(p))
            return "does not exist";
        if (!Files.isDirectory(p))
            return "not a directory";
        if (addedSubmissions.contains(p))
            return "already added";

        addedSubmissions.add(p);
        return null;
    }

    public void checkForReload(Path modified) {
        if (modified.equals(currentFile)) {
            putFile(currentFile, codeArea);
        }
    }

    public void checkForSubmissionDeletion(Path name) {
        Path toRemove = null;

        for (Path p : addedSubmissions) {
            if (p.endsWith(name) && !Files.exists(p)) {
                toRemove = p;
                break;
            }
        }

        if (toRemove == null)
            return;

        Alert a = new Alert(
                AlertType.WARNING,
                "The submission directory “" + toRemove.getFileName().toString() +
                        "” was moved or deleted on the file system."
        );
        a.show();

        addedSubmissions.remove(toRemove);
    }

    @FXML
    private void showConsole(ActionEvent e) {
        ConsoleStage stage = ConsoleStage.getInstance();
        stage.show();
        stage.toFront();
    }

    @FXML
    private void changeReportName(ActionEvent e) {
        Dialog<String> d = new ReportFileNameDialog(
                reportFileName,
                Globals.DEFAULT_REPORT_FILE_NAME,
                "jsmith",
                "Problem Set 2"
        );
        reportFileName = d.showAndWait().filter(s -> !s.isEmpty()).orElse(Globals.DEFAULT_REPORT_FILE_NAME);
    }

    public ObservableList<Path> getAddedSubmissionsUnmodifiable() {
        return FXCollections.unmodifiableObservableList(addedSubmissions);
    }
}
