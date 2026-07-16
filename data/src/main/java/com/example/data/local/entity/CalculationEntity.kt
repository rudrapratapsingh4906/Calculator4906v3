package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculations")
data class CalculationEntity(
    @PrimaryKey val id: String,
    val expression: String,
    val result: String,
    val timestamp: Long
)
