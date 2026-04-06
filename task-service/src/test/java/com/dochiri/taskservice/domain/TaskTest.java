package com.dochiri.taskservice.domain;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.taskservice.application.error.TaskErrorCode;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskTest {

    @Test
    void 완료된_할일에_completedAt이_없으면_커스텀_예외가_발생한다() {
        assertThatThrownBy(() -> Task.from("task-1", TaskOwner.user("user-1"), "운동", true, null))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(TaskErrorCode.TASK_COMPLETED_AT_REQUIRED);
    }

    @Test
    void 미완료_할일에_completedAt이_있으면_커스텀_예외가_발생한다() {
        assertThatThrownBy(() -> Task.from("task-1", TaskOwner.user("user-1"), "운동", false, Instant.now()))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(TaskErrorCode.TASK_COMPLETED_AT_MUST_BE_NULL);
    }

    @Test
    void 완료하면_completedAt이_기록된다() {
        Task task = Task.create(TaskOwner.user("user-1"), "운동");
        Instant completedAt = Instant.parse("2026-04-06T12:00:00Z");

        task.complete(completedAt);

        assertThat(task.isCompleted()).isTrue();
        assertThat(task.getCompletedAt()).isEqualTo(completedAt);
    }
}
