package io.breen.socrates;

import java.io.FileNotFoundException;

public abstract class UserInput {
    public abstract void showFile(java.io.File file) throws FileNotFoundException;
    public abstract boolean promptForDeduction(Deduction deduction);
    public abstract boolean promptForDeduction(Deduction deduction, String description);
}
