package com.amore.task.model;

import com.amore.task.enums.TaskImportance;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Policy {
    private TaskImportance importance;
    private int order;

    public Policy() {
        this.importance = TaskImportance.NONE;
        this.order = 0;
    }

    public Policy(TaskImportance importance, int order) {
        this.importance = importance;
        this.order = order;
    }
}
