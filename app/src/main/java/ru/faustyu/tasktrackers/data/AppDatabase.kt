package ru.faustyu.tasktrackers.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.faustyu.tasktrackers.data.dao.SubtaskDao
import ru.faustyu.tasktrackers.data.dao.TagDao
import ru.faustyu.tasktrackers.data.dao.TaskDao
import ru.faustyu.tasktrackers.data.model.Subtask
import ru.faustyu.tasktrackers.data.model.Tag
import ru.faustyu.tasktrackers.data.model.TagGroup
import ru.faustyu.tasktrackers.data.model.Task
import ru.faustyu.tasktrackers.data.model.TaskTagCrossRef

@Database(
    entities = [Task::class, Tag::class, TaskTagCrossRef::class, Subtask::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun tagDao(): TagDao
    abstract fun subtaskDao(): SubtaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_tracker_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN dueDate INTEGER")
                db.execSQL("ALTER TABLE tasks ADD COLUMN colorHex TEXT")
                db.execSQL("ALTER TABLE tasks ADD COLUMN repeatMode TEXT NOT NULL DEFAULT 'none'")
                db.execSQL("ALTER TABLE tasks ADD COLUMN manualSortOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS subtasks (
                        subtaskId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        taskId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (taskId) REFERENCES tasks(taskId) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_subtasks_taskId ON subtasks(taskId)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_task_tag_cross_ref_tagId ON task_tag_cross_ref(tagId)")
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultTags(database.tagDao())
                    }
                }
            }
        }

        private suspend fun populateDefaultTags(tagDao: TagDao) {
            val defaultTags = listOf(
                // Readiness
                Tag(name = "Not Started", nameRu = "Не начата", group = TagGroup.READINESS, colorHex = "#78909C", sortOrder = 0),
                Tag(name = "In Progress", nameRu = "В процессе", group = TagGroup.READINESS, colorHex = "#42A5F5", sortOrder = 1),
                Tag(name = "Done", nameRu = "Готова", group = TagGroup.READINESS, colorHex = "#66BB6A", sortOrder = 2),

                // Importance
                Tag(name = "Low", nameRu = "Низкая", group = TagGroup.IMPORTANCE, colorHex = "#A5D6A7", sortOrder = 0),
                Tag(name = "Medium", nameRu = "Средняя", group = TagGroup.IMPORTANCE, colorHex = "#FFD54F", sortOrder = 1),
                Tag(name = "High", nameRu = "Высокая", group = TagGroup.IMPORTANCE, colorHex = "#FF7043", sortOrder = 2),
                Tag(name = "Critical", nameRu = "Критическая", group = TagGroup.IMPORTANCE, colorHex = "#EF5350", sortOrder = 3),

                // Urgency
                Tag(name = "Not Urgent", nameRu = "Не срочно", group = TagGroup.URGENCY, colorHex = "#81C784", sortOrder = 0),
                Tag(name = "Urgent", nameRu = "Срочно", group = TagGroup.URGENCY, colorHex = "#FFA726", sortOrder = 1),
                Tag(name = "On Fire", nameRu = "Горит", group = TagGroup.URGENCY, colorHex = "#F44336", sortOrder = 2),

                // Sphere
                Tag(name = "Work", nameRu = "Работа", group = TagGroup.SPHERE, colorHex = "#5C6BC0", sortOrder = 0),
                Tag(name = "Personal", nameRu = "Личное", group = TagGroup.SPHERE, colorHex = "#AB47BC", sortOrder = 1),
                Tag(name = "Home", nameRu = "Дом", group = TagGroup.SPHERE, colorHex = "#8D6E63", sortOrder = 2),
                Tag(name = "Shopping", nameRu = "Покупки", group = TagGroup.SPHERE, colorHex = "#26A69A", sortOrder = 3),
                Tag(name = "Health", nameRu = "Здоровье", group = TagGroup.SPHERE, colorHex = "#EC407A", sortOrder = 4),
                Tag(name = "Finance", nameRu = "Финансы", group = TagGroup.SPHERE, colorHex = "#FFA000", sortOrder = 5),
                Tag(name = "Education", nameRu = "Обучение", group = TagGroup.SPHERE, colorHex = "#29B6F6", sortOrder = 6)
            )
            tagDao.insertTags(defaultTags)
        }
    }
}
