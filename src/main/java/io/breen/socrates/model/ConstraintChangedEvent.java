package io.breen.socrates.model;

import io.breen.socrates.util.ObservableChangedEvent;

/**
 * The event that is generated by a TestWrapperNode when its constraint state changes.
 */
public class ConstraintChangedEvent extends ObservableChangedEvent<TestWrapperNode> {

    public final boolean isNowConstrained;

    public ConstraintChangedEvent(TestWrapperNode source, boolean newState) {
        super(source);
        this.isNowConstrained = newState;
    }
}