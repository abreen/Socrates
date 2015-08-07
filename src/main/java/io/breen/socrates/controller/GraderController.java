package io.breen.socrates.controller;

import io.breen.socrates.constructor.InvalidCriteriaException;
import io.breen.socrates.constructor.SocratesConstructor;
import io.breen.socrates.controller.action.OpenCriteriaAction;
import io.breen.socrates.immutable.criteria.Criteria;
import io.breen.socrates.view.GraderView;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class GraderController {

    private Criteria criteria;
    private GraderView graderView;

    /*
     * Actions
     */
    private OpenCriteriaAction openCriteriaAction;

    public GraderController() {
        graderView = new GraderView();

        // TODO remove this
        // TODO change this to adding students
        openCriteriaAction = new OpenCriteriaAction("Open Criteria",
                                                    "Open a criteria file");

        JMenu menu = new JMenu("Criteria");
        menu.add(new JMenuItem(openCriteriaAction));
        graderView.addMenu(menu);
    }

    public void run(String criteriaPath) {
        File critFile;

        if (criteriaPath != null) {
            critFile = new File(criteriaPath);
        } else {
            critFile = null;
        }

        graderView.setVisible(true);
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Socrates criteria files", "scf", "sca"));
        chooser.setAcceptAllFileFilterUsed(false);

        while (true) {
            while (critFile == null) {
                int status = chooser.showDialog(graderView, "Open Criteria");

                if (status == JFileChooser.APPROVE_OPTION) {
                    critFile = chooser.getSelectedFile();

                } else if (status == JFileChooser.CANCEL_OPTION) {
                    return;

                } else if (status == JFileChooser.ERROR_OPTION) {
                    // dialog is dismissed or error occurs

                }
            }

            try {
                loadCriteria(critFile);
            } catch (FileNotFoundException e) {

                JOptionPane.showMessageDialog(chooser, "The specified criteria could " +
                    "not be found.",
                                              "Not Found", JOptionPane.ERROR_MESSAGE);

                critFile = null;
                continue;
            } catch (InvalidCriteriaException e) {

                JOptionPane.showMessageDialog(chooser, "There was an error opening the " +
                                                  "specified criteria.",
                                              "Error", JOptionPane.ERROR_MESSAGE);

                critFile = null;
                continue;
            }

            break;
        }

        // activate main view now
        graderView.setStatusText("Opened criteria file.");

        graderView.setTitle("Socrates — " + criteria.getName());
    }

    private void loadCriteria(File file) throws FileNotFoundException {
        Yaml yaml = new Yaml(new SocratesConstructor());
        criteria = (Criteria)yaml.load(new FileReader(file));
    }
}
