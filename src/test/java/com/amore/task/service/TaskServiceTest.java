package com.amore.task.service;

import com.amore.task.enums.ResultStatus;
import com.amore.task.enums.TaskImportance;
import com.amore.task.enums.TaskStatus;
import com.amore.task.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TaskServiceTest {

    @Autowired
    TaskService taskService;

    @Test
    public void createToDoTest() {
        //given
        ToDo toDo = new ToDo();
        toDo.setUserNo(1);
        toDo.setTask("테스트 업무");

        //when
        ResultMessage resultMessage = taskService.createToDo(toDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(resultMessage.getMessage()).isEqualTo("새로운 업무가 생성되었습니다.");
    }

    @Test
    public void createToDoTest_Delegation() {
        //given
        ToDo toDo = new ToDo();
        toDo.setUserNo(1);
        toDo.setTask("테스트 업무");

        taskService.createToDo(toDo);
        toDo.setTargetUserNo(2);
        toDo.setStatus(TaskStatus.DELEGATION);

        //when
        ResultMessage resultMessage = taskService.createToDo(toDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(resultMessage.getMessage()).isEqualTo("업무가 위임되었습니다.");
    }

    @Test
    public void createToDoTest_No_User_No() {
        //given
        ToDo toDo = new ToDo();

        //when
        ResultMessage resultMessage = taskService.createToDo(toDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.FAIL);
        assertThat(resultMessage.getMessage()).isEqualTo("담당자가 입력되지 않았습니다.");
    }

    @Test
    public void createToDoTest_User_No_Wrong() {
        //given
        ToDo toDo = new ToDo();
        toDo.setUserNo(10);

        //when
        ResultMessage resultMessage = taskService.createToDo(toDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.FAIL);
        assertThat(resultMessage.getMessage()).isEqualTo("등록되지 않은 담당자입니다.");
    }

    @Test
    public void createToDoTest_No_Task_Value() {
        //given
        ToDo toDo = new ToDo();
        toDo.setUserNo(1);
        toDo.setTask("");

        //when
        ResultMessage resultMessage = taskService.createToDo(toDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.FAIL);
        assertThat(resultMessage.getMessage()).isEqualTo("업무제목이 입력되지 않았습니다.");
    }

    @Test
    public void createToDoTest_Null_Task_Value() {
        //given
        ToDo toDo = new ToDo();
        toDo.setUserNo(1);
        toDo.setTask(null);

       //when
        ResultMessage resultMessage = taskService.createToDo(toDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.FAIL);
        assertThat(resultMessage.getMessage()).isEqualTo("업무제목이 입력되지 않았습니다.");
    }

    @Test
    public void createToDoTest_ToDoNo_Wrong() {
        //given
        ToDo toDo = new ToDo();
        toDo.setUserNo(1);
        toDo.setTask("테스트 업무");
        toDo.setToDoNo(1);

        //when
        ResultMessage resultMessage = taskService.createToDo(toDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.FAIL);
        assertThat(resultMessage.getMessage()).isEqualTo("변경할 업무가 존재하지 않습니다.");
    }

    @Test
    public void createToDoTest_FirstToDo() {
        //given
        ToDo toDo = new ToDo();
        toDo.setStatus(TaskStatus.PROGRESS);
        toDo.setUserNo(1);
        toDo.setTask("테스트 업무");

        //when
        ResultMessage resultMessage = taskService.createToDo(toDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(resultMessage.getMessage()).isEqualTo("새로운 업무가 생성되었습니다.");
        assertThat(toDo.getToDoNo()).isEqualTo(TaskService.globalToDoNo);
        assertThat(toDo.getExecutionDate()).isNotNull();
        assertThat(toDo.getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(toDo.getPolicy().getOrder()).isEqualTo(0);
        assertThat(toDo.getTask()).isEqualTo("테스트 업무");
        assertThat(toDo.getDescription()).isEqualTo("");
        assertThat(toDo.getStatus()).isEqualTo(TaskStatus.PROGRESS);
        assertThat(toDo.getTargetUserNo()).isEqualTo(0);
    }

    @Test
    public void createToDoTest_FirstToDoOnExecutionDate() throws ParseException {
        //given
        ToDo toDo = new ToDo();
        toDo.setStatus(TaskStatus.PROGRESS);
        toDo.setUserNo(1);
        toDo.setTask("테스트 업무1");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        toDo.setExecutionDate(simpleDateFormat.parse("2023-04-01 10:00:00"));

        taskService.createToDo(toDo);

        ToDo firstToDo = new ToDo();
        firstToDo.setStatus(TaskStatus.PROGRESS);
        firstToDo.setUserNo(1);
        firstToDo.setTask("테스트 업무2");
        firstToDo.setDescription("테스트 업무2 등록합니다.");
        Date firstExecutionDate = simpleDateFormat.parse("2023-04-02 10:00:00");
        firstToDo.setExecutionDate(firstExecutionDate);

        //when
        ResultMessage resultMessage = taskService.createToDo(firstToDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(resultMessage.getMessage()).isEqualTo("새로운 업무가 생성되었습니다.");
        assertThat(firstToDo.getToDoNo()).isEqualTo(TaskService.globalToDoNo);
        assertThat(firstToDo.getExecutionDate()).isEqualTo(firstExecutionDate);
        assertThat(firstToDo.getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(firstToDo.getPolicy().getOrder()).isEqualTo(0);
        assertThat(firstToDo.getTask()).isEqualTo("테스트 업무2");
        assertThat(firstToDo.getDescription()).isEqualTo("테스트 업무2 등록합니다.");
        assertThat(toDo.getStatus()).isEqualTo(TaskStatus.PROGRESS);
        assertThat(toDo.getTargetUserNo()).isEqualTo(0);
    }

    @Test
    public void createToDoTest_SecondToDoOnExecutionDate() throws ParseException {
        //given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date executionDate = simpleDateFormat.parse("2023-04-02 10:00:00");

        ToDo firstToDo = new ToDo();
        firstToDo.setStatus(TaskStatus.PROGRESS);
        firstToDo.setUserNo(1);
        firstToDo.setTask("테스트 업무1");
        firstToDo.setExecutionDate(executionDate);

        taskService.createToDo(firstToDo);

        ToDo secondToDo = new ToDo();
        secondToDo.setStatus(TaskStatus.PROGRESS);
        secondToDo.setUserNo(1);
        secondToDo.setTask("테스트 업무2");;
        secondToDo.setExecutionDate(executionDate);

        //when
        ResultMessage resultMessage = taskService.createToDo(secondToDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(resultMessage.getMessage()).isEqualTo("새로운 업무가 생성되었습니다.");
        assertThat(secondToDo.getToDoNo()).isEqualTo(TaskService.globalToDoNo);
        assertThat(secondToDo.getExecutionDate()).isEqualTo(executionDate);
        assertThat(secondToDo.getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(secondToDo.getPolicy().getOrder()).isEqualTo(1);
        assertThat(secondToDo.getStatus()).isEqualTo(TaskStatus.PROGRESS);
        assertThat(secondToDo.getTargetUserNo()).isEqualTo(0);
    }

    @Test
    public void getToDoListTest_NoToDoList() throws ParseException{
        //given
        ToDoSearchCondition toDoSearchCondition = new ToDoSearchCondition();
        toDoSearchCondition.setUserNo(1);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date executionDate = simpleDateFormat.parse("2023-04-02 10:00:00");
        toDoSearchCondition.setSearchDate(executionDate);

        //when
        List<ToDo> toDoList = taskService.getToDoList(toDoSearchCondition);

        //then
        assertThat(toDoList.size()).isEqualTo(0);
    }

    @Test
    public void getToDoListTest_UserNo1_ExecutionDateFirstDayOfApril_ToDoList() throws ParseException{
        //given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date executionDate = simpleDateFormat.parse("2023-04-01 10:00:00");

        ToDo firstToDo = new ToDo();
        firstToDo.setStatus(TaskStatus.PROGRESS);
        firstToDo.setUserNo(1);
        firstToDo.setTask("테스트 업무1");
        firstToDo.setExecutionDate(executionDate);

        taskService.createToDo(firstToDo);

        ToDo secondToDo = new ToDo();
        secondToDo.setStatus(TaskStatus.PROGRESS);
        secondToDo.setUserNo(1);
        secondToDo.setTask("테스트 업무2");;
        secondToDo.setExecutionDate(executionDate);

        taskService.createToDo(secondToDo);

        ToDo thirdToDo = new ToDo();
        thirdToDo.setStatus(TaskStatus.PROGRESS);
        thirdToDo.setUserNo(1);
        thirdToDo.setTask("테스트 업무3");
        thirdToDo.setExecutionDate(simpleDateFormat.parse("2023-04-02 10:00:00"));

        taskService.createToDo(thirdToDo);

        ToDoSearchCondition toDoSearchCondition = new ToDoSearchCondition();
        toDoSearchCondition.setUserNo(1);
        toDoSearchCondition.setSearchDate(executionDate);

        //when
        List<ToDo> toDoList = taskService.getToDoList(toDoSearchCondition);

        //then
        assertThat(toDoList.size()).isEqualTo(2);
        assertThat(toDoList.get(0).getTask()).isEqualTo("테스트 업무1");
        assertThat(toDoList.get(1).getTask()).isEqualTo("테스트 업무2");
    }

    @Test
    public void getToDoListTest_Delegation_ToDoList() throws ParseException, CloneNotSupportedException{
        //given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date executionDate = simpleDateFormat.parse("2023-04-01 10:00:00");

        ToDo firstToDo = new ToDo();
        firstToDo.setStatus(TaskStatus.PROGRESS);
        firstToDo.setUserNo(1);
        firstToDo.setTask("테스트 업무1");
        firstToDo.setExecutionDate(executionDate);

        taskService.createToDo(firstToDo);

        ToDo secondToDo = new ToDo();
        secondToDo.setStatus(TaskStatus.PROGRESS);
        secondToDo.setUserNo(1);
        secondToDo.setTask("테스트 업무2");;
        secondToDo.setExecutionDate(executionDate);

        taskService.createToDo(secondToDo);

        ToDo delegationToDo = firstToDo.clone();
        delegationToDo.setStatus(TaskStatus.DELEGATION);
        delegationToDo.setTargetUserNo(2);

        taskService.modifyToDo(delegationToDo);

        ToDoSearchCondition toDoSearchCondition = new ToDoSearchCondition();
        toDoSearchCondition.setUserNo(1);
        toDoSearchCondition.setSearchDate(executionDate);

        //when
        List<ToDo> toDoList = taskService.getToDoList(toDoSearchCondition);

        toDoSearchCondition.setUserNo(2);
        List<ToDo> delegatedToDoList = taskService.getToDoList(toDoSearchCondition);

        //then
        assertThat(toDoList.size()).isEqualTo(2);
        assertThat(toDoList.get(0).getTask()).isEqualTo("테스트 업무1 위임(test2)");
        assertThat(toDoList.get(1).getTask()).isEqualTo("테스트 업무2");

        assertThat(delegatedToDoList.get(0).getTask()).isEqualTo("테스트 업무1 test1");
    }

    @Test
    public void getToDoListTest_NoUserNo() throws ParseException{
        //given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date executionDate = simpleDateFormat.parse("2023-04-01 10:00:00");

        ToDo firstToDo = new ToDo();
        firstToDo.setStatus(TaskStatus.PROGRESS);
        firstToDo.setUserNo(1);
        firstToDo.setTask("테스트 업무1");
        firstToDo.setExecutionDate(executionDate);

        taskService.createToDo(firstToDo);

        ToDo secondToDo = new ToDo();
        secondToDo.setStatus(TaskStatus.PROGRESS);
        secondToDo.setUserNo(1);
        secondToDo.setTask("테스트 업무2");;
        secondToDo.setExecutionDate(executionDate);

        taskService.createToDo(secondToDo);

        ToDo thirdToDo = new ToDo();
        thirdToDo.setStatus(TaskStatus.PROGRESS);
        thirdToDo.setUserNo(1);
        thirdToDo.setTask("테스트 업무3");
        thirdToDo.setExecutionDate(simpleDateFormat.parse("2023-04-02 10:00:00"));

        taskService.createToDo(thirdToDo);

        ToDoSearchCondition toDoSearchCondition = new ToDoSearchCondition();

        //when
        List<ToDo> toDoList = taskService.getToDoList(toDoSearchCondition);

        //then
        assertThat(toDoList.size()).isEqualTo(0);
    }

    @Test
    public void getToDoListTest_SearchDate_IS_NULL() throws ParseException{
        //given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date executionDate = simpleDateFormat.parse("2023-04-01 10:00:00");

        ToDo firstToDo = new ToDo();
        firstToDo.setStatus(TaskStatus.PROGRESS);
        firstToDo.setUserNo(1);
        firstToDo.setTask("테스트 업무1");
        firstToDo.setExecutionDate(executionDate);

        taskService.createToDo(firstToDo);

        ToDo secondToDo = new ToDo();
        secondToDo.setStatus(TaskStatus.PROGRESS);
        secondToDo.setUserNo(1);
        secondToDo.setTask("테스트 업무2");;
        secondToDo.setExecutionDate(executionDate);

        taskService.createToDo(secondToDo);

        ToDo thirdToDo = new ToDo();
        thirdToDo.setStatus(TaskStatus.PROGRESS);
        thirdToDo.setUserNo(1);
        thirdToDo.setTask("테스트 업무3");
        thirdToDo.setExecutionDate(simpleDateFormat.parse("2023-04-02 10:00:00"));

        taskService.createToDo(thirdToDo);

        ToDoSearchCondition toDoSearchCondition = new ToDoSearchCondition();
        toDoSearchCondition.setUserNo(1);
        toDoSearchCondition.setSearchDate(null);

        //when
        List<ToDo> toDoList = taskService.getToDoList(toDoSearchCondition);

        //then
        assertThat(toDoList.size()).isEqualTo(0);
    }

    @Test
    public void getAllUserListTest(){
        //given

        //when
        List<User> userList = taskService.getAllUserList();

        //then
        assertThat(userList.size()).isEqualTo(5);
        assertThat(userList.get(0).getNo()).isEqualTo(1);
        assertThat(userList.get(0).getName()).isEqualTo("test1");
    }

    @Test
    public void modifyToDoTest_Modify_OtherUser() throws CloneNotSupportedException {
        //given
        ToDo toDo = new ToDo();
        toDo.setUserNo(1);
        toDo.setTask("테스트 업무1");

        taskService.createToDo(toDo);

        ToDo modifyToDo = toDo.clone();
        modifyToDo.setUserNo(2);

        //when
        ResultMessage resultMessage = taskService.modifyToDo(modifyToDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.FAIL);
        assertThat(resultMessage.getMessage()).isEqualTo("test2님은 해당 업무를 변경할 수 없습니다.");
    }

    @Test
    public void modifyToDoTest_Same_Policy() throws ParseException, CloneNotSupportedException {
        //given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date executionDate = simpleDateFormat.parse("2023-04-01 10:00:00");
        ToDo firstToDo = new ToDo();
        firstToDo.setUserNo(1);
        firstToDo.setTask("테스트 업무1");
        firstToDo.setExecutionDate(executionDate);

        taskService.createToDo(firstToDo);

        ToDo secondToDo = new ToDo();
        secondToDo.setUserNo(1);
        secondToDo.setTask("테스트 업무2");
        secondToDo.setExecutionDate(executionDate);

        taskService.createToDo(secondToDo);

        ToDo modifiedToDo = firstToDo.clone();
        Policy policy = new Policy(TaskImportance.B, 1);
        modifiedToDo.setPolicy(policy);

        //when
        ResultMessage resultMessage = taskService.modifyToDo(modifiedToDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(resultMessage.getMessage()).isEqualTo("업무 변경이 완료되었습니다.");
        assertThat(firstToDo.getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(firstToDo.getPolicy().getOrder()).isEqualTo(0);
        assertThat(secondToDo.getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(secondToDo.getPolicy().getOrder()).isEqualTo(1);
    }

    @Test
    public void modifyToDoTest_Change_Importance() throws ParseException, CloneNotSupportedException {
        //given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date executionDate = simpleDateFormat.parse("2023-04-01 10:00:00");

        for (int i=0; i<5; i++) {
            ToDo toDo = new ToDo();
            toDo.setUserNo(1);
            toDo.setTask("테스트 업무" + i);
            toDo.setExecutionDate(executionDate);

            taskService.createToDo(toDo);
        }

        ToDo modifiedToDo = taskService.toDoMap.get(3L).clone();
        Policy policy = new Policy(TaskImportance.A, 5);
        modifiedToDo.setPolicy(policy);

        //when
        ResultMessage resultMessage = taskService.modifyToDo(modifiedToDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(resultMessage.getMessage()).isEqualTo("업무 변경이 완료되었습니다.");
        assertThat(taskService.toDoMap.get(1L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(1L).getPolicy().getOrder()).isEqualTo(0);
        assertThat(taskService.toDoMap.get(2L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(2L).getPolicy().getOrder()).isEqualTo(1);
        assertThat(taskService.toDoMap.get(3L).getPolicy().getImportance()).isEqualTo(TaskImportance.A);
        assertThat(taskService.toDoMap.get(3L).getPolicy().getOrder()).isEqualTo(0);
        assertThat(taskService.toDoMap.get(4L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(4L).getPolicy().getOrder()).isEqualTo(2);
        assertThat(taskService.toDoMap.get(5L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(5L).getPolicy().getOrder()).isEqualTo(3);
    }

    @Test
    public void modifyToDoTest_Change_Order() throws ParseException, CloneNotSupportedException {
        //given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date executionDate = simpleDateFormat.parse("2023-04-01 10:00:00");

        for (int i=0; i<5; i++) {
            ToDo toDo = new ToDo();
            toDo.setUserNo(1);
            toDo.setTask("테스트 업무" + i);
            toDo.setExecutionDate(executionDate);

            taskService.createToDo(toDo);
        }

        ToDo modifiedToDo = taskService.toDoMap.get(3L).clone();
        Policy policy = new Policy(TaskImportance.B, 5);
        modifiedToDo.setPolicy(policy);

        //when
        ResultMessage resultMessage = taskService.modifyToDo(modifiedToDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(resultMessage.getMessage()).isEqualTo("업무 변경이 완료되었습니다.");
        assertThat(taskService.toDoMap.get(1L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(1L).getPolicy().getOrder()).isEqualTo(0);
        assertThat(taskService.toDoMap.get(2L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(2L).getPolicy().getOrder()).isEqualTo(1);
        assertThat(taskService.toDoMap.get(3L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(3L).getPolicy().getOrder()).isEqualTo(4);
        assertThat(taskService.toDoMap.get(4L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(4L).getPolicy().getOrder()).isEqualTo(2);
        assertThat(taskService.toDoMap.get(5L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(5L).getPolicy().getOrder()).isEqualTo(3);
    }

    @Test
    public void modifyToDoTest_Delegation_NoTargetUserNo() throws ParseException, CloneNotSupportedException {
        //given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date executionDate = simpleDateFormat.parse("2023-04-01 10:00:00");
        ToDo toDo = new ToDo();
        toDo.setUserNo(1);
        toDo.setTask("테스트 업무1");
        toDo.setExecutionDate(executionDate);

        taskService.createToDo(toDo);

        ToDo modifyToDo = toDo.clone();
        modifyToDo.setStatus(TaskStatus.DELEGATION);

        //when
        ResultMessage resultMessage = taskService.modifyToDo(modifyToDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.FAIL);
        assertThat(resultMessage.getMessage()).isEqualTo("위임할 대상자의 사용자 번호를 입력해 주세요.");
    }

    @Test
    public void modifyToDoTest_Change_ExecutionDate() throws ParseException, CloneNotSupportedException {
        //given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date executionDate = simpleDateFormat.parse("2023-04-01 10:00:00");

        for (int i=0; i<5; i++) {
            ToDo toDo = new ToDo();
            toDo.setUserNo(1);
            toDo.setTask("테스트 업무" + i);
            toDo.setExecutionDate(executionDate);

            taskService.createToDo(toDo);
        }

        ToDo modifiedToDo = taskService.toDoMap.get(3L).clone();
        modifiedToDo.setExecutionDate(simpleDateFormat.parse("2023-04-02 10:00:00"));

        //when
        ResultMessage resultMessage = taskService.modifyToDo(modifiedToDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(resultMessage.getMessage()).isEqualTo("업무 변경이 완료되었습니다.");
        assertThat(taskService.toDoMap.get(1L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(1L).getPolicy().getOrder()).isEqualTo(0);
        assertThat(taskService.toDoMap.get(2L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(2L).getPolicy().getOrder()).isEqualTo(1);
        assertThat(taskService.toDoMap.get(3L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(3L).getPolicy().getOrder()).isEqualTo(0);
        assertThat(taskService.userToDoListMap.get(1L).get("20230402").size()).isEqualTo(1);
        assertThat(taskService.toDoMap.get(4L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(4L).getPolicy().getOrder()).isEqualTo(2);
        assertThat(taskService.toDoMap.get(5L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(5L).getPolicy().getOrder()).isEqualTo(3);
    }

    @Test
    public void modifyToDoTest_Delegation_NoExistTargetUserNo() throws ParseException, CloneNotSupportedException {
        //given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date executionDate = simpleDateFormat.parse("2023-04-01 10:00:00");
        ToDo toDo = new ToDo();
        toDo.setUserNo(1);
        toDo.setTask("테스트 업무1");
        toDo.setExecutionDate(executionDate);

        taskService.createToDo(toDo);

        ToDo modifyToDo = toDo.clone();
        modifyToDo.setStatus(TaskStatus.DELEGATION);
        modifyToDo.setTargetUserNo(10);

        //when
        ResultMessage resultMessage = taskService.modifyToDo(modifyToDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.FAIL);
        assertThat(resultMessage.getMessage()).isEqualTo("위임할 대상자가 존재하지 않습니다.");
    }

    @Test
    public void modifyToDoTest_DelegatedToDo_Try_Delegation() {
        //given
        ToDo toDo = new ToDo();
        toDo.setUserNo(1);
        toDo.setTask("테스트 업무");

        taskService.createToDo(toDo);
        toDo.setTargetUserNo(2);
        toDo.setStatus(TaskStatus.DELEGATION);

        taskService.modifyToDo(toDo);

        ToDo delegateToDo = taskService.toDoMap.get(taskService.globalToDoNo);

        delegateToDo.setStatus(TaskStatus.DELEGATION);
        delegateToDo.setTargetUserNo(3);

        //when
        ResultMessage resultMessage = taskService.modifyToDo(delegateToDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.FAIL);
        assertThat(resultMessage.getMessage()).isEqualTo("위임받은 업무는 재위임할 수 없습니다.");
    }

    @Test
    public void modifyToDoTest_Delegation() {
        //given
        ToDo toDo = new ToDo();
        toDo.setUserNo(1);
        toDo.setTask("테스트 업무");

        taskService.createToDo(toDo);
        toDo.setTargetUserNo(2);
        toDo.setStatus(TaskStatus.DELEGATION);
        //when
        ResultMessage resultMessage = taskService.modifyToDo(toDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(resultMessage.getMessage()).isEqualTo("업무 변경이 완료되었습니다.");
        assertThat(taskService.delegationRelationMap.get(toDo.getToDoNo()).size()).isEqualTo(2);
        assertThat(taskService.delegatedToDoMap.get(taskService.globalToDoNo)).isEqualTo(toDo.getToDoNo());
        assertThat(taskService.toDoMap.get(taskService.globalToDoNo).getStatus()).isEqualTo(TaskStatus.DELEGATION);
    }

    @Test
    public void modifyToDoTest_Cancel_DelegatedToDo() {
        //given
        ToDo toDo = new ToDo();
        toDo.setUserNo(1);
        toDo.setTask("테스트 업무");

        taskService.createToDo(toDo);
        toDo.setTargetUserNo(2);
        toDo.setStatus(TaskStatus.DELEGATION);
        taskService.modifyToDo(toDo);

        ToDo delegatedToDo = taskService.toDoMap.get(taskService.globalToDoNo);
        delegatedToDo.setStatus(TaskStatus.CANCEL);

        //when
        ResultMessage resultMessage = taskService.modifyToDo(delegatedToDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(resultMessage.getMessage()).isEqualTo("업무 변경이 완료되었습니다.");
        assertThat(taskService.delegatedToDoMap.containsKey(delegatedToDo.getDescription())).isEqualTo(false);
    }

    @Test
    public void deleteToDoTest_NoToDo() {
        //given
        ToDo toDo = new ToDo();

        //when
        ResultMessage resultMessage = taskService.deleteToDo(toDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.FAIL);
        assertThat(resultMessage.getMessage()).isEqualTo("삭제할 업무가 존재하지 않습니다.");
    }

    @Test
    public void deleteToDoTest() {
        //given
        ToDo toDo = new ToDo();
        toDo.setUserNo(1);
        toDo.setTask("테스트 업무");

        taskService.createToDo(toDo);

        long toDoNo = toDo.getToDoNo();

        //when
        ResultMessage resultMessage = taskService.deleteToDo(toDo);

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(resultMessage.getMessage()).isEqualTo("업무가 삭제되었습니다.");
        assertThat(taskService.toDoMap.containsKey(toDoNo)).isEqualTo(false);
    }

    @Test
    public void deleteToDoTest_OnTheList() throws ParseException {
        //given
        //given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date executionDate = simpleDateFormat.parse("2023-04-01 10:00:00");

        for (int i=0; i<5; i++) {
            ToDo toDo = new ToDo();
            toDo.setUserNo(1);
            toDo.setTask("테스트 업무" + i);
            toDo.setExecutionDate(executionDate);

            taskService.createToDo(toDo);
        }


        //when
        ResultMessage resultMessage = taskService.deleteToDo(taskService.toDoMap.get(3L));

        //then
        assertThat(resultMessage.getResultStatus()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(resultMessage.getMessage()).isEqualTo("업무가 삭제되었습니다.");
        assertThat(taskService.toDoMap.containsKey(3L)).isEqualTo(false);
        assertThat(taskService.toDoMap.get(1L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(1L).getPolicy().getOrder()).isEqualTo(0);
        assertThat(taskService.toDoMap.get(2L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(2L).getPolicy().getOrder()).isEqualTo(1);
        assertThat(taskService.toDoMap.get(4L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(4L).getPolicy().getOrder()).isEqualTo(2);
        assertThat(taskService.toDoMap.get(5L).getPolicy().getImportance()).isEqualTo(TaskImportance.B);
        assertThat(taskService.toDoMap.get(5L).getPolicy().getOrder()).isEqualTo(3);
    }
}
