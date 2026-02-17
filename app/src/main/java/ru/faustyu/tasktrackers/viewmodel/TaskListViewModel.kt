package ru.faustyu.tasktrackers.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import ru.faustyu.tasktrackers.data.AppDatabase
import ru.faustyu.tasktrackers.data.model.*
import ru.faustyu.tasktrackers.data.repository.TaskRepository

enum class SortMode {
    DATE_DESC,
    DATE_ASC,
    ALPHA_ASC,
    ALPHA_DESC,
    IMPORTANCE,
    URGENCY,
    SPHERE,
    DUE_DATE,
    MANUAL
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
    val repository = TaskRepository(database.taskDao(), database.tagDao(), database.subtaskDao())

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
                (searchQuery.isBlank() || taskWithTags.task.title.contains(searchQuery, ignoreCase = true)) &&
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
            SortMode.DUE_DATE -> tasks.sortedBy { it.task.dueDate ?: Long.MAX_VALUE }
            SortMode.MANUAL -> tasks.sortedBy { it.task.manualSortOrder }
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
            // Handle recurring tasks
            if (!taskWithTags.task.isCompleted && taskWithTags.task.repeatMode != "none") {
                recreateRecurringTask(taskWithTags)
            }
        }
    }

    private suspend fun recreateRecurringTask(taskWithTags: TaskWithTags) {
        val task = taskWithTags.task
        val nextDueDate = task.dueDate?.let { due ->
            when (task.repeatMode) {
                "daily" -> due + 86_400_000L
                "weekly" -> due + 604_800_000L
                "monthly" -> due + 2_592_000_000L
                else -> null
            }
        } ?: return

        // Prevent duplicates: check if a task with valid duplication criteria already exists
        val allTasks = repository.getAllTasksForExport()
        val alreadyExists = allTasks.any {
            it.task.title == task.title &&
            it.task.repeatMode == task.repeatMode &&
            !it.task.isCompleted &&
            !it.task.isArchived &&
            it.task.dueDate == nextDueDate
        }

        if (alreadyExists) return

        val tagIds = taskWithTags.tags.map { it.tagId }
        repository.addTask(
            title = task.title,
            description = task.description,
            tagIds = tagIds,
            dueDate = nextDueDate,
            colorHex = task.colorHex,
            repeatMode = task.repeatMode
        )
    }

    fun deleteTask(taskWithTags: TaskWithTags) {
        viewModelScope.launch {
            repository.deleteTask(taskWithTags.task)
        }
    }

    fun addTask(
        title: String,
        description: String = "",
        tagIds: List<Long>,
        dueDate: Long? = null,
        colorHex: String? = null,
        repeatMode: String = "none"
    ) {
        viewModelScope.launch {
            repository.addTask(title, description, tagIds, dueDate, colorHex, repeatMode)
        }
    }

    fun updateTask(taskId: Long, title: String, description: String, tagIds: List<Long>,
                   dueDate: Long? = null, colorHex: String? = null, repeatMode: String = "none") {
        viewModelScope.launch {
            val existing = repository.getTaskWithTagsByIdOnce(taskId) ?: return@launch
            val updatedTask = existing.task.copy(
                title = title,
                description = description,
                dueDate = dueDate,
                colorHex = colorHex,
                repeatMode = repeatMode
            )
            repository.updateTask(updatedTask, tagIds)
        }
    }

    fun addCustomTag(name: String, group: TagGroup, colorHex: String) {
        viewModelScope.launch {
            repository.addCustomTag(name, name, group, colorHex)
        }
    }

    fun deleteCustomTag(tagId: Long) {
        viewModelScope.launch {
            repository.deleteTag(tagId)
        }
    }

    val archivedTasks: StateFlow<List<TaskWithTags>> = repository.getArchivedTasksWithTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun archiveTask(taskId: Long) {
        viewModelScope.launch {
            repository.archiveTask(taskId)
        }
    }

    fun unarchiveTask(taskId: Long) {
        viewModelScope.launch {
            repository.unarchiveTask(taskId)
        }
    }

    // Subtasks
    fun addSubtask(taskId: Long, title: String) {
        viewModelScope.launch {
            repository.addSubtask(taskId, title)
        }
    }

    fun toggleSubtask(subtask: Subtask) {
        viewModelScope.launch {
            repository.toggleSubtask(subtask)
        }
    }

    fun deleteSubtask(subtaskId: Long) {
        viewModelScope.launch {
            repository.deleteSubtask(subtaskId)
        }
    }

    // Drag & drop sort order
    fun updateSortOrders(taskIds: List<Long>) {
        viewModelScope.launch {
            taskIds.forEachIndexed { index, taskId ->
                repository.updateManualSortOrder(taskId, index)
            }
        }
    }

    // Export
    suspend fun exportToJson(): String {
        val allTasks = repository.getAllTasksForExport()
        val allTags = repository.getAllTagsOnce()

        val root = JSONObject()

        val tasksArray = JSONArray()
        allTasks.forEach { twt ->
            val obj = JSONObject()
            obj.put("title", twt.task.title)
            obj.put("description", twt.task.description)
            obj.put("isCompleted", twt.task.isCompleted)
            obj.put("isArchived", twt.task.isArchived)
            obj.put("createdAt", twt.task.createdAt)
            obj.put("dueDate", twt.task.dueDate ?: JSONObject.NULL)
            obj.put("colorHex", twt.task.colorHex ?: JSONObject.NULL)
            obj.put("repeatMode", twt.task.repeatMode)
            val tagNames = JSONArray()
            twt.tags.forEach { tag -> tagNames.put(tag.name) }
            obj.put("tags", tagNames)
            val subtasksArr = JSONArray()
            twt.subtasks.forEach { sub ->
                val subObj = JSONObject()
                subObj.put("title", sub.title)
                subObj.put("isCompleted", sub.isCompleted)
                subtasksArr.put(subObj)
            }
            obj.put("subtasks", subtasksArr)
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        val tagsArray = JSONArray()
        allTags.filter { it.isCustom }.forEach { tag ->
            val obj = JSONObject()
            obj.put("name", tag.name)
            obj.put("nameRu", tag.nameRu)
            obj.put("group", tag.group.name)
            obj.put("colorHex", tag.colorHex)
            tagsArray.put(obj)
        }
        root.put("customTags", tagsArray)

        return root.toString(2)
    }

    fun importFromJson(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@launch
                val json = inputStream.bufferedReader().use { it.readText() }
                inputStream.close()

                val root = JSONObject(json)
                val allTags = repository.getAllTagsOnce().toMutableList()

                // Import custom tags first
                if (root.has("customTags")) {
                    val customTags = root.getJSONArray("customTags")
                    for (i in 0 until customTags.length()) {
                        val obj = customTags.getJSONObject(i)
                        val name = obj.getString("name")
                        if (allTags.none { it.name == name }) {
                            val tagId = repository.insertTag(Tag(
                                name = name,
                                nameRu = obj.getString("nameRu"),
                                group = TagGroup.valueOf(obj.getString("group")),
                                colorHex = obj.getString("colorHex"),
                                isCustom = true,
                                sortOrder = 99
                            ))
                            allTags.add(Tag(tagId = tagId, name = name, nameRu = obj.getString("nameRu"),
                                group = TagGroup.valueOf(obj.getString("group")),
                                colorHex = obj.getString("colorHex"), isCustom = true, sortOrder = 99))
                        }
                    }
                }

                // Import tasks
                if (root.has("tasks")) {
                    val tasks = root.getJSONArray("tasks")
                    for (i in 0 until tasks.length()) {
                        val obj = tasks.getJSONObject(i)
                        val task = Task(
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            isArchived = obj.optBoolean("isArchived", false),
                            dueDate = if (obj.isNull("dueDate")) null else obj.optLong("dueDate"),
                            colorHex = if (obj.isNull("colorHex")) null else obj.optString("colorHex"),
                            repeatMode = obj.optString("repeatMode", "none"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                        val taskId = repository.insertTaskRaw(task)

                        // Link tags
                        if (obj.has("tags")) {
                            val tagNames = obj.getJSONArray("tags")
                            for (j in 0 until tagNames.length()) {
                                val tagName = tagNames.getString(j)
                                val tag = allTags.find { it.name == tagName }
                                if (tag != null) {
                                    repository.insertTaskTagCrossRef(TaskTagCrossRef(taskId, tag.tagId))
                                }
                            }
                        }

                        // Import subtasks
                        if (obj.has("subtasks")) {
                            val subtasks = obj.getJSONArray("subtasks")
                            for (j in 0 until subtasks.length()) {
                                val subObj = subtasks.getJSONObject(j)
                                repository.addSubtask(taskId, subObj.getString("title"))
                                // Toggle if completed
                                if (subObj.optBoolean("isCompleted", false)) {
                                    val subs = repository.getSubtasksForTask(taskId).first()
                                    subs.lastOrNull()?.let { repository.toggleSubtask(it) }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
