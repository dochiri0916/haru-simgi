package com.dochiri.habitservice.domain;

import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.record.HabitCompletion;
import com.dochiri.habitservice.domain.record.HabitRecord;
import com.dochiri.habitservice.domain.record.exception.HabitRecordErrorCode;
import com.dochiri.habitservice.domain.record.exception.InvalidCompletedAtException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HabitRecordTest {

    @Test
    void 완료할_때_소요_시간을_분으로_기록할_수_있다() {
        Instant completedAt = Instant.parse("2026-04-14T00:00:00Z");
        HabitId habitId = HabitId.newId();

        HabitRecord record = HabitRecord.create(
                habitId,
                HabitCompletion.of(completedAt, 30, null)
        );

        assertThat(record.getHabitId()).isEqualTo(habitId);
        assertThat(record.getCompletedAt()).isEqualTo(completedAt);
        assertThat(record.getDuration().minutes()).isEqualTo(30);
    }

    @Test
    void 소요_시간_없이도_완료_기록을_생성할_수_있다() {
        Instant completedAt = Instant.parse("2026-04-14T00:00:00Z");
        HabitId habitId = HabitId.newId();

        HabitRecord record = HabitRecord.create(
                habitId,
                HabitCompletion.of(completedAt, null, null)
        );

        assertThat(record.getHabitId()).isEqualTo(habitId);
        assertThat(record.getCompletedAt()).isEqualTo(completedAt);
        assertThat(record.hasDuration()).isFalse();
        assertThat(record.getDuration()).isNull();
    }

    @Test
    void 완료_시간이_없으면_도메인_예외가_발생한다() {
        assertThatThrownBy(() -> HabitCompletion.of(null, null, null))
                .isInstanceOf(InvalidCompletedAtException.class)
                .extracting("errorCode")
                .isEqualTo(HabitRecordErrorCode.INVALID_COMPLETED_AT);
    }

}
