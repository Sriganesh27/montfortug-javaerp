package com.erp.montfortuganda.core.task.service;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.core.task.entity.ErpTask;
import com.erp.montfortuganda.core.task.repository.ErpTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final ErpTaskRepository taskRepository;

    @Transactional
    public ErpTask createTask(
            String module,
            String referenceType,
            Long referenceId,
            String taskType,
            String actionCode,
            String title,
            String description,
            ErpTask.TaskPriority priority,
            String assignedRole,
            ErpTask parentTask,
            Integer sequenceNo,
            Long dueInDays) {

        ErpTask task = new ErpTask();
        task.setModule(module);
        task.setReferenceType(referenceType);
        task.setReferenceId(referenceId);
        task.setTaskType(taskType);
        task.setActionCode(actionCode);
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority != null ? priority : ErpTask.TaskPriority.NORMAL);
        task.setAssignedRole(assignedRole);
        task.setStatus(ErpTask.TaskStatus.PENDING);
        task.setParentTask(parentTask);
        task.setSequenceNo(sequenceNo != null ? sequenceNo : 0);

        if (dueInDays != null) {
            task.setDueDate(LocalDateTime.now().plusDays(dueInDays));
        }

        return taskRepository.save(task);
    }

    @Transactional
    public ErpTask completeTask(Long taskId, User completedByUser, String remarks) {
        ErpTask task = taskRepository.findByTaskIdAndActiveTrueAndDeletedFalse(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found or is inactive with ID: " + taskId));

        if (task.getStatus() == ErpTask.TaskStatus.COMPLETED) {
            return task;
        }

        task.setStatus(ErpTask.TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        task.setCompletedBy(completedByUser);

        if (remarks != null && !remarks.trim().isEmpty()) {
            task.setRemarks(remarks);
        }

        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<ErpTask> getPendingTasksForReference(String referenceType, Long referenceId) {
        return taskRepository.findByReferenceTypeAndReferenceIdAndStatusAndActiveTrueAndDeletedFalse(
                referenceType, referenceId, ErpTask.TaskStatus.PENDING);
    }

    @Transactional
    public void completeAllPendingTasksForReference(String referenceType, Long referenceId, User completedByUser, String remarks) {
        List<ErpTask> pendingTasks = getPendingTasksForReference(referenceType, referenceId);
        for (ErpTask task : pendingTasks) {
            completeTask(task.getTaskId(), completedByUser, remarks);
        }
    }

    @Transactional
    public void cancelAllPendingTasksForReference(String referenceType, Long referenceId, String remarks) {
        List<ErpTask> pendingTasks = getPendingTasksForReference(referenceType, referenceId);
        for (ErpTask task : pendingTasks) {
            task.setStatus(ErpTask.TaskStatus.CANCELLED);
            task.setRemarks(remarks);
            taskRepository.save(task);
        }
    }
}