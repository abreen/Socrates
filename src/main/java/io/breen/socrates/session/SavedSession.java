package io.breen.socrates.session;

import io.breen.socrates.criteria.Criteria;
import io.breen.socrates.criteria.InvalidCriteriaException;
import io.breen.socrates.presenter.SessionPresenter;
import io.breen.socrates.view.SessionStage;
import javafx.stage.Window;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


public class SavedSession implements Serializable {
    /*
     * Window position and dimensions
     */
    private final Double x;
    private final Double y;
    private final Double width;
    private final Double height;

    private String criteriaPath;
    private List<String> submissions;


    public void save(File destFile) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(destFile));
        out.writeObject(this);
        out.close();
    }

    public static SavedSession load(File f) throws IOException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));

        SavedSession s;
        try {
            s = (SavedSession)in.readObject();
        } catch (ClassNotFoundException x) {
            throw new RuntimeException(x);
        }

        in.close();
        return s;
    }

    public SavedSession(SessionPresenter p) {
        SessionStage stage = p.getStage();

        x = stage == null ? null : stage.getX();
        y = stage == null ? null : stage.getY();
        width = stage == null ? null : stage.getWidth();
        height = stage == null ? null : stage.getHeight();

        criteriaPath = p.getCriteria().getPath().toString();

        // need ArrayList, since it is serializable
        submissions = p.getAddedSubmissionsUnmodifiable()
                       .stream()
                       .map(Path::toString)
                       .collect(Collectors.toCollection(ArrayList::new));
    }

    public void apply(SessionPresenter p) throws IOException, InvalidCriteriaException {
        SessionStage s = p.getStage();

        if (s != null) {
            set(s::setX, x);
            set(s::setY, y);
            set(s::setWidth, width);
            set(s::setHeight, height);
        }

        p.setCriteria(Criteria.loadFromPath(Paths.get(criteriaPath)));

        submissions.stream().map(str -> Paths.get(str)).forEach(p::addSubmission);
    }

    private static <T> void set(Consumer<T> setter, T value) {
        if (value != null)
            setter.accept(value);
    }
}
