package io.breen.socrates.test;


public class AutomationInterruptedException extends CannotBeAutomatedException {

    public AutomationInterruptedException() {
        super("test interrupted by grader");
    }
}
