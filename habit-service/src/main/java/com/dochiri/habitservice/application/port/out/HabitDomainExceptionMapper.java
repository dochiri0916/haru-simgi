package com.dochiri.habitservice.application.port.out;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.habitservice.domain.exception.HabitDomainException;

public interface HabitDomainExceptionMapper {

    BaseException map(HabitDomainException exception);

}
