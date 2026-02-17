package ru.faustyu.tasktrackers.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val taskId: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val isArchived: Boolean = false,
    val dueDate: Long? = null,
    val colorHex: String? = null,
    val repeatMode: String = "none", // none, daily, weekly, monthly
    val manualSortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
