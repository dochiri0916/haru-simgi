package com.dochiri.taskservice.infrastructure.adapter.in.web.external;

import com.dochiri.taskservice.application.port.in.CompleteTaskUseCase;
import com.dochiri.taskservice.application.port.in.DeleteTaskUseCase;
import com.dochiri.taskservice.application.port.in.GetTaskGrassUseCase;
import com.dochiri.taskservice.application.port.in.GetTasksUseCase;
import com.dochiri.taskservice.application.port.in.ReopenTaskUseCase;
import com.dochiri.taskservice.application.port.in.dto.DeleteTaskCommand;
import com.dochiri.taskservice.application.port.in.dto.CompleteTaskCommand;
import com.dochiri.taskservice.application.port.in.dto.CreateTaskCommand;
import com.dochiri.taskservice.application.port.in.dto.GetTaskGrassCommand;
import com.dochiri.taskservice.application.port.in.dto.GetTasksCommand;
import com.dochiri.taskservice.application.port.in.dto.ReopenTaskCommand;
import com.dochiri.taskservice.application.service.CreateTaskService;
import com.dochiri.taskservice.domain.TaskOwner;
import com.dochiri.taskservice.infrastructure.adapter.in.web.external.request.CreateTaskRequest;
import com.dochiri.taskservice.infrastructure.adapter.in.web.external.response.CompleteTaskResponse;
import com.dochiri.taskservice.infrastructure.adapter.in.web.external.response.CreateTaskResponse;
import com.dochiri.taskservice.infrastructure.adapter.in.web.external.response.TaskResponse;
import com.dochiri.taskservice.infrastructure.adapter.in.web.external.response.TaskGrassResponse;
import com.dochiri.security.jwt.JwtPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final CreateTaskService createTaskService;
    private final CompleteTaskUseCase completeTaskUseCase;
    private final ReopenTaskUseCase reopenTaskUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final GetTasksUseCase getTasksUseCase;
    private final GetTaskGrassUseCase getTaskGrassUseCase;

    @PostMapping
    public ResponseEntity<CreateTaskResponse> create(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        return ResponseEntity.ok().body(
                CreateTaskResponse.from(
                        createTaskService.create(
                                new CreateTaskCommand(
                                        authenticatedOwner(principal),
                                        request.title()
                                )
                        )
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(required = false) Boolean completed
    ) {
        return ResponseEntity.ok(
                getTasksUseCase.getTasks(
                                new GetTasksCommand(authenticatedOwner(principal), completed)
                        ).stream()
                        .map(TaskResponse::from)
                        .toList()
        );
    }

    @PatchMapping("/{taskId}/complete")
    public ResponseEntity<CompleteTaskResponse> complete(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable String taskId
    ) {
        return ResponseEntity.ok().body(
                CompleteTaskResponse.from(
                        completeTaskUseCase.complete(
                                new CompleteTaskCommand(taskId, String.valueOf(principal.userId()))
                        )
                )
        );
    }

    @PatchMapping("/{taskId}/reopen")
    public ResponseEntity<TaskResponse> reopen(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable String taskId
    ) {
        return ResponseEntity.ok(
                TaskResponse.from(
                        reopenTaskUseCase.reopen(
                                new ReopenTaskCommand(taskId, String.valueOf(principal.userId()))
                        )
                )
        );
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable String taskId
    ) {
        deleteTaskUseCase.delete(new DeleteTaskCommand(taskId, String.valueOf(principal.userId())));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/grass")
    public ResponseEntity<TaskGrassResponse> getGrass(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok().body(
                TaskGrassResponse.from(
                        getTaskGrassUseCase.getGrass(
                                new GetTaskGrassCommand(authenticatedOwner(principal), from, to)
                        )
                )
        );
    }

    private TaskOwner authenticatedOwner(JwtPrincipal principal) {
        return TaskOwner.user(String.valueOf(principal.userId()));
    }
}
