package com.dochiri.taskservice.domain;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.taskservice.application.error.TaskErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskOwnerTest {

    @Test
    void 소유자_타입이_null이면_커스텀_예외가_발생한다() {
        assertThatThrownBy(() -> new TaskOwner(null, "user-1"))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(TaskErrorCode.TASK_OWNER_TYPE_MISSING);
    }

    @Test
    void 소유자_식별자가_null이면_커스텀_예외가_발생한다() {
        assertThatThrownBy(() -> new TaskOwner(OwnerType.USER, null))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(TaskErrorCode.TASK_OWNER_REFERENCE_ID_MISSING);
    }

    @Test
    void 소유자_식별자가_비어있으면_커스텀_예외가_발생한다() {
        assertThatThrownBy(() -> new TaskOwner(OwnerType.USER, "   "))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(TaskErrorCode.TASK_OWNER_REFERENCE_ID_BLANK);
    }
}
