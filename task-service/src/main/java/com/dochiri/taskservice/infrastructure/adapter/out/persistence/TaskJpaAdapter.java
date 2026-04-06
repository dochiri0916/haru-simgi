package com.dochiri.taskservice.infrastructure.adapter.out.persistence;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.taskservice.application.error.TaskErrorCode;
import com.dochiri.taskservice.application.port.out.TaskRepository;
import com.dochiri.taskservice.domain.Task;
import com.dochiri.taskservice.domain.TaskOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TaskJpaAdapter implements TaskRepository {

    private final TaskJpaRepository taskJpaRepository;
    private final TaskMapper taskMapper;

    @Override
    public Task save(Task task) {
        Optional<TaskEntity> existingOptional = taskJpaRepository.findByPublicId(task.getId());

        if (existingOptional.isEmpty()) {
            TaskEntity newEntity = taskMapper.toEntity(task);
            TaskEntity saved = taskJpaRepository.save(newEntity);
            return taskMapper.toDomain(saved);
        }

        return taskMapper.toDomain(existingOptional.get());
    }

    @Override
    public Optional<Task> findById(String publicId) {
        return taskJpaRepository.findByPublicId(publicId)
                .map(taskMapper::toDomain);
    }

    @Override
    public Task loadById(String publicId) {
        return findById(publicId)
                .orElseThrow(() -> new BaseException(TaskErrorCode.TASK_NOT_FOUND));
    }

    @Override
    public List<Task> findAllByOwner(TaskOwner owner) {
        return taskJpaRepository.findAllByOwnerTypeAndOwnerReferenceId(owner.type(), owner.referenceId()).stream()
                .map(taskMapper::toDomain)
                .toList();
    }

    @Override
    public int migrateOwner(TaskOwner sourceOwner, TaskOwner targetOwner) {
        return 0;
    }
}
