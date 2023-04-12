package com.amore.task.service;

import com.amore.task.enums.ResultStatus;
import com.amore.task.enums.TaskImportance;
import com.amore.task.enums.TaskStatus;
import com.amore.task.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskService {
    public static long globalUserNo = 0;
    public static long globalToDoNo = 0;

    public static Map<Long, User> userMap = new HashMap<>();
    public static Map<Long, ToDo> toDoMap = new HashMap<>();
    public static Map<Long, Map<String, List<Long>>> userToDoListMap = new HashMap<>();
    public static Map<Long, List<Long>> delegationRelationMap = new HashMap<>();
    public static Map<Long, Long> delegatedToDoMap = new HashMap<>();


    public TaskService() {
        String[] testUser = {"test1", "test2", "test3", "test4", "test1"};

        for (int i=0; i<5; i++) {
            globalUserNo++;

            userMap.put(globalUserNo, new User(globalUserNo, testUser[i]));
        }
    }

    public ResultMessage createToDo(ToDo newToDo) {
        ResultMessage resultMessage = new ResultMessage();

        if (!validateBasicToDo(newToDo, resultMessage)) {
            return resultMessage;
        }

        setDefaultToDo(newToDo);

        String keyDate = getKeyDate(newToDo.getExecutionDate());

        Map<String, List<Long>> dateToDoListMap = new HashMap<>();
        List<Long> toDoNoList = new ArrayList<>();

        if (!isFirstToDo(newToDo.getUserNo())) {
            dateToDoListMap = userToDoListMap.get(newToDo.getUserNo());

            if (!isFirstToDoOnExecutionData(dateToDoListMap, keyDate)) {
                toDoNoList = dateToDoListMap.get(keyDate);

                if (toDoNoList.size() > 0) {
                    setLowestPolicy(toDoNoList, newToDo);
                }
            }
        }

        setNewToDo(newToDo, keyDate, toDoNoList, dateToDoListMap);

        if (newToDo.getStatus() == TaskStatus.DELEGATION) {
            resultMessage.setResult(ResultStatus.SUCCESS, "업무가 위임되었습니다.");
        } else {
            resultMessage.setResult(ResultStatus.SUCCESS, "새로운 업무가 생성되었습니다.");
        }

        return resultMessage;
    }

    public List<ToDo> getToDoList(ToDoSearchCondition toDoSearchCondition) {
        List<ToDo> toDoList = new ArrayList<>();

        List<Long> toDoNoList = getToDoNoList(toDoSearchCondition);

        if (CollectionUtils.isEmpty(toDoNoList)) {
            return toDoList;
        }

        toDoList = getUserToDoList(toDoNoList);

        return orderToDoListByPolicy(toDoList);
    }

    private List<ToDo> getToDoList(long userNo, Date searchDate) {
        ToDoSearchCondition searchCondition = new ToDoSearchCondition();

        searchCondition.setUserNo(userNo);
        searchCondition.setSearchDate(searchDate);

        return getToDoList(searchCondition);
    }

    public List<User> getAllUserList() {
        return userMap.values().stream().collect(Collectors.toList());
    }

    public ResultMessage modifyToDo(ToDo toDo) {
        ResultMessage resultMessage= new ResultMessage();

        if (!validateBasicToDo(toDo, resultMessage)) {
            return resultMessage;
        }

        User user = userMap.get(toDo.getUserNo());
        ToDo originToDo = toDoMap.get(toDo.getToDoNo());

        if (!validateModifyToDo(originToDo, toDo, user.getName(), resultMessage)) {
            return resultMessage;
        }

        if (toDo.getStatus() == TaskStatus.DELEGATION) {
            if (!validateDelegationToDo(toDo, resultMessage)) {
                return resultMessage;
            }

            delegateToDo(toDo);
        } else if (toDo.getStatus() == TaskStatus.CANCEL && delegatedToDoMap.containsKey(toDo.getToDoNo())) {
            cancelDelegateToDo(toDo.getToDoNo());
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String executionDate = simpleDateFormat.format(toDo.getExecutionDate());
        String originExecutionDate = simpleDateFormat.format(originToDo.getExecutionDate());

        if (!executionDate.equals(originExecutionDate)) {
            changePolicy(originToDo, originExecutionDate, "remove");
            changePolicy(toDo, executionDate, "add");
        } else if (toDo.getPolicy().getImportance() != originToDo.getPolicy().getImportance()) {
            changePolicy(toDo, executionDate, "changeImportance");
            setModifiedToDo(toDo);
        } else if (toDo.getPolicy().getOrder() != originToDo.getPolicy().getOrder()) {
            changePolicy(toDo, executionDate, "changeOrder");
            setModifiedToDo(toDo);
        }else {
            setModifiedToDo(toDo);
        }

        setModifiedToDo(toDo);

        resultMessage.setResult(ResultStatus.SUCCESS, "업무 변경이 완료되었습니다.");

        return resultMessage;
    }

    public ResultMessage deleteToDo(ToDo toDo) {
        ResultMessage resultMessage = new ResultMessage();

        if (!validateDeleteToDo(toDo.getToDoNo(), resultMessage)) {
            return resultMessage;
        }

        if (isDelegatedToDo(toDo.getToDoNo())) {
            cancelDelegateToDo(toDo.getToDoNo());
        }

        String keyDate = getKeyDate(toDo.getExecutionDate());
        changePolicy(toDo, keyDate, "remove");
        toDoMap.remove(toDo.getToDoNo());
        toDo = null;

        resultMessage.setResult(ResultStatus.SUCCESS, "업무가 삭제되었습니다.");

        return resultMessage;
    }

    private boolean validateBasicToDo(ToDo toDo, ResultMessage resultMessage) {
        if (toDo.getUserNo() == 0) {
            resultMessage.setResult(ResultStatus.FAIL, "담당자가 입력되지 않았습니다.");
            return false;
        }

        if (!userMap.containsKey(toDo.getUserNo())) {
            resultMessage.setResult(ResultStatus.FAIL, "등록되지 않은 담당자입니다.");
            return false;
        }

        if (!StringUtils.hasText(toDo.getTask())) {
            resultMessage.setResult(ResultStatus.FAIL, "업무제목이 입력되지 않았습니다.");
            return false;
        }

        if (toDo.getToDoNo() > 0 && !toDoMap.containsKey(toDo.getToDoNo())) {
            resultMessage.setResult(ResultStatus.FAIL, "변경할 업무가 존재하지 않습니다.");
            return false;
        }

        if (toDo.getToDoNo() == 0 && !ObjectUtils.isEmpty(toDo.getPolicy())) {
            resultMessage.setResult(ResultStatus.FAIL, "새로운 업무는 중요도 및 우선순위를 지정할 수 없습니다.");
            return false;
        }

        return true;
    }

    private void setDefaultToDo(ToDo toDo) {
        if (!StringUtils.hasText(toDo.getDescription())) {
            toDo.setDescription("");
        }

        if (ObjectUtils.isEmpty(toDo.getExecutionDate())) {
            toDo.setExecutionDate(new Date());
        }

        Policy policy;
        if (toDo.getStatus() == TaskStatus.DELEGATION) {
            policy = new Policy(TaskImportance.A, 0);
        } else {
            policy = new Policy(TaskImportance.B, 0);
        }

        toDo.setPolicy(policy);
    }

    private boolean isFirstToDo(long userNo) {
        if (userToDoListMap.containsKey(userNo)) {
            return false;
        }

        return true;
    }

    private boolean isFirstToDoOnExecutionData(Map<String, List<Long>> dateToDoListMap, String keyDate) {
        if (dateToDoListMap.containsKey(keyDate)) {
            return false;
        }

        return true;
    }

    private String getKeyDate(Date executionDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

        return simpleDateFormat.format(executionDate);
    }

    private void setLowestPolicy(List<Long> toDoNoList, ToDo newToDo) {
        TaskImportance importance = TaskImportance.NONE;
        int order = 0;

        boolean existA0 = false;
        for (Long toDoNo : toDoNoList) {
            if (toDoMap.containsKey(toDoNo)) {
                ToDo toDo = toDoMap.get(toDoNo);

                if (importance == TaskImportance.NONE
                        || toDo.getPolicy().getImportance().getPriority() <= importance.getPriority()) {
                    importance = toDo.getPolicy().getImportance();
                    order = toDo.getPolicy().getOrder();
                }

                if (toDo.getPolicy().getImportance() == TaskImportance.A && toDo.getPolicy().getOrder() == 0) {
                    existA0 = true;
                }
            }
        }

        Policy policy;
        if (newToDo.getStatus() == TaskStatus.DELEGATION && !existA0) {
            policy = new Policy(TaskImportance.A, 0);
        } else {
            policy = new Policy(importance, order+1);
        }

        newToDo.setPolicy(policy);
    }

    private void setNewToDo(ToDo newToDo, String keyDate, List<Long> toDoNoList, Map<String, List<Long>> dateToDoListMap) {
        globalToDoNo++;
        newToDo.setToDoNo(globalToDoNo);

        toDoMap.put(globalToDoNo, newToDo);

        toDoNoList.add(globalToDoNo);
        dateToDoListMap.put(keyDate, toDoNoList);
        userToDoListMap.put(newToDo.getUserNo(), dateToDoListMap);
    }

    private List<Long> getToDoNoList(ToDoSearchCondition toDoSearchCondition) {
        if (!userToDoListMap.containsKey(toDoSearchCondition.getUserNo())) {
            return new ArrayList<>();
        }

        Map<String, List<Long>> dateToDoListMap = userToDoListMap.get(toDoSearchCondition.getUserNo());

        if (ObjectUtils.isEmpty(toDoSearchCondition.getSearchDate())) {
            return new ArrayList<>();
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

        String searchDate = simpleDateFormat.format(toDoSearchCondition.getSearchDate());

        if (!dateToDoListMap.containsKey(searchDate)) {
            return new ArrayList<>();
        }

        return dateToDoListMap.get(searchDate);
    }

    private List<ToDo> getUserToDoList(List<Long> toDoNoList) {
        List<ToDo> toDoList = new ArrayList<>();

        for (Long no : toDoNoList) {
            if (!toDoMap.containsKey(no)) {
                continue;
            }

            ToDo toDo;

            try {
                toDo = toDoMap.get(no).clone();
            } catch (CloneNotSupportedException e) {
                log.error("[TaskService::getUserToDoListOnExecutionDate] CloneNotSupportedException is occurs. toDoNo: {}, message: {}", no, e.getCause());
                continue;
            }

            if (toDo.getTargetUserNo() > 0) {
                User targetUsr = userMap.containsKey(toDo.getTargetUserNo()) ? userMap.get(toDo.getTargetUserNo()) : new User();
                toDo.setTask(toDo.getTask() + " 위임(" + targetUsr.getName() + ")");
            } else if (delegatedToDoMap.containsKey(toDo.getToDoNo())) {
                setDelegateMarkToTask(toDo);
            }

            toDoList.add(toDo);
        }

        return toDoList;
    }

    private List<ToDo> orderToDoListByPolicy(List<ToDo> toDoList) {
        return toDoList.stream()
                .sorted((ToDo o1, ToDo o2) -> {
                    int importance = o1.getPolicy().getImportance().getPriority()-o2.getPolicy().getImportance().getPriority();

                    if (importance == 0) {
                        return o1.getPolicy().getOrder()-o2.getPolicy().getOrder();
                    } else {
                        return importance;
                    }
                })
                .collect(Collectors.toList());
    }

    private boolean validateModifyToDo(ToDo originToDo, ToDo toDo, String userName, ResultMessage resultMessage) {
        if (originToDo.getUserNo() != toDo.getUserNo()) {
            resultMessage.setResult(ResultStatus.FAIL, userName + "님은 해당 업무를 변경할 수 없습니다." );
            return false;
        }

        if (!userToDoListMap.containsKey(toDo.getUserNo())) {
            resultMessage.setResult(ResultStatus.FAIL, userName + "님의 업무가 존재하지 않습니다.");
            return false;
        }

        return true;
    }

    private boolean validateDelegationToDo(ToDo toDo, ResultMessage resultMessage) {
        if (toDo.getTargetUserNo() <= 0) {
            resultMessage.setResult(ResultStatus.FAIL, "위임할 대상자의 사용자 번호를 입력해 주세요.");
            return false;
        } else {
            if (!userMap.containsKey(toDo.getTargetUserNo())) {
                resultMessage.setResult(ResultStatus.FAIL, "위임할 대상자가 존재하지 않습니다.");
                return false;
            }

            if (delegatedToDoMap.containsKey(toDo.getToDoNo())) {
                resultMessage.setResult(ResultStatus.FAIL, "위임받은 업무는 재위임할 수 없습니다.");
                return false;
            }
        }

        return true;
    }

    private void delegateToDo(ToDo toDo) {
        List<Long> userNoList = new ArrayList<>();
        userNoList.add(toDo.getUserNo());
        userNoList.add(toDo.getTargetUserNo());

        delegationRelationMap.put(toDo.getToDoNo(), userNoList);

        ToDo newToDo = new ToDo(toDo, toDo.getTargetUserNo());
        createToDo(newToDo);

        delegatedToDoMap.put(newToDo.getToDoNo(), toDo.getToDoNo());
    }

    private void setModifiedToDo(ToDo toDo) {
        toDoMap.put(toDo.getToDoNo(), toDo);
    }

    private void cancelDelegateToDo(long toDoNo) {
        long originToDoNo = delegatedToDoMap.get(toDoNo);

        if (originToDoNo == 0) {
            return;
        }

        if (delegationRelationMap.containsKey(originToDoNo)) {
            delegationRelationMap.remove(originToDoNo);
        }

        if (toDoMap.containsKey(originToDoNo)) {
            ToDo originToDo = toDoMap.get(originToDoNo);
            originToDo.setStatus(TaskStatus.PROGRESS);
            originToDo.setTargetUserNo(0);
        }

        delegatedToDoMap.remove(toDoNo);
    }

    private boolean validateDeleteToDo(long toDoNo, ResultMessage resultMessage) {
        if (!toDoMap.containsKey(toDoNo)) {
            resultMessage.setResult(ResultStatus.FAIL, "삭제할 업무가 존재하지 않습니다.");

            return false;
        }

        return true;
    }

    private boolean isDelegatedToDo(long toDoNo) {
        if (delegatedToDoMap.containsKey(toDoNo)) {
            return true;
        }

        return false;
    }

    private void setDelegateMarkToTask(ToDo toDo) {
        long originToDoNo = delegatedToDoMap.get(toDo.getToDoNo());

        if (!delegationRelationMap.containsKey(originToDoNo)) {
            return;
        }

        List<Long> userNoList = delegationRelationMap.get(originToDoNo);

        if (userNoList.size() != 2) {
            return;
        }

        long userNo = userNoList.get(0);

        if (!userMap.containsKey(userNo)) {
            return;
        }

        toDo.setTask(toDo.getTask() + " " + userMap.get(userNo).getName());
    }

    private void changePolicy(ToDo toDo, String keyDate, String type) {
        Map<String, List<Long>> dateToDoListMap = userToDoListMap.get(toDo.getUserNo());

        List<Long> toDoNoList= dateToDoListMap.containsKey(keyDate) ? dateToDoListMap.get(keyDate) : new ArrayList<>();
        List<ToDo> toDoList;
        Map<TaskImportance, Map<Integer, List<Long>>> policyMap;

        if ("remove".equals(type) & toDoNoList.size() > 0) {
            toDoNoList.remove(toDo.getToDoNo());

            toDoList = getToDoList(toDo.getUserNo(), toDo.getExecutionDate());
            policyMap = getPolicyMap(toDo, toDoList, type);

            reOrderPolicy(policyMap, toDo, "remove");
        } else if (toDoNoList.size() > 0) {
            toDoList = getToDoList(toDo.getUserNo(), toDo.getExecutionDate());
            policyMap = getPolicyMap(toDo, toDoList, type);

            if (policyMap.containsKey(toDo.getPolicy().getImportance())) {
                reOrderPolicy(policyMap, toDo, type);
            }

            ToDo originToDo = toDoMap.get(toDo.getToDoNo());
            if (originToDo.getPolicy().getImportance() != toDo.getPolicy().getImportance()) {
                reOrderPolicy(policyMap, originToDo, "remove");
            }

            if (!policyMap.containsKey(toDo.getPolicy().getImportance())) {
                Policy changePolicy = new Policy(toDo.getPolicy().getImportance(), 0);
                toDo.setPolicy(changePolicy);
            }
        } else {
            toDoNoList.add(toDo.getToDoNo());

            Policy newPolicy = new Policy(toDo.getPolicy().getImportance(), 0);
            toDo.setPolicy(newPolicy);

            toDoMap.put(toDo.getToDoNo(), toDo);

            dateToDoListMap.put(keyDate, toDoNoList);
            userToDoListMap.put(toDo.getUserNo(), dateToDoListMap);
        }
    }

    private Map<TaskImportance, Map<Integer, List<Long>>> getPolicyMap(ToDo toDo, List<ToDo> toDoList, String type) {
        Map<TaskImportance, Map<Integer, List<Long>>> policyMap = new HashMap<>();

        for (ToDo otherToDo : toDoList) {
            if (toDo.getToDoNo() == otherToDo.getToDoNo() && !"changeOrder".equals(type)) {
                continue;
            }

            Map<Integer, List<Long>> orderMap;
            List<Long> toDoNoList = new ArrayList<>();
            if (policyMap.containsKey(otherToDo.getPolicy().getImportance())) {
                orderMap = policyMap.get(otherToDo.getPolicy().getImportance());

                if (orderMap.containsKey(otherToDo.getPolicy().getOrder())) {
                    toDoNoList = orderMap.get(otherToDo.getPolicy().getOrder());
                }
            } else {
                orderMap = new HashMap<>();
            }

            if (toDo.getToDoNo() == otherToDo.getToDoNo() && "changeOrder".equals(type)) {
                toDoNoList.add(toDo.getToDoNo());
                orderMap.put(toDo.getPolicy().getOrder(), toDoNoList);
            } else {
                toDoNoList.add(otherToDo.getToDoNo());
                orderMap.put(otherToDo.getPolicy().getOrder(), toDoNoList);
            }

            policyMap.put(otherToDo.getPolicy().getImportance(), orderMap);
        }

        return policyMap;
    }

    private void reOrderPolicy(Map<TaskImportance, Map<Integer, List<Long>>> policyMap, ToDo toDo, String reOrderType) {
        List<Long> policyOrderToDoNoList = new ArrayList<>();

        Map<Integer, List<Long>> orderMap = policyMap.get(toDo.getPolicy().getImportance());

        if (CollectionUtils.isEmpty(orderMap)) {
            return;
        }

        int count = orderMap.size();
        int duplicatedOrder = -1;
        if ("add".equals(reOrderType)) {
            count++;

            if (orderMap.containsKey(toDo.getPolicy().getOrder())) {
                duplicatedOrder = toDo.getPolicy().getOrder();
            }

            for (int index=0; index<count; index++) {
                if (!orderMap.containsKey(index)) {
                    continue;
                }

                if (duplicatedOrder == index) {
                    policyOrderToDoNoList.add(toDo.getToDoNo());
                }

                addPolicyOrderList(policyOrderToDoNoList, orderMap.get(index));
            }
        } else {
            List<Integer> keySet = new ArrayList(orderMap.keySet());

            Collections.sort(keySet);

            for (Integer key : keySet) {
                addPolicyOrderList(policyOrderToDoNoList, orderMap.get(key));
            }
        }

        int newOrder = 0;
        for (Long policyToDoNo : policyOrderToDoNoList) {
            ToDo policyToDo;
            if (policyToDoNo == toDo.getToDoNo()) {
                policyToDo = toDo;
            } else {
                policyToDo = toDoMap.get(policyToDoNo);
            }

            Policy changePolicy = new Policy(toDo.getPolicy().getImportance(), newOrder);
            policyToDo.setPolicy(changePolicy);
            toDoMap.put(policyToDoNo, policyToDo);
            newOrder++;
        }
    }

    private void addPolicyOrderList(List<Long> policyOrderToDoNoList, List<Long> orderToDoNoList) {
        for (Long toDoNo : orderToDoNoList) {
            policyOrderToDoNoList.add(toDoNo);
        }

    }
}

