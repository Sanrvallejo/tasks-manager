package com.sanr.task_manager.services;

import com.sanr.task_manager.model.Task;
import com.sanr.task_manager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks(Boolean completed) {
        if (completed == null) {
            return taskRepository.findAll();
        }else {
            return taskRepository.findByCompleted(completed);
        }
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task taskDetails) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.setTitle(taskDetails.getTitle());
                    task.setDescription(taskDetails.getDescription());
                    task.setCompleted(taskDetails.isCompleted());
                    return taskRepository.save(task);
                })
                .orElse(null);
    }

    public boolean deleteTask(Long id) {
        return taskRepository.findById(id)
                .map(task ->  {
                    taskRepository.delete(task);
                    return true;
                }).orElse(false);
    }
}
