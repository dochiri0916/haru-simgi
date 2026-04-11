package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.GetHabitRecordsResult;

import java.time.Instant;
import java.util.List;

public record GetHabitRecordsResponse(String habitId, List<RecordItem> records) {

    public record RecordItem(String id, Instant completedAt, int value) {}

    public static GetHabitRecordsResponse from(GetHabitRecordsResult result) {
        List<RecordItem> records = result.records().stream()
            .map(r -> new RecordItem(r.id(), r.completedAt(), r.minutes()))
            .toList();
        return new GetHabitRecordsResponse(result.habitId(), records);
    }

}
