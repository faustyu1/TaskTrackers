package ru.faustyu.tasktrackers.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.faustyu.tasktrackers.data.model.Task
import ru.faustyu.tasktrackers.data.model.TaskTagCrossRef
import ru.faustyu.tasktrackers.data.model.TaskWithTags

@Dao
interface TaskDao {

    @Transaction
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasksWithTags(): Flow<List<TaskWithTags>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE taskId = :taskId")
    fun getTaskWithTagsById(taskId: Long): Flow<TaskWithTags?>

    @Transaction
    @Query("SELECT * FROM tasks WHERE taskId = :taskId")
    suspend fun getTaskWithTagsByIdOnce(taskId: Long): TaskWithTags?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE taskId = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskTagCrossRef(crossRef: TaskTagCrossRef)

    @Delete
    suspend fun deleteTaskTagCrossRef(crossRef: TaskTagCrossRef)

    @Query("DELETE FROM task_tag_cross_ref WHERE taskId = :taskId")
    suspend fun deleteAllTagsForTask(taskId: Long)

    @Transaction
    suspend fun insertTaskWithTags(task: Task, tagIds: List<Long>): Long {
        val taskId = insertTask(task)
        tagIds.forEach { tagId ->
            insertTaskTagCrossRef(TaskTagCrossRef(taskId, tagId))
        }
        return taskId
    }

    @Transaction
    suspend fun updateTaskWithTags(task: Task, tagIds: List<Long>) {
        updateTask(task)
        deleteAllTagsForTask(task.taskId)
        tagIds.forEach { tagId ->
            insertTaskTagCrossRef(TaskTagCrossRef(task.taskId, tagId))
        }
    }
}
