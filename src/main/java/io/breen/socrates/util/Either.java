package io.breen.socrates.util;

public abstract class Either<L, R> {

    public L getLeft() {
        return null;
    }

    public R getRight() {
        return null;
    }

    public final Object get() {
        L left = this.getLeft();
        R right = this.getRight();

        if (left != null) return left;
        else if (right != null) return right;
        else throw new RuntimeException();
    }
}
