package ru.faustyu.tasktrackers.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TagGroup {
    READINESS,
    IMPORTANCE,
    URGENCY,
    SPHERE,
    CUSTOM
}

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val tagId: Long = 0,
    val name: String,
    val nameRu: String,
    val group: TagGroup,
    val colorHex: String,
    val isCustom: Boolean = false,
    val sortOrder: Int = 0
)
