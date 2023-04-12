package com.amore.task.model;

import com.amore.task.enums.ResultStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ResultMessage implements Serializable {
    ResultStatus resultStatus;
    String message;

    public void setResult(ResultStatus resultStatus, String message) {
        this.resultStatus = resultStatus;
        this.message = message;
    }
}
