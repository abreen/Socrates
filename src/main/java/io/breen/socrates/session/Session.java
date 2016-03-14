package io.breen.socrates.session;

import java.io.Serializable;

/**
 * This class represents a "snapshot" of a grading session: it can keep track of which submissions were added to a
 * given Socrates session, the current state of tests for each file, and whatever else is necessary to continue
 * grading if Socrates were to be closed after creating this "snapshot".
 */
public class Session implements Serializable {

}
