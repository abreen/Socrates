package io.breen.socrates.util;

public final class Right<L, R> extends Either<L, R> {

    private R value;

    public Right(R value) {
        this.value = value;
    }

    @Override
    public R getRight() {
        return value;
    }

    public String toString() {
        return "Right(" + value + ")";
    }
}
