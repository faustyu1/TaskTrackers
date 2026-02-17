package ru.faustyu.tasktrackers.data.repository

import kotlinx.coroutines.flow.Flow
import ru.faustyu.tasktrackers.data.dao.SubtaskDao
import ru.faustyu.tasktrackers.data.dao.TagDao
import ru.faustyu.tasktrackers.data.dao.TaskDao
import ru.faustyu.tasktrackers.data.model.*

class TaskRepository(
    private val taskDao: TaskDao,
    private val tagDao: TagDao,
    private val subtaskDao: SubtaskDao
) {
    // Tasks
    fun getAllTasksWithTags(): Flow<List<TaskWithTags>> = taskDao.getAllTasksWithTags()

    fun getArchivedTasksWithTags(): Flow<List<TaskWithTags>> = taskDao.getArchivedTasksWithTags()

    fun getTaskWithTagsById(taskId: Long): Flow<TaskWithTags?> = taskDao.getTaskWithTagsById(taskId)

    suspend fun getTaskWithTagsByIdOnce(taskId: Long): TaskWithTags? = taskDao.getTaskWithTagsByIdOnce(taskId)

    suspend fun addTask(
        title: String,
        description: String = "",
        tagIds: List<Long>,
        dueDate: Long? = null,
        colorHex: String? = null,
        repeatMode: String = "none"
    ): Long {
        val task = Task(
            title = title,
            description = description,
            dueDate = dueDate,
            colorHex = colorHex,
            repeatMode = repeatMode
        )
        return taskDao.insertTaskWithTags(task, tagIds)
    }

    suspend fun updateTask(task: Task, tagIds: List<Long>) {
        taskDao.updateTaskWithTags(task, tagIds)
    }

    suspend fun toggleTaskCompletion(task: Task) {
        taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteAllTagsForTask(task.taskId)
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(taskId: Long) {
        taskDao.deleteAllTagsForTask(taskId)
        taskDao.deleteTaskById(taskId)
    }

    suspend fun archiveTask(taskId: Long) {
        taskDao.archiveTask(taskId)
    }

    suspend fun unarchiveTask(taskId: Long) {
        taskDao.unarchiveTask(taskId)
    }

    suspend fun updateManualSortOrder(taskId: Long, sortOrder: Int) {
        taskDao.updateManualSortOrder(taskId, sortOrder)
    }

    suspend fun getAllTasksForExport(): List<TaskWithTags> {
        return taskDao.getAllTasksForExport()
    }

    suspend fun insertTaskRaw(task: Task): Long {
        return taskDao.insertTask(task)
    }

    suspend fun insertTaskTagCrossRef(crossRef: TaskTagCrossRef) {
        taskDao.insertTaskTagCrossRef(crossRef)
    }

    // Tags
    fun getAllTags(): Flow<List<Tag>> = tagDao.getAllTags()

    suspend fun getAllTagsOnce(): List<Tag> = tagDao.getAllTagsOnce()

    suspend fun addCustomTag(name: String, nameRu: String, group: TagGroup, colorHex: String): Long {
        val tag = Tag(
            name = name,
            nameRu = nameRu,
            group = group,
            colorHex = colorHex,
            isCustom = true,
            sortOrder = 99
        )
        return tagDao.insertTag(tag)
    }

    suspend fun deleteTag(tagId: Long) {
        tagDao.deleteTagById(tagId)
    }

    suspend fun insertTag(tag: Tag): Long {
        return tagDao.insertTag(tag)
    }

    // Subtasks
    fun getSubtasksForTask(taskId: Long): Flow<List<Subtask>> =
        subtaskDao.getSubtasksForTask(taskId)

    suspend fun addSubtask(taskId: Long, title: String): Long {
        val count = subtaskDao.getSubtaskCount(taskId)
        return subtaskDao.insertSubtask(
            Subtask(taskId = taskId, title = title, sortOrder = count)
        )
    }

    suspend fun toggleSubtask(subtask: Subtask) {
        subtaskDao.updateSubtask(subtask.copy(isCompleted = !subtask.isCompleted))
    }

    suspend fun deleteSubtask(subtaskId: Long) {
        subtaskDao.deleteSubtaskById(subtaskId)
    }

    suspend fun updateSubtask(subtask: Subtask) {
        subtaskDao.updateSubtask(subtask)
    }
}
