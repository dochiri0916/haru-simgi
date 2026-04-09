package com.dochiri.taskservice.application.service;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.taskservice.application.error.TaskErrorCode;
import com.dochiri.taskservice.domain.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskOwnerGuard {

    public void validateUserOwner(Task task, String requesterUserId) {
        if (!task.getOwner().isUser()) {
            throw new BaseException(TaskErrorCode.TASK_OWNER_FORBIDDEN);
        }
        if (!task.getOwner().referenceId().equals(requesterUserId)) {
            throw new BaseException(TaskErrorCode.TASK_OWNER_FORBIDDEN);
        }
    }
}
