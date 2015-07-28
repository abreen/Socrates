package io.breen.socrates.ui;

import io.breen.socrates.test.Deduction;

import java.io.FileNotFoundException;

public abstract class UserInput {
    public abstract void showFile(java.io.File file) throws FileNotFoundException;

    public abstract boolean promptForDeduction(Deduction deduction);
    public abstract boolean promptForDeduction(Deduction deduction, String description);

    public abstract String promptForPath(String message);

    public abstract void error(String errorMessage);

    public abstract void setup();
    public abstract void finish();
}
