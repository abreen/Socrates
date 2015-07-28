package io.breen.socrates.ui;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalFactory;
import io.breen.socrates.test.Deduction;

import java.io.FileNotFoundException;
import java.io.IOException;

public class GUIUserInput extends UserInput {

    private final Terminal terminal;

    public GUIUserInput() throws IOException {
        TerminalFactory factory = new DefaultTerminalFactory();
        terminal = factory.createTerminal();
    }

    public void setup() {
        // TODO
    }

    public void finish() {
        // TODO
    }

    public void error(String errorMessage) {

    }

    public String promptForPath(String message) {
        return null;
    }

    public void showFile(java.io.File file) throws FileNotFoundException {

    }

    public boolean promptForDeduction(Deduction deduction, String description) {
        return false;
    }

    public boolean promptForDeduction(Deduction deduction) {
        return false;
    }
}
