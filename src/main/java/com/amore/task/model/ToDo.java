package com.amore.task.model;

import com.amore.task.enums.TaskStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToDo implements Serializable, Cloneable {
    private static final long serialVersionUID = -6606155426665077510L;

    private long toDoNo;
    private Date executionDate;
    private long userNo;
    private Policy policy;
    private String task;
    private String description;
    private TaskStatus status;
    private long targetUserNo;

    public ToDo(long toDoNo, Date executionDate, long userNo, Policy policy, String task, String description) {
        this.toDoNo = toDoNo;
        this.executionDate = executionDate;
        this.userNo = userNo;
        this.policy = policy;
        this.task = task;
        this.description = description;
        this.status = TaskStatus.PROGRESS;
    }

    public ToDo(long toDoNo, Date executionDate, long userNo, Policy policy, String task, String description, TaskStatus status) {
        this(toDoNo, executionDate, userNo, policy, task, description);
        this.status = status;
    }

    public ToDo(ToDo toDo, long userNo) {
        this.toDoNo = toDo.getToDoNo();
        this.executionDate = toDo.getExecutionDate();
        this.userNo = userNo;
        this.policy = toDo.getPolicy();
        this.task = toDo.getTask();
        this.description = toDo.getDescription();
        this.status = toDo.getStatus();
        this.targetUserNo = 0;
    }

    @Override
    public ToDo clone() throws CloneNotSupportedException {
        return (ToDo) super.clone();
    }
}
