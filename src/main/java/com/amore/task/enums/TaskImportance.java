package com.amore.task.enums;

public enum TaskImportance {
    S(0),
    A(1),
    B(2),
    C(3),
    D(4),
    NONE(-1);

    private int priority;

    TaskImportance(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
