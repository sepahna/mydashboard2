package ir.mbarani.mydashboard.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val color: Long = 0xFF263449,
    val pinned: Boolean = false,
    val locked: Boolean = false,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Int,
    val title: String,
    val details: String = "",
    val priority: Int = 1,
    val dueAt: Long? = null,
    val completed: Boolean = false,
    val pinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quick_links")
data class QuickLinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val icon: String = "link",
    val category: String = "عمومی",
    val pinned: Boolean = false
)

@Dao
interface DashboardDao {
    @Query("SELECT * FROM notes WHERE archived = 0 ORDER BY pinned DESC, updatedAt DESC")
    fun observeNotes(): Flow<List<NoteEntity>>

    @Insert
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    @Query("SELECT * FROM tasks WHERE listId = :listId ORDER BY completed ASC, pinned DESC, createdAt DESC")
    fun observeTasks(listId: Int): Flow<List<TaskEntity>>

    @Insert
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("SELECT * FROM quick_links ORDER BY pinned DESC, title COLLATE NOCASE")
    fun observeLinks(): Flow<List<QuickLinkEntity>>

    @Insert
    suspend fun insertLink(link: QuickLinkEntity)

    @Update
    suspend fun updateLink(link: QuickLinkEntity)

    @Query("DELETE FROM quick_links WHERE id = :id")
    suspend fun deleteLink(id: Long)
}

@Database(
    entities = [NoteEntity::class, TaskEntity::class, QuickLinkEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dashboardDao(): DashboardDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mydashboard.db"
                ).build().also { INSTANCE = it }
            }
    }
}
