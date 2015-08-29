package io.breen.socrates.util;

public final class Left<L, R> extends Either<L, R> {

    private L value;

    public Left(L value) {
        this.value = value;
    }

    @Override
    public L getLeft() {
        return value;
    }

    public String toString() {
        return "Left(" + value + ")";
    }
}
