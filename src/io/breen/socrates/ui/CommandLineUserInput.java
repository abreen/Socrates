package io.breen.socrates.ui;

import io.breen.socrates.test.Deduction;

import java.io.FileNotFoundException;
import java.util.Scanner;

public class CommandLineUserInput extends UserInput {

    private final Scanner input;

    public CommandLineUserInput() {
        input = new Scanner(System.in);
    }

    public void setup() {

    }

    public void finish() {

    }

    public void error(String errorMessage) {
        System.err.println(errorMessage);
    }

    public String promptForPath(String message) {
        String cwd = System.getProperty("user.dir");
        System.out.print(message + " (current directory: " + cwd + "): ");
        return input.nextLine();
    }

    public void showFile(java.io.File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine())
            System.out.println(scanner.nextLine());
    }

    public boolean promptForDeduction(Deduction deduction, String description) {
        System.out.println(description);
        System.out.println(deduction);

        Boolean response;
        do {
            response = stringToBoolean(input.nextLine());
            if (response == null)
                System.err.println("invalid string");

        } while (response == null);

        return response;
    }

    public boolean promptForDeduction(Deduction deduction) {
        return promptForDeduction(deduction, "Take this deduction?");
    }

    private Boolean stringToBoolean(String str) {
        switch (str) {
        case "y":
        case "yes":
            return true;
        case "n":
        case "no":
            return false;
        default:
            return null;
        }
    }
}
