package com.amore.task.controller;

import com.amore.task.enums.ResultStatus;
import com.amore.task.enums.TaskStatus;
import com.amore.task.model.ResultMessage;
import com.amore.task.model.ToDo;
import com.amore.task.model.ToDoSearchCondition;
import com.amore.task.model.User;
import com.amore.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.transform.Result;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping(value = "/create/todo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultMessage createToDo(@RequestBody ToDo toDo) {
        toDo.setStatus(TaskStatus.PROGRESS);
        return taskService.createToDo(toDo);
    }

    @PostMapping(value = "/get/todo/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ToDo> getToDoList(@RequestBody ToDoSearchCondition searchCondition) {
        return taskService.getToDoList(searchCondition);
    }

    @PostMapping(value = "/get/user/all", produces = {"application/json"})
    public List<User> getAllUserList() {
        return taskService.getAllUserList();
    }

    @PostMapping(value = "/modify/todo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultMessage modifyToDo(@RequestBody ToDo toDo) {
        return taskService.modifyToDo(toDo);
    }

    @PostMapping(value = "/delete/todo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultMessage deleteToDo(@RequestBody ToDo toDo) {
        return taskService.deleteToDo(toDo);
    }
}
