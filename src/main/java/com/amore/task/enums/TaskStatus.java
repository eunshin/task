package com.amore.task.enums;

public enum TaskStatus {
    PROGRESS("진행 중"),
    COMPLETE("완료"),
    CANCEL("취소"),
    DELEGATION("위임"),
    NONE("");

    private String description;

    TaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
