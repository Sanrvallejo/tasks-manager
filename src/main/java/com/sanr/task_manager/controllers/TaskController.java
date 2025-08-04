package com.sanr.task_manager.controllers;

import com.sanr.task_manager.model.Task;
import com.sanr.task_manager.services.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(@RequestParam(name = "completed", required = false) Boolean completed) {
        List<Task> tasks = taskService.getAllTasks(completed);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable("id") Long id) {
        return taskService.getTaskById(id)
                .map(task -> new ResponseEntity<>(task, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getTaskStatics() {
        Map<String, Long> taskStatics = taskService.getTaskStatics();
        return new ResponseEntity<>(taskStatics, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task createdTask = taskService.createTask(task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @PostMapping("/many-tasks")
    public ResponseEntity<List<Task>> saveMultiple(@RequestBody List<Task> tasks) {
        List<Task> saveTasks = taskService.saveMultipleTasks(tasks);
        return new ResponseEntity<>(saveTasks, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable("id") Long id, @RequestBody Task taskDetails) {
        Task updatedTask = taskService.updateTask(id, taskDetails);
        if (updatedTask != null) {
            return new ResponseEntity<>(updatedTask, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/subtasks/{id}")
    public ResponseEntity<Task> addSubtasks(@PathVariable("id") Long id,
                                            @RequestBody List<Long> subtasksToAdd) {
        try {
            Task updatedtask = taskService.addSubtasks(id, subtasksToAdd);

            if (updatedtask != null) {
                return new ResponseEntity<>(updatedtask, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (taskService.deleteTask(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


}
