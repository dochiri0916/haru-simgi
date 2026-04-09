package com.dochiri.taskservice.infrastructure.adapter.out.persistence;

import com.dochiri.taskservice.domain.Task;
import com.dochiri.taskservice.domain.TaskOwner;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class TaskMapper {

    public Task toDomain(TaskEntity entity) {
        requireNonNull(entity);
        return Task.from(
                entity.getPublicId(),
                new TaskOwner(entity.getOwnerType(), entity.getOwnerReferenceId()),
                entity.getTitle(),
                entity.isCompleted(),
                entity.getCompletedAt()
        );
    }

    public TaskEntity toEntity(Task domain) {
        requireNonNull(domain);
        return TaskEntity.from(
                domain.getId(),
                domain.getOwner().type(),
                domain.getOwner().referenceId(),
                domain.getTitle(),
                domain.isCompleted(),
                domain.getCompletedAt()
        );
    }

}