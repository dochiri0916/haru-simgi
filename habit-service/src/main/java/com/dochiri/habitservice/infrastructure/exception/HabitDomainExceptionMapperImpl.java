package com.dochiri.habitservice.infrastructure.exception;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.habitservice.application.error.HabitErrorCode;
import com.dochiri.habitservice.application.port.out.HabitDomainExceptionMapper;
import com.dochiri.habitservice.domain.exception.HabitAccessDeniedException;
import com.dochiri.habitservice.domain.exception.HabitDomainException;
import com.dochiri.habitservice.domain.exception.HabitNotFoundException;
import com.dochiri.habitservice.domain.exception.InvalidHabitIdException;
import com.dochiri.habitservice.domain.exception.InvalidHabitNameException;
import com.dochiri.habitservice.domain.exception.InvalidHabitOwnerException;
import com.dochiri.habitservice.domain.exception.InvalidHabitRecordIdException;
import org.springframework.stereotype.Component;

@Component
public class HabitDomainExceptionMapperImpl implements HabitDomainExceptionMapper {

    @Override
    public BaseException map(HabitDomainException exception) {
        HabitErrorCode errorCode = switch (exception) {
            case HabitNotFoundException e -> HabitErrorCode.HABIT_NOT_FOUND;
            case HabitAccessDeniedException e -> HabitErrorCode.HABIT_NOT_FOUND;
            case InvalidHabitIdException e -> HabitErrorCode.INVALID_HABIT_ID;
            case InvalidHabitNameException e -> HabitErrorCode.INVALID_HABIT_NAME;
            case InvalidHabitRecordIdException e -> HabitErrorCode.INVALID_HABIT_RECORD_ID;
            case InvalidHabitOwnerException e ->
                e.getReason() == InvalidHabitOwnerException.Reason.INVALID_TYPE
                    ? HabitErrorCode.INVALID_HABIT_OWNER_TYPE
                    : HabitErrorCode.INVALID_HABIT_OWNER_REFERENCE_ID;
            default -> throw new IllegalStateException("매핑되지 않은 도메인 예외: " + exception.getClass().getSimpleName());
        };
        return new BaseException(errorCode, exception);
    }

}
