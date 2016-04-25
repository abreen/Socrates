package io.breen.socrates.event;

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventType;


public class SocratesEvent extends Event {
    public static EventType<SocratesEvent> ANY = new EventType<>("SOCRATES_ANY");

    public SocratesEvent(@NamedArg("eventType") EventType<? extends Event> eventType) {
        super(eventType);
    }
}
