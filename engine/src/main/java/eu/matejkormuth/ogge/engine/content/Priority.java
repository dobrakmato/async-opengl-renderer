package eu.matejkormuth.ogge.engine.content;

import lombok.AccessLevel;
import lombok.Getter;

public enum Priority {
    LOW(0),
    NORMAL(1),
    HIGH(2),
    CRITICAL(3);
    @Getter(AccessLevel.PACKAGE)
    private final int id;

    Priority(int id) {
        this.id = id;
    }
}