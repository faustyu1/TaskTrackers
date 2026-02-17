package ru.faustyu.tasktrackers.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.faustyu.tasktrackers.data.AppDatabase
import ru.faustyu.tasktrackers.data.model.Tag
import ru.faustyu.tasktrackers.data.model.TagGroup
import ru.faustyu.tasktrackers.data.model.TaskWithTags
import ru.faustyu.tasktrackers.data.repository.TaskRepository

enum class SortMode {
    DATE_DESC,
    DATE_ASC,
    ALPHA_ASC,
    ALPHA_DESC,
    IMPORTANCE,
    URGENCY,
    SPHERE
}

data class TaskListUiState(
    val tasks: List<TaskWithTags> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val selectedFilterTags: Set<Long> = emptySet(),
    val sortMode: SortMode = SortMode.DATE_DESC,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

class TaskListViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = TaskRepository(database.taskDao(), database.tagDao())

    private val _selectedFilterTags = MutableStateFlow<Set<Long>>(emptySet())
    private val _sortMode = MutableStateFlow(SortMode.DATE_DESC)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<TaskListUiState> = combine(
        repository.getAllTasksWithTags(),
        repository.getAllTags(),
        _selectedFilterTags,
        _sortMode,
        _searchQuery
    ) { tasks, tags, filterTags, sortMode, searchQuery ->
        val filtered = tasks
            .filter { taskWithTags ->
                // Search filter
                (searchQuery.isBlank() || taskWithTags.task.title.contains(searchQuery, ignoreCase = true)) &&
                // Tag filter: task must have ALL selected filter tags
                (filterTags.isEmpty() || filterTags.all { filterId ->
                    taskWithTags.tags.any { it.tagId == filterId }
                })
            }
            .let { list -> sortTasks(list, sortMode) }

        TaskListUiState(
            tasks = filtered,
            allTags = tags,
            selectedFilterTags = filterTags,
            sortMode = sortMode,
            searchQuery = searchQuery,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TaskListUiState()
    )

    private fun sortTasks(tasks: List<TaskWithTags>, sortMode: SortMode): List<TaskWithTags> {
        val importanceOrder = mapOf("Critical" to 0, "High" to 1, "Medium" to 2, "Low" to 3)
        val urgencyOrder = mapOf("On Fire" to 0, "Urgent" to 1, "Not Urgent" to 2)
        val sphereOrder = mapOf("Work" to 0, "Personal" to 1, "Home" to 2, "Shopping" to 3, "Health" to 4, "Finance" to 5, "Education" to 6)

        return when (sortMode) {
            SortMode.DATE_DESC -> tasks.sortedByDescending { it.task.createdAt }
            SortMode.DATE_ASC -> tasks.sortedBy { it.task.createdAt }
            SortMode.ALPHA_ASC -> tasks.sortedBy { it.task.title.lowercase() }
            SortMode.ALPHA_DESC -> tasks.sortedByDescending { it.task.title.lowercase() }
            SortMode.IMPORTANCE -> tasks.sortedBy { taskWithTags ->
                taskWithTags.tags
                    .filter { it.group == TagGroup.IMPORTANCE }
                    .minOfOrNull { importanceOrder[it.name] ?: 99 } ?: 99
            }
            SortMode.URGENCY -> tasks.sortedBy { taskWithTags ->
                taskWithTags.tags
                    .filter { it.group == TagGroup.URGENCY }
                    .minOfOrNull { urgencyOrder[it.name] ?: 99 } ?: 99
            }
            SortMode.SPHERE -> tasks.sortedBy { taskWithTags ->
                taskWithTags.tags
                    .filter { it.group == TagGroup.SPHERE }
                    .minOfOrNull { sphereOrder[it.name] ?: 99 } ?: 99
            }
        }
    }

    fun toggleFilterTag(tagId: Long) {
        _selectedFilterTags.update { current ->
            if (tagId in current) current - tagId else current + tagId
        }
    }

    fun clearFilters() {
        _selectedFilterTags.value = emptySet()
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTaskCompletion(taskWithTags: TaskWithTags) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(taskWithTags.task)
        }
    }

    fun deleteTask(taskWithTags: TaskWithTags) {
        viewModelScope.launch {
            repository.deleteTask(taskWithTags.task)
        }
    }

    fun addTask(title: String, description: String = "", tagIds: List<Long>) {
        viewModelScope.launch {
            repository.addTask(title, description, tagIds)
        }
    }

    fun updateTask(taskId: Long, title: String, description: String, tagIds: List<Long>) {
        viewModelScope.launch {
            val existing = repository.getTaskWithTagsByIdOnce(taskId) ?: return@launch
            val updatedTask = existing.task.copy(title = title, description = description)
            repository.updateTask(updatedTask, tagIds)
        }
    }

    fun addCustomTag(name: String, nameRu: String, group: TagGroup, colorHex: String) {
        viewModelScope.launch {
            repository.addCustomTag(name, nameRu, group, colorHex)
        }
    }

    fun deleteCustomTag(tagId: Long) {
        viewModelScope.launch {
            repository.deleteTag(tagId)
        }
    }
}
