package com.dochiri.taskservice.infrastructure.adapter.in.web.external;

import com.dochiri.taskservice.application.service.CreateTaskService;
import com.dochiri.taskservice.infrastructure.adapter.in.web.external.request.CreateTaskRequest;
import com.dochiri.taskservice.infrastructure.adapter.in.web.external.response.CreateTaskResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final CreateTaskService createTaskService;

    @PostMapping
    public ResponseEntity<CreateTaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok().body(
                CreateTaskResponse.from(
                        createTaskService.create(
                                request.toCommand()
                        )
                )
        );
    }
}
