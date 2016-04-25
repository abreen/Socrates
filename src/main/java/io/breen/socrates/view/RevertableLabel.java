package io.breen.socrates.view;

import javafx.scene.control.Label;


public class RevertableLabel extends Label {
    private String defaultText;


    public RevertableLabel() {
        setText(getDefaultText());
    }

    public void setDefaultText(String text) {
        defaultText = text;

        if (getText() == null)
            setText(text);
    }

    public String getDefaultText() {
        return defaultText;
    }

    public void revertText() {
        setText(getDefaultText());
    }
}
