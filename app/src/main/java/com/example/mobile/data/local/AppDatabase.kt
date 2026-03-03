package com.example.mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ChallengeEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ChallengeTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun challengeDao(): ChallengeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

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
