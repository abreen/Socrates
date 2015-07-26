package io.breen.socrates;

import java.io.FileNotFoundException;

public class ReviewTest extends Test {

    protected final java.io.File file;
    protected final String description;
    protected final Deduction deduction;

    public ReviewTest(java.io.File file, String description, Deduction deduction) {
        this.file = file;
        this.description = description;
        this.deduction = deduction;
    }

    public Deduction getDeduction() {
        return deduction;
    }

    public Deduction run() {
        try {
            Socrates.userInput.showFile(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("file not found");
        }

        boolean result = Socrates.userInput.promptForDeduction(deduction, description);

        if (result)
            return deduction;
        else
            return null;
    }
}
