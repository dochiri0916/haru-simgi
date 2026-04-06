package com.dochiri.taskservice.application.port.out;

import com.dochiri.taskservice.domain.Task;
import com.dochiri.taskservice.domain.TaskOwner;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    Task save(Task task);

    Optional<Task> findById(String publicId);

    Task loadById(String publicId);

    List<Task> findAllByOwner(TaskOwner owner);

    int migrateOwner(TaskOwner sourceOwner, TaskOwner targetOwner);
}
