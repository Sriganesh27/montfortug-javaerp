package com.erp.montfortuganda.core.task.repository;

import com.erp.montfortuganda.auth.entity.User;
import com.erp.montfortuganda.core.task.entity.ErpTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErpTaskRepository extends JpaRepository<ErpTask, Long> {

    List<ErpTask> findByReferenceTypeAndReferenceIdAndActiveTrueAndDeletedFalse(String referenceType, Long referenceId);

    List<ErpTask> findByReferenceTypeAndReferenceIdAndStatusAndActiveTrueAndDeletedFalse(String referenceType, Long referenceId, ErpTask.TaskStatus status);

    List<ErpTask> findByAssignedToAndStatusAndActiveTrueAndDeletedFalse(User assignedTo, ErpTask.TaskStatus status);

    List<ErpTask> findByAssignedRoleAndStatusAndActiveTrueAndDeletedFalse(String assignedRole, ErpTask.TaskStatus status);

    List<ErpTask> findByModuleAndStatusAndActiveTrueAndDeletedFalse(String module, ErpTask.TaskStatus status);

    Optional<ErpTask> findByTaskIdAndActiveTrueAndDeletedFalse(Long taskId);
}