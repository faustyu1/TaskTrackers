package ru.faustyu.tasktrackers.data.repository

import kotlinx.coroutines.flow.Flow
import ru.faustyu.tasktrackers.data.dao.TagDao
import ru.faustyu.tasktrackers.data.dao.TaskDao
import ru.faustyu.tasktrackers.data.model.*

class TaskRepository(
    private val taskDao: TaskDao,
    private val tagDao: TagDao
) {
    // Tasks
    fun getAllTasksWithTags(): Flow<List<TaskWithTags>> = taskDao.getAllTasksWithTags()

    fun getTaskWithTagsById(taskId: Long): Flow<TaskWithTags?> = taskDao.getTaskWithTagsById(taskId)

    suspend fun getTaskWithTagsByIdOnce(taskId: Long): TaskWithTags? = taskDao.getTaskWithTagsByIdOnce(taskId)

    suspend fun addTask(title: String, description: String = "", tagIds: List<Long>): Long {
        val task = Task(title = title, description = description)
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
}
