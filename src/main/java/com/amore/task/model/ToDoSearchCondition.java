package com.amore.task.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToDoSearchCondition {
    long userNo;
    Date searchDate;
}
