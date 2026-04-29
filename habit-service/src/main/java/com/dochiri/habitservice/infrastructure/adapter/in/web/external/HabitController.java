package com.dochiri.habitservice.infrastructure.adapter.in.web.external;

import com.dochiri.habitservice.application.port.in.CreateHabitRecordUseCase;
import com.dochiri.habitservice.application.port.in.CreateHabitUseCase;
import com.dochiri.habitservice.application.port.in.DeleteHabitRecordUseCase;
import com.dochiri.habitservice.application.port.in.DeleteHabitUseCase;
import com.dochiri.habitservice.application.port.in.GetHabitDetailUseCase;
import com.dochiri.habitservice.application.port.in.GetHabitGrassUseCase;
import com.dochiri.habitservice.application.port.in.GetHabitRecordsUseCase;
import com.dochiri.habitservice.application.port.in.GetHabitsUseCase;
import com.dochiri.habitservice.application.port.in.SwapHabitIndexUseCase;
import com.dochiri.habitservice.application.port.in.UpdateHabitNameUseCase;
import com.dochiri.habitservice.application.port.in.UpdateHabitRecordUseCase;
import com.dochiri.habitservice.application.port.in.dto.DeleteHabitRecordCommand;
import com.dochiri.habitservice.application.port.in.dto.DeleteHabitCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitDetailCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitRecordsCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitsCommand;
import com.dochiri.habitservice.application.security.HabitOwnerProvider;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.request.CreateHabitRecordRequest;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.request.CreateHabitRequest;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.request.SwapHabitIndexRequest;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.request.UpdateHabitNameRequest;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.request.UpdateHabitRecordRequest;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.CreateHabitRecordResponse;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.CreateHabitResponse;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.GetHabitDetailResponse;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.GetHabitGrassResponse;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.GetHabitRecordsResponse;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.GetHabitsResponse;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.SwapHabitIndexResponse;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.UpdateHabitNameResponse;
import com.dochiri.habitservice.infrastructure.adapter.in.web.external.response.UpdateHabitRecordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Habit", description = "습관 관리 API. JWT(Authorization 헤더 또는 access_token 쿠키) 또는 게스트 세션 쿠키(guest_session)로 인증한다.")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "guestSession")
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
    private final SwapHabitIndexUseCase swapHabitIndexUseCase;
    private final DeleteHabitUseCase deleteHabitUseCase;
    private final GetHabitRecordsUseCase getHabitRecordsUseCase;
    private final UpdateHabitRecordUseCase updateHabitRecordUseCase;
    private final DeleteHabitRecordUseCase deleteHabitRecordUseCase;
    private final HabitOwnerProvider habitOwnerProvider;

    @Operation(summary = "습관 목록 조회", description = "로그인한 사용자의 전체 습관 목록을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = GetHabitsResponse.class)))
    @GetMapping
    public ResponseEntity<GetHabitsResponse> getHabits() {
        return ResponseEntity.ok(GetHabitsResponse.from(getHabitsUseCase.execute(new GetHabitsCommand(owner()))));
    }

    @Operation(summary = "습관 생성", description = "새로운 습관을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "생성 성공",
            content = @Content(schema = @Schema(implementation = CreateHabitResponse.class)))
    @PostMapping
    public ResponseEntity<CreateHabitResponse> createHabit(
            @Valid @RequestBody CreateHabitRequest request
    ) {
        return ResponseEntity.ok(CreateHabitResponse.from(createHabitUseCase.execute(request.toCommand(owner()))));
    }

    @Operation(summary = "습관 상세 조회", description = "특정 습관의 상세 정보를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = GetHabitDetailResponse.class)))
    @ApiResponse(responseCode = "404", description = "습관을 찾을 수 없음", content = @Content)
    @GetMapping("/{habitId}")
    public ResponseEntity<GetHabitDetailResponse> getHabitDetail(
            @Parameter(description = "습관 ID") @PathVariable String habitId
    ) {
        return ResponseEntity.ok(GetHabitDetailResponse.from(getHabitDetailUseCase.execute(new GetHabitDetailCommand(habitId, owner()))));
    }

    @Operation(summary = "습관 이름 수정", description = "특정 습관의 이름을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(schema = @Schema(implementation = UpdateHabitNameResponse.class)))
    @ApiResponse(responseCode = "404", description = "습관을 찾을 수 없음", content = @Content)
    @PatchMapping("/{habitId}")
    public ResponseEntity<UpdateHabitNameResponse> updateHabitName(
            @Parameter(description = "습관 ID") @PathVariable String habitId,
            @Valid @RequestBody UpdateHabitNameRequest request
    ) {
        return ResponseEntity.ok(UpdateHabitNameResponse.from(updateHabitNameUseCase.execute(request.toCommand(habitId, owner()))));
    }

    @Operation(summary = "습관 정렬 순서 교환", description = "두 습관의 정렬 순서를 서로 교환합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(schema = @Schema(implementation = SwapHabitIndexResponse.class)))
    @ApiResponse(responseCode = "404", description = "습관을 찾을 수 없음", content = @Content)
    @PatchMapping("/index/swap")
    public ResponseEntity<SwapHabitIndexResponse> swapHabitIndex(
            @Valid @RequestBody SwapHabitIndexRequest request
    ) {
        return ResponseEntity.ok(SwapHabitIndexResponse.from(swapHabitIndexUseCase.execute(request.toCommand(owner()))));
    }

    @Operation(summary = "습관 삭제", description = "특정 습관을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content)
    @ApiResponse(responseCode = "404", description = "습관을 찾을 수 없음", content = @Content)
    @DeleteMapping("/{habitId}")
    public ResponseEntity<Void> deleteHabit(
            @Parameter(description = "습관 ID") @PathVariable String habitId
    ) {
        deleteHabitUseCase.execute(new DeleteHabitCommand(habitId, owner()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "습관 기록 조회", description = "특정 습관의 완료 기록을 조회합니다. 기간을 지정하지 않으면 전체 기록을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = GetHabitRecordsResponse.class)))
    @ApiResponse(responseCode = "404", description = "습관을 찾을 수 없음", content = @Content)
    @GetMapping("/{habitId}/records")
    public ResponseEntity<GetHabitRecordsResponse> getHabitRecords(
            @Parameter(description = "습관 ID") @PathVariable String habitId,
            @Parameter(description = "조회 시작일 (ISO 8601, 예: 2024-01-01)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "조회 종료일 (ISO 8601, 예: 2024-01-31)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(GetHabitRecordsResponse.from(getHabitRecordsUseCase.execute(GetHabitRecordsCommand.of(habitId, owner(), from, to))));
    }

    @Operation(summary = "습관 완료 기록 생성", description = "특정 습관의 완료 기록을 추가합니다.")
    @ApiResponse(responseCode = "200", description = "기록 생성 성공",
            content = @Content(schema = @Schema(implementation = CreateHabitRecordResponse.class)))
    @ApiResponse(responseCode = "404", description = "습관을 찾을 수 없음", content = @Content)
    @PostMapping("/{habitId}/records")
    public ResponseEntity<CreateHabitRecordResponse> createHabitRecord(
            @Parameter(description = "습관 ID") @PathVariable String habitId,
            @RequestBody CreateHabitRecordRequest request
    ) {
        return ResponseEntity.ok(CreateHabitRecordResponse.from(createHabitRecordUseCase.execute(request.toCommand(habitId, owner()))));
    }

    @Operation(summary = "습관 완료 기록 수정", description = "특정 습관의 완료 기록을 수정합니다. completedAt 또는 minutes 중 필요한 필드만 보낼 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "기록 수정 성공",
            content = @Content(schema = @Schema(implementation = UpdateHabitRecordResponse.class)))
    @ApiResponse(responseCode = "404", description = "습관 또는 기록을 찾을 수 없음", content = @Content)
    @PatchMapping("/{habitId}/records/{recordId}")
    public ResponseEntity<UpdateHabitRecordResponse> updateHabitRecord(
            @Parameter(description = "습관 ID") @PathVariable String habitId,
            @Parameter(description = "기록 ID") @PathVariable String recordId,
            @Valid @RequestBody UpdateHabitRecordRequest request
    ) {
        return ResponseEntity.ok(UpdateHabitRecordResponse.from(updateHabitRecordUseCase.execute(request.toCommand(habitId, recordId, owner()))));
    }

    @Operation(summary = "습관 완료 기록 삭제", description = "특정 습관의 완료 기록을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "기록 삭제 성공", content = @Content)
    @ApiResponse(responseCode = "404", description = "습관 또는 기록을 찾을 수 없음", content = @Content)
    @DeleteMapping("/{habitId}/records/{recordId}")
    public ResponseEntity<Void> deleteHabitRecord(
            @Parameter(description = "습관 ID") @PathVariable String habitId,
            @Parameter(description = "기록 ID") @PathVariable String recordId
    ) {
        deleteHabitRecordUseCase.execute(new DeleteHabitRecordCommand(habitId, recordId, owner()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "잔디 조회", description = "날짜 범위 내 습관 완료 이력을 잔디 형태로 집계합니다. 기본값: from = 첫 습관 생성일, to = 오늘. 요청 from이 첫 습관 생성일보다 앞서면 첫 습관 생성일로 보정합니다. 레벨 기준: 0건→0, 1건→1, 2건→2, 3~4건→3, 5건 이상→4.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = GetHabitGrassResponse.class)))
    @GetMapping("/grass")
    public ResponseEntity<GetHabitGrassResponse> getGrass(
            @Parameter(description = "조회 시작일 (ISO 8601, 예: 2024-01-01)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "조회 종료일 (ISO 8601, 예: 2024-04-12)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(GetHabitGrassResponse.from(getHabitGrassUseCase.execute(new GetHabitGrassCommand(owner(), from, to))));
    }

    private HabitOwner owner() {
        return habitOwnerProvider.currentOwner();
    }

}
