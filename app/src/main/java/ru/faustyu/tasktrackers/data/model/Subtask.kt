package ru.faustyu.tasktrackers.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subtasks",
    foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = ["taskId"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["taskId"])]
)
data class Subtask(
    @PrimaryKey(autoGenerate = true)
    val subtaskId: Long = 0,
    val taskId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0
)
