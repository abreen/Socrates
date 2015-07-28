package io.breen.socrates.test;

public class AnySingleton extends Ceiling {
    private static AnySingleton instance = null;

    protected AnySingleton() { }

    public static AnySingleton getInstance() {
        if (instance == null)
            instance = new AnySingleton();
        return instance;
    }
}
