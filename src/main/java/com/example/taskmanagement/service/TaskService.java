package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.request.TaskRequest;
import com.example.taskmanagement.dto.response.TaskResponse;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.TaskPriority;
import com.example.taskmanagement.entity.TaskStatus;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.mapper.TaskMapper;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.repository.specification.TaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Transactional
    public TaskResponse createTask(TaskRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "id", userId));

        Task task = taskMapper.toEntity(request);
        task.setUser(user);
        if (task.getStatus() == null) task.setStatus(TaskStatus.NEW);
        if (task.getPriority() == null) task.setPriority(TaskPriority.MEDIUM);

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasks(UUID userId, TaskStatus status, TaskPriority priority, Pageable pageable) {
        Specification<Task> spec = TaskSpecification.build(userId, status, priority);
        return taskRepository.findAll(spec, pageable).map(taskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID id, UUID userId) {
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача", "id", id));
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(UUID id, TaskRequest request, UUID userId) {
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача", "id", id));

        taskMapper.updateEntityFromRequest(request, task);
        if (task.getStatus() == null) task.setStatus(TaskStatus.NEW);
        if (task.getPriority() == null) task.setPriority(TaskPriority.MEDIUM);

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(UUID id, UUID userId) {
        if (!taskRepository.existsByIdAndUserId(id, userId)) {
            throw new ResourceNotFoundException("Задача", "id", id);
        }
        taskRepository.deleteById(id);
    }
}