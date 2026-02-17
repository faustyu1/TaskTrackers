package ru.faustyu.tasktrackers.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation

@Entity(
    tableName = "task_tag_cross_ref",
    primaryKeys = ["taskId", "tagId"],
    indices = [androidx.room.Index("tagId")]
)
data class TaskTagCrossRef(
    val taskId: Long,
    val tagId: Long
)

data class TaskWithTags(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "taskId",
        entityColumn = "tagId",
        associateBy = Junction(TaskTagCrossRef::class)
    )
    val tags: List<Tag>,
    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val subtasks: List<Subtask> = emptyList()
)
