package io.breen.socrates.view.main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by abreen on 2015-08-26.
 */
public class PropertiesList extends JPanel {

    private final List<String> keys;
    private final List<String> defaults;
    private final List<JTextField> values;

    private JPanel keysPanel;
    private JPanel valuesPanel;

    public PropertiesList(String[] keys, String[] defaults) {
        super(new BorderLayout());

        if (keys.length != defaults.length)
            throw new IllegalArgumentException("invalid defaults: length mismatch");

        this.keys = new LinkedList<>();
        this.defaults = new LinkedList<>(Arrays.asList(defaults));
        values = new LinkedList<>();

        keysPanel = new JPanel();
        keysPanel.setLayout(new BoxLayout(keysPanel, BoxLayout.PAGE_AXIS));
        keysPanel.setOpaque(false);

        valuesPanel = new JPanel();
        valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.PAGE_AXIS));
        valuesPanel.setOpaque(false);

        for (int i = 0; i < keys.length; i++)
            add(keys[i], defaults[i]);

        this.add(keysPanel, BorderLayout.LINE_START);
        this.add(valuesPanel, BorderLayout.LINE_END);
    }

    public int numItems() {
        return values.size();
    }

    public void add(String key, String value) {
        JLabel keyLabel = new JLabel(key, SwingConstants.RIGHT);
        keyLabel.setFont(Font.decode("Dialog-12"));
        keyLabel.setForeground(UIManager.getColor("inactiveCaptionText"));
        keyLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        keysPanel.add(keyLabel);
        keysPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField valueField = new JTextField(value, 13);
        valueField.setOpaque(false);
        valueField.setEditable(false);
        valueField.setBackground(new Color(0, 0, 0, 0));
        valueField.setBorder(new EmptyBorder(0, 0, 0, 0));
        valueField.setFont(Font.decode("Dialog-12"));
        valueField.setAlignmentX(Component.LEFT_ALIGNMENT);
        valuesPanel.add(valueField);
        valuesPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        values.add(valueField);
    }

    public void set(int index, String newValue) {
        values.get(index).setText(newValue);
    }

    public void reset(int index) {
        values.get(index).setText(defaults.get(index));
    }

    public void resetAll() {
        for (int i = 0; i < numItems(); i++)
            reset(i);
    }
}
