package com.example.data.mapper

import com.example.data.local.entity.CalculationEntity
import com.example.domain.model.Calculation

fun CalculationEntity.toDomain(): Calculation {
    return Calculation(
        id = id,
        expression = expression,
        result = result,
        timestamp = timestamp
    )
}

fun Calculation.toEntity(): CalculationEntity {
    return CalculationEntity(
        id = id,
        expression = expression,
        result = result,
        timestamp = timestamp
    )
}
