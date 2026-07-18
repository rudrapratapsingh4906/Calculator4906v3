package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CalculationDao
import com.example.data.local.dao.VoiceCommandDao
import com.example.data.local.dao.VoiceHistoryDao
import com.example.data.local.entity.CalculationEntity
import com.example.data.local.entity.VoiceCommandEntity
import com.example.data.local.entity.VoiceHistoryEntity

@Database(
    entities = [CalculationEntity::class, VoiceCommandEntity::class, VoiceHistoryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val calculationDao: CalculationDao
    abstract val voiceCommandDao: VoiceCommandDao
    abstract val voiceHistoryDao: VoiceHistoryDao

    companion object {
        const val DATABASE_NAME = "calculator_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
        }
    }
}
