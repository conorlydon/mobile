package com.example.mobile.data.local
// AI Generated
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// Room database class — declares which entities (tables) exist and provides access to DAOs.
// @TypeConverters tells Room to use ChallengeTypeConverters for types it can't store natively (e.g. LocalDate).
@Database(
    entities = [ChallengeEntity::class], // one table: challenges
    version = 1,
    exportSchema = false
)
@TypeConverters(ChallengeTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun challengeDao(): ChallengeDao // provides access to all challenge queries

    companion object {
        // @Volatile ensures INSTANCE is always read from main memory, not a thread-local cache.
        // This is part of the double-checked locking singleton pattern for thread safety.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Returns the single shared instance of the database.
        // synchronized(this) ensures only one thread can create the instance at a time.
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mobile4.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
