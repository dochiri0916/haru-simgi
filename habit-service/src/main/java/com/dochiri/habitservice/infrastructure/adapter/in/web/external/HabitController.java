package com.dochiri.habitservice.infrastructure.adapter.in.web.external;

import com.dochiri.habitservice.application.port.in.CreateHabitRecordUseCase;
import com.dochiri.habitservice.application.port.in.CreateHabitUseCase;
import com.dochiri.habitservice.application.port.in.DeleteHabitUseCase;
import com.dochiri.habitservice.application.port.in.GetHabitDetailUseCase;
import com.dochiri.habitservice.application.port.in.GetHabitGrassUseCase;
import com.dochiri.habitservice.application.port.in.GetHabitRecordsUseCase;
import com.dochiri.habitservice.application.port.in.GetHabitsUseCase;
import com.dochiri.habitservice.application.port.in.UpdateHabitNameUseCase;
import com.dochiri.habitservice.application.port.in.dto.DeleteHabitCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitDetailCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitRecordsCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitsCommand;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.request.CreateHabitRecordRequest;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.request.CreateHabitRequest;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.request.UpdateHabitNameRequest;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.CreateHabitRecordResponse;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.CreateHabitResponse;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.GetHabitDetailResponse;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.GetHabitGrassResponse;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.GetHabitRecordsResponse;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.GetHabitsResponse;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.UpdateHabitNameResponse;
import com.dochiri.security.jwt.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        GetHabitsCommand command = new GetHabitsCommand(String.valueOf(principal.userId()));
        return ResponseEntity.ok(GetHabitsResponse.from(getHabitsUseCase.execute(command)));
    }

    @PostMapping
    public ResponseEntity<CreateHabitResponse> createHabit(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody CreateHabitRequest request
    ) {
        return ResponseEntity.ok(CreateHabitResponse.from(createHabitUseCase.execute(request.toCommand(String.valueOf(principal.userId())))));
    }

    @GetMapping("/{habitId}")
    public ResponseEntity<GetHabitDetailResponse> getHabitDetail(
            @PathVariable String habitId,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        GetHabitDetailCommand command = new GetHabitDetailCommand(habitId, String.valueOf(principal.userId()));
        return ResponseEntity.ok(GetHabitDetailResponse.from(getHabitDetailUseCase.execute(command)));
    }

    @PatchMapping("/{habitId}")
    public ResponseEntity<UpdateHabitNameResponse> updateHabitName(
            @PathVariable String habitId,
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody UpdateHabitNameRequest request
    ) {
        return ResponseEntity.ok(UpdateHabitNameResponse.from(updateHabitNameUseCase.execute(request.toCommand(habitId, String.valueOf(principal.userId())))));
    }

    @DeleteMapping("/{habitId}")
    public ResponseEntity<Void> deleteHabit(
            @PathVariable String habitId,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        DeleteHabitCommand command = new DeleteHabitCommand(habitId, String.valueOf(principal.userId()));
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

        GetHabitRecordsCommand command = new GetHabitRecordsCommand(habitId, String.valueOf(principal.userId()), fromInstant, toInstant);
        return ResponseEntity.ok(GetHabitRecordsResponse.from(getHabitRecordsUseCase.execute(command)));
    }

    @PostMapping("/{habitId}/records")
    public ResponseEntity<CreateHabitRecordResponse> createHabitRecord(
            @PathVariable String habitId,
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody CreateHabitRecordRequest request
    ) {
        return ResponseEntity.ok(CreateHabitRecordResponse.from(createHabitRecordUseCase.execute(request.toCommand(habitId, String.valueOf(principal.userId())))));
    }

    @GetMapping("/grass")
    public ResponseEntity<GetHabitGrassResponse> getGrass(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate fromDate = from != null ? from : LocalDate.now().minusWeeks(18);
        LocalDate toDate = to != null ? to : LocalDate.now();

        GetHabitGrassCommand command = new GetHabitGrassCommand(String.valueOf(principal.userId()), fromDate, toDate);
        return ResponseEntity.ok(GetHabitGrassResponse.from(getHabitGrassUseCase.execute(command)));
    }

}