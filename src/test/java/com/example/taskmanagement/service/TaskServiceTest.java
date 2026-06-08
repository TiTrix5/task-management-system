package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.request.TaskRequest;
import com.example.taskmanagement.dto.response.TaskResponse;
import com.example.taskmanagement.entity.*;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.mapper.TaskMapper;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskMapper taskMapper;

    @InjectMocks private TaskService taskService;

    private User owner;
    private Task task;
    private UUID userId;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        owner = User.builder().id(userId).username("owner").email("o@e.com").build();

        task = Task.builder()
                .id(taskId)
                .title("Test")
                .status(TaskStatus.NEW)
                .priority(TaskPriority.MEDIUM)
                .user(owner)
                .build();
    }

    @Test
    void createTask_Success() {
        TaskRequest req = new TaskRequest("Title", "Desc", null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(taskMapper.toEntity(req)).thenReturn(task);
        when(taskRepository.save(any())).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(TaskResponse.builder().id(taskId).title("Test").userId(userId).build());

        TaskResponse res = taskService.createTask(req, userId);

        assertEquals("Test", res.getTitle());
    }

    @Test
    void getTaskById_NotOwned_Throws() {
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(taskId, userId));
    }

    @Test
    void deleteTask_Success() {
        when(taskRepository.existsByIdAndUserId(taskId, userId)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(taskId);

        assertDoesNotThrow(() -> taskService.deleteTask(taskId, userId));
    }

    @Test
    void getTasks_WithFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        when(taskRepository.findAll((org.springframework.data.jpa.domain.Specification<Task>) any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(task)));

        when(taskMapper.toResponse(task)).thenReturn(TaskResponse.builder().id(taskId).build());

        Page<TaskResponse> page = taskService.getTasks(userId, TaskStatus.NEW, null, pageable);
        assertEquals(1, page.getTotalElements());
    }
}