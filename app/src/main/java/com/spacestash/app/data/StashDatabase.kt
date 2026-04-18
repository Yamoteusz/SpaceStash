package com.spacestash.app.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface StashDao {
    @Query("SELECT * FROM stash_table ORDER BY id DESC")
    fun getAllItems(): Flow<List<StashEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: StashEntity): Long

    @Delete
    suspend fun deleteItem(item: StashEntity): Int

    @Update
    suspend fun updateItem(item: StashEntity): Int
}

@Database(entities = [StashEntity::class], version = 1, exportSchema = false)
abstract class StashDatabase : RoomDatabase() {
    abstract fun stashDao(): StashDao
    companion object {
        @Volatile private var INSTANCE: StashDatabase? = null
        fun getDatabase(context: Context): StashDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StashDatabase::class.java,
                    "spacestash_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}