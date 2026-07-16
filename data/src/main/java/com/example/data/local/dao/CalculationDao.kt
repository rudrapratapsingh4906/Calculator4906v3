package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CalculationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculations ORDER BY timestamp DESC")
    fun getCalculationHistory(): Flow<List<CalculationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calculation: CalculationEntity)

    @Query("DELETE FROM calculations")
    suspend fun clearHistory()
}
