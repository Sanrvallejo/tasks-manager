package com.sanr.task_manager.services;

import com.sanr.task_manager.model.Task;
import com.sanr.task_manager.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

    public List<Task> saveMultipleTasks(List<Task> tasks) {
        return taskRepository.saveAll(tasks);
    }

    public Task updateTask(Long id, Task taskDetails) {
        return taskRepository.findById(id)
                .map(task -> {
                    if (taskDetails.isCompleted() && !areSubtasksCompleted(task.getSubtasks())) {
                        throw new IllegalStateException("Subtasks are not completed");
                    }
                    task.setTitle(taskDetails.getTitle());
                    task.setDescription(taskDetails.getDescription());
                    task.setCompleted(taskDetails.isCompleted());
                    return taskRepository.save(task);
                })
                .orElse(null);
    }

    @Transactional
    public Task addSubtasks(Long id, List<Long> subtasksToAdd) {
        return taskRepository.findById(id)
                .map(task -> {
                    for (Long subTaskId: subtasksToAdd) {
                        if (!taskRepository.existsById(subTaskId)) {
                            throw new IllegalStateException("Subtask does not exist");
                        }

                        if (task.getId().equals(subTaskId)) {
                            throw new IllegalStateException("A task cannot be its own subtask.");
                        }
                    }

                    List<Long> currentSubtasks = task.getSubtasks();
                    subtasksToAdd.stream()
                            .filter(subtaskId -> !currentSubtasks.contains(subtaskId))
                            .forEach(currentSubtasks::add);

                    task.setSubtasks(currentSubtasks);

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

    public Map<String, Long> getTaskStatics() {
        List<Task> allTasks = taskRepository.findAll();

        long totalTasks = allTasks.stream().count();

        long completedTasks = allTasks.stream()
                .filter(task -> task.isCompleted())
                .count();

        long taskWithDescription = allTasks.stream()
                .filter(task ->
                    task.getDescription() != null && !task.getDescription().isBlank())
                .count();

        return Map.of(
                "totalTasks", totalTasks,
                "completedTasks" ,completedTasks,
                "tasksWithDescription", taskWithDescription
        );
    }

    public boolean areSubtasksCompleted(List<Long> subtaskIds) {
       if (subtaskIds == null || subtaskIds.isEmpty()) {
           return true;
       }

       return subtaskIds.stream()
               .allMatch(id -> taskRepository.findById(id)
                       .map(task -> task.isCompleted())
                       .orElse(false));
    }
}
