package com.example.domain.usecase

import com.example.domain.model.ConversionCategory
import com.example.domain.model.ConversionUnit

class ConvertUnitUseCase {
    operator fun invoke(value: Double, fromUnit: ConversionUnit, toUnit: ConversionUnit): Double {
        if (fromUnit.category != toUnit.category) throw IllegalArgumentException("Categories must match")

        if (fromUnit.category == ConversionCategory.TEMPERATURE) {
            val celsius = when (fromUnit.id) {
                "C" -> value
                "F" -> (value - 32) * 5 / 9
                "K" -> value - 273.15
                else -> value
            }
            return when (toUnit.id) {
                "C" -> celsius
                "F" -> (celsius * 9 / 5) + 32
                "K" -> celsius + 273.15
                else -> celsius
            }
        }

        if (fromUnit.category == ConversionCategory.FUEL_CONSUMPTION) {
            val l100km = when (fromUnit.id) {
                "L100KM" -> value
                "KM_L" -> if (value == 0.0) 0.0 else 100.0 / value
                "MPG_US" -> if (value == 0.0) 0.0 else 235.214583 / value
                "MPG_UK" -> if (value == 0.0) 0.0 else 282.4809363 / value
                else -> value
            }
            return when (toUnit.id) {
                "L100KM" -> l100km
                "KM_L" -> if (l100km == 0.0) 0.0 else 100.0 / l100km
                "MPG_US" -> if (l100km == 0.0) 0.0 else 235.214583 / l100km
                "MPG_UK" -> if (l100km == 0.0) 0.0 else 282.4809363 / l100km
                else -> l100km
            }
        }

        val baseValue = value * fromUnit.factorToBase
        return baseValue / toUnit.factorToBase
    }
}
