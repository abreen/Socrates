package io.breen.socrates.view.main;

import io.breen.socrates.Globals;
import io.breen.socrates.immutable.file.File;
import io.breen.socrates.immutable.submission.SubmittedFile;
import io.breen.socrates.immutable.test.Test;
import io.breen.socrates.immutable.test.TestGroup;
import io.breen.socrates.immutable.test.ceiling.AtMost;
import io.breen.socrates.immutable.test.ceiling.Ceiling;
import io.breen.socrates.model.FileReport;
import io.breen.socrates.model.TestGroupNode;
import io.breen.socrates.model.TestNode;
import io.breen.socrates.view.icon.DefaultTestIcon;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;

public class TestTree {

    private JPanel rootPanel;
    private JScrollPane scrollPane;
    private JTree tree;

    private void createUIComponents() {
        tree = new JTree() {
            @Override
            public String convertValueToText(Object value, boolean selected,
                                             boolean expanded, boolean leaf, int row,
                                             boolean hasFocus)
            {
                if (value instanceof TestGroupNode) {
                    TestGroup group = ((TestGroupNode)value).testGroup;
                    return testGroupToString(group);

                } else if (value instanceof TestNode) {
                    Test test = ((TestNode)value).test;
                    return test.description;
                }

                return super.convertValueToText(
                        value, selected, expanded, leaf, row, hasFocus
                );
            }
        };

        /*
         * This ensures that only tests can be selected.
         */
        tree.setSelectionModel(
                new PredicateTreeSelectionModel(
                        path -> path.getLastPathComponent() instanceof TestNode
                )
        );

        DefaultTreeCellRenderer r = (DefaultTreeCellRenderer)tree.getCellRenderer();
        r.setLeafIcon(new DefaultTestIcon());
        r.setClosedIcon(null);
        r.setOpenIcon(null);

        tree.setShowsRootHandles(true);

        scrollPane = new JScrollPane(tree);
        if (Globals.operatingSystem == Globals.OS.OSX) {
            Border border = new LineBorder(new Color(197, 197, 197));
            scrollPane.setBorder(border);
        }
    }

    public void update(SubmittedFile submitted, File matchingFile) {
        if (matchingFile == null)
            tree.setModel(null);
        else
            tree.setModel(new FileReport(submitted, matchingFile));
    }

    private static String testGroupToString(TestGroup group) {
        Ceiling<Integer> maxNum = group.maxNum;
        Ceiling<Double> maxValue = group.maxValue;

        DecimalFormat fmt = new DecimalFormat("#.#");

        if (maxNum == Ceiling.ANY && maxValue == Ceiling.ANY) {
            return "fail any";
        } else if (maxNum != Ceiling.ANY && maxValue == Ceiling.ANY) {
            int max = ((AtMost<Integer>)maxNum).getValue();
            return "fail ≤ " + max;
        } else if (maxNum == Ceiling.ANY && maxValue != Ceiling.ANY) {
            double max = ((AtMost<Double>)maxValue).getValue();
            return "take ≤ " + fmt.format(max) + " points";
        } else {
            int maxN = ((AtMost<Integer>)maxNum).getValue();
            double maxV = ((AtMost<Double>)maxValue).getValue();

            return "fail ≤ " + maxN + " and take ≤ " + fmt.format(maxV) + " points";
        }
    }

    public void addTreeSelectionListener(TreeSelectionListener listener) {
        tree.addTreeSelectionListener(listener);
    }

    public TestNode getSelectedTestNode() {
        return (TestNode)tree.getLastSelectedPathComponent();
    }
}
