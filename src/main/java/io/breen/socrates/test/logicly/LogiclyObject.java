package io.breen.socrates.test.logicly;


import java.util.UUID;

public class LogiclyObject implements Comparable<LogiclyObject> {

    public final UUID uuid;

    public LogiclyObject(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public int compareTo(LogiclyObject o) {
        return this.uuid.compareTo(o.uuid);
    }
}
