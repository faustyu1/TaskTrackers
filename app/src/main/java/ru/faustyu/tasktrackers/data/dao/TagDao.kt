package ru.faustyu.tasktrackers.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.faustyu.tasktrackers.data.model.Tag
import ru.faustyu.tasktrackers.data.model.TagGroup

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY `group`, sortOrder, name")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags ORDER BY `group`, sortOrder, name")
    suspend fun getAllTagsOnce(): List<Tag>

    @Query("SELECT * FROM tags WHERE `group` = :group ORDER BY sortOrder, name")
    fun getTagsByGroup(group: TagGroup): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE tagId = :tagId")
    suspend fun getTagById(tagId: Long): Tag?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<Tag>)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("DELETE FROM tags WHERE tagId = :tagId")
    suspend fun deleteTagById(tagId: Long)

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getTagCount(): Int
}
