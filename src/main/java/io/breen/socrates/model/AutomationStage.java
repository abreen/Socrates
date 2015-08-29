package io.breen.socrates.model;

/**
 * If a TestWrapperNode wraps an automatable test, it will be in one of the following stages,
 * depending on whether the automation has started, ended normally, or ended abnormally.
 */
public enum AutomationStage {
    NONE, STARTED, FINISHED_NORMAL, FINISHED_ERROR
}
