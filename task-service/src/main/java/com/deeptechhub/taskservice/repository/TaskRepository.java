package com.deeptechhub.taskservice.repository;

import com.deeptechhub.taskservice.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCreatedByUserId(Long userId); // Custom query

}
