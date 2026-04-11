package com.dochiri.habitservice.infrastructure.adapter.in.web;

import com.dochiri.habitservice.application.port.in.CreateHabitRecordUseCase;
import com.dochiri.habitservice.application.port.in.CreateHabitUseCase;
import com.dochiri.habitservice.application.port.in.DeleteHabitUseCase;
import com.dochiri.habitservice.application.port.in.GetHabitDetailUseCase;
import com.dochiri.habitservice.application.port.in.GetHabitGrassUseCase;
import com.dochiri.habitservice.application.port.in.GetHabitRecordsUseCase;
import com.dochiri.habitservice.application.port.in.GetHabitsUseCase;
import com.dochiri.habitservice.application.port.in.UpdateHabitNameUseCase;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitCommand;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordCommand;
import com.dochiri.habitservice.application.port.in.dto.DeleteHabitCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitDetailCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitRecordsCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitsCommand;
import com.dochiri.habitservice.application.port.in.dto.UpdateHabitNameCommand;
import com.dochiri.habitservice.infrastructure.adapter.in.web.request.CreateHabitRequest;
import com.dochiri.habitservice.infrastructure.adapter.in.web.response.CreateHabitResponse;
import com.dochiri.security.jwt.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {

    private final CreateHabitUseCase createHabitUseCase;
    private final GetHabitGrassUseCase getHabitGrassUseCase;
    private final CreateHabitRecordUseCase createHabitRecordUseCase;
    private final GetHabitsUseCase getHabitsUseCase;
    private final GetHabitDetailUseCase getHabitDetailUseCase;
    private final UpdateHabitNameUseCase updateHabitNameUseCase;
    private final DeleteHabitUseCase deleteHabitUseCase;
    private final GetHabitRecordsUseCase getHabitRecordsUseCase;

    @GetMapping
    public ResponseEntity<GetHabitsResponse> getHabits(
        @AuthenticationPrincipal JwtPrincipal principal
    ) {
        var command = new GetHabitsCommand(String.valueOf(principal.userId()));
        var result = getHabitsUseCase.execute(command);

        var habits = result.habits().stream()
            .map(h -> new HabitDto(h.id(), h.name()))
            .toList();

        return ResponseEntity.ok(new GetHabitsResponse(habits));
    }

    @PostMapping
    public ResponseEntity<CreateHabitResponse> createHabit(
        @AuthenticationPrincipal JwtPrincipal principal,
        @RequestBody CreateHabitRequest request
    ) {
        var command = new CreateHabitCommand(
            String.valueOf(principal.userId()),
            request.name()
        );
        var result = createHabitUseCase.execute(command);
        return ResponseEntity.ok(new CreateHabitResponse(result.id(), result.name()));
    }

    @GetMapping("/{habitId}")
    public ResponseEntity<GetHabitDetailResponse> getHabitDetail(
        @PathVariable String habitId,
        @AuthenticationPrincipal JwtPrincipal principal
    ) {
        var command = new GetHabitDetailCommand(habitId, String.valueOf(principal.userId()));
        var result = getHabitDetailUseCase.execute(command);

        return ResponseEntity.ok(new GetHabitDetailResponse(result.id(), result.name()));
    }

    @PatchMapping("/{habitId}")
    public ResponseEntity<UpdateHabitNameResponse> updateHabitName(
        @PathVariable String habitId,
        @AuthenticationPrincipal JwtPrincipal principal,
        @RequestBody UpdateHabitNameRequest request
    ) {
        var command = new UpdateHabitNameCommand(habitId, String.valueOf(principal.userId()), request.name());
        var result = updateHabitNameUseCase.execute(command);

        return ResponseEntity.ok(new UpdateHabitNameResponse(result.id(), result.name()));
    }

    @DeleteMapping("/{habitId}")
    public ResponseEntity<Void> deleteHabit(
        @PathVariable String habitId,
        @AuthenticationPrincipal JwtPrincipal principal
    ) {
        var command = new DeleteHabitCommand(habitId, String.valueOf(principal.userId()));
        deleteHabitUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{habitId}/records")
    public ResponseEntity<GetHabitRecordsResponse> getHabitRecords(
        @PathVariable String habitId,
        @AuthenticationPrincipal JwtPrincipal principal,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate fromDate = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate toDate = to != null ? to : LocalDate.now();

        Instant fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toInstant = toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        var command = new GetHabitRecordsCommand(habitId, String.valueOf(principal.userId()), fromInstant, toInstant);
        var result = getHabitRecordsUseCase.execute(command);

        var records = result.records().stream()
            .map(r -> new HabitRecordDto(r.id(), r.completedAt(), r.value()))
            .toList();

        return ResponseEntity.ok(new GetHabitRecordsResponse(result.habitId(), records));
    }

    @PostMapping("/{habitId}/records")
    public ResponseEntity<CreateHabitRecordResponse> createHabitRecord(
        @PathVariable String habitId,
        @AuthenticationPrincipal JwtPrincipal principal,
        @RequestBody CreateHabitRecordRequest request
    ) {
        var command = new CreateHabitRecordCommand(
            habitId,
            String.valueOf(principal.userId()),
            request.completedAt(),
            request.value()
        );
        var result = createHabitRecordUseCase.execute(command);
        return ResponseEntity.ok(new CreateHabitRecordResponse(result.id(), result.habitId(), result.completedAt(), result.value()));
    }

    @GetMapping("/grass")
    public ResponseEntity<GetHabitGrassResponse> getGrass(
        @AuthenticationPrincipal JwtPrincipal principal,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate fromDate = from != null ? from : LocalDate.now().minusWeeks(18);
        LocalDate toDate = to != null ? to : LocalDate.now();

        var command = new GetHabitGrassCommand(String.valueOf(principal.userId()), fromDate, toDate);
        var result = getHabitGrassUseCase.execute(command);

        var days = result.days().stream()
            .map(d -> new HabitGrassDayDto(d.date(), d.value(), d.level()))
            .toList();

        return ResponseEntity.ok(new GetHabitGrassResponse(result.fromDate(), result.toDate(), result.totalValue(), days));
    }

    public record HabitDto(String id, String name) {}
    public record GetHabitsResponse(java.util.List<HabitDto> habits) {}

    public record GetHabitDetailResponse(String id, String name) {}

    public record UpdateHabitNameRequest(String name) {}
    public record UpdateHabitNameResponse(String id, String name) {}

    public record CreateHabitRecordRequest(Instant completedAt, int value) {}
    public record CreateHabitRecordResponse(String id, String habitId, Instant completedAt, int value) {}

    public record HabitRecordDto(String id, Instant completedAt, int value) {}
    public record GetHabitRecordsResponse(String habitId, java.util.List<HabitRecordDto> records) {}

    public record HabitGrassDayDto(LocalDate date, int value, int level) {}
    public record GetHabitGrassResponse(LocalDate fromDate, LocalDate toDate, int totalValue, java.util.List<HabitGrassDayDto> days) {}
}
