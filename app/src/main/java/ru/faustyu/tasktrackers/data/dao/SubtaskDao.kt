package ru.faustyu.tasktrackers.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.faustyu.tasktrackers.data.model.Subtask

@Dao
interface SubtaskDao {

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY sortOrder, subtaskId")
    fun getSubtasksForTask(taskId: Long): Flow<List<Subtask>>

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY sortOrder, subtaskId")
    suspend fun getSubtasksForTaskOnce(taskId: Long): List<Subtask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: Subtask): Long

    @Update
    suspend fun updateSubtask(subtask: Subtask)

    @Delete
    suspend fun deleteSubtask(subtask: Subtask)

    @Query("DELETE FROM subtasks WHERE subtaskId = :subtaskId")
    suspend fun deleteSubtaskById(subtaskId: Long)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteAllSubtasksForTask(taskId: Long)

    @Query("SELECT COUNT(*) FROM subtasks WHERE taskId = :taskId")
    suspend fun getSubtaskCount(taskId: Long): Int

    @Query("SELECT COUNT(*) FROM subtasks WHERE taskId = :taskId AND isCompleted = 1")
    suspend fun getCompletedSubtaskCount(taskId: Long): Int
}
