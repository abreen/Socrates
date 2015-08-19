package io.breen.socrates.constructor;

import org.yaml.snakeyaml.error.Mark;

public class InvalidCriteriaException extends RuntimeException {
    private Mark mark;

    public InvalidCriteriaException(Mark mark, String msg) {
        super(msg);
        this.mark = mark;
    }

    public InvalidCriteriaException(String msg) {
        this(null, msg);
    }

    public String toString() {
        return getMessage() + (mark != null ? mark : "");
    }
}
