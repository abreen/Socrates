package io.breen.socrates.util;


/**
 * Classes that extend this class should support being "frozen". Freezing an object is a one-time operation, after
 * which the object cannot be modified. After freezing, attempting to change an object using a setter method results
 * in a FrozenObjectModificationException.
 *
 * Implementation note: subclasses of Freezable must call the checkFrozen() method, given in this class, in all of
 * their setter methods, in order to fully adhere to the above protocol.
 */
public abstract class Freezable {
    private boolean frozen;

    protected void checkFrozen() throws FrozenObjectModificationException {
        if (frozen) throw new FrozenObjectModificationException();
    }

    /**
     * Freezes this object. Subsequent attempts to modify this object using its setter methods will throw a
     * FrozenObjectModificationException.
     */
    public void freeze() {
        frozen = true;
    }
}
