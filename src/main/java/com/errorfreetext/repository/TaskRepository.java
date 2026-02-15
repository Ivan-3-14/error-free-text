package com.errorfreetext.repository;

import com.errorfreetext.entity.Task;
import com.errorfreetext.entity.enums.TaskStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for {@link Task} entity operations.
 * <p>
 * Provides database access methods for managing text correction tasks.
 * Extends Spring Data JPA's {@link JpaRepository} to inherit basic CRUD operations
 * like save, findById, findAll, delete, etc.
 * </p>
 *
 * <p>
 * <b>Custom query methods:</b>
 * <ul>
 *   <li>{@link #findByStatusOrderByCreatedAtAsc} - Retrieves tasks by status with FIFO ordering</li>
 *   <li>{@link #findByStatusAndUpdatedAtBefore} - Finds stuck tasks for recovery</li>
 * </ul>
 * </p>
 *
 * @see com.errorfreetext.entity.Task
 * @see com.errorfreetext.service.TaskService
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Finds tasks with the specified status, ordered by creation date (oldest first).
     * <p>
     * Used by the scheduler to retrieve pending tasks for processing in FIFO order.
     * The {@link Pageable} parameter limits the number of tasks returned to prevent
     * memory issues when there are many pending tasks.
     * </p>
     *
     * <p>
     * <b>Example usage:</b>
     * <pre>
     * Pageable limit = PageRequest.of(0, 10);
     * List&lt;Task&gt; pendingTasks = repository.findByStatusOrderByCreatedAtAsc(
     *     TaskStatus.PENDING, limit
     * );
     * </pre>
     * </p>
     *
     * @param status   the {@link TaskStatus} to filter by (typically PENDING)
     * @param pageable pagination information including page size
     * @return a list of tasks with the given status, ordered by creation date (oldest first),
     *         limited by the provided page size
     */
    List<Task> findByStatusOrderByCreatedAtAsc(TaskStatus status, Pageable pageable);

    /**
     * Finds tasks with the specified status that haven't been updated since the given cutoff time.
     * <p>
     * Used to identify tasks stuck in PROCESSING state for too long. These tasks may
     * have been abandoned due to application crashes, network timeouts, or other errors.
     * The recovery scheduler uses this method to find and recover such tasks.
     * </p>
     *
     * <p>
     * <b>Example usage:</b>
     * <pre>
     * LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
     * List&lt;Task&gt; stuckTasks = repository.findByStatusAndUpdatedAtBefore(
     *     TaskStatus.PROCESSING, cutoff
     * );
     * </pre>
     * </p>
     *
     * @param status     the {@link TaskStatus} to filter by (typically PROCESSING)
     * @param updatedAt  the cutoff time; tasks updated before this time are considered stuck
     * @return a list of tasks with the given status that haven't been updated since the cutoff time
     */
    List<Task> findByStatusAndUpdatedAtBefore(TaskStatus status, LocalDateTime updatedAt);
}