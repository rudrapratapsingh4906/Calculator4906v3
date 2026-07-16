package com.example.domain.usecase

import com.example.domain.model.ConversionCategory
import com.example.domain.model.ConversionUnit

class GetConversionUnitsUseCase {
    operator fun invoke(): List<ConversionUnit> {
        return listOf(
            // Length (Base: Meter)
            ConversionUnit("M", "Meter", "m", ConversionCategory.LENGTH, 1.0),
            ConversionUnit("KM", "Kilometer", "km", ConversionCategory.LENGTH, 1000.0),
            ConversionUnit("CM", "Centimeter", "cm", ConversionCategory.LENGTH, 0.01),
            ConversionUnit("MM", "Millimeter", "mm", ConversionCategory.LENGTH, 0.001),
            ConversionUnit("MI", "Mile", "mi", ConversionCategory.LENGTH, 1609.344),
            ConversionUnit("YD", "Yard", "yd", ConversionCategory.LENGTH, 0.9144),
            ConversionUnit("FT", "Foot", "ft", ConversionCategory.LENGTH, 0.3048),
            ConversionUnit("IN", "Inch", "in", ConversionCategory.LENGTH, 0.0254),

            // Weight (Base: Kilogram)
            ConversionUnit("KG", "Kilogram", "kg", ConversionCategory.WEIGHT, 1.0),
            ConversionUnit("G", "Gram", "g", ConversionCategory.WEIGHT, 0.001),
            ConversionUnit("MG", "Milligram", "mg", ConversionCategory.WEIGHT, 0.000001),
            ConversionUnit("LB", "Pound", "lb", ConversionCategory.WEIGHT, 0.45359237),
            ConversionUnit("OZ", "Ounce", "oz", ConversionCategory.WEIGHT, 0.02834952),

            // Temperature (Base: Celsius, handled specially)
            ConversionUnit("C", "Celsius", "°C", ConversionCategory.TEMPERATURE, 1.0),
            ConversionUnit("F", "Fahrenheit", "°F", ConversionCategory.TEMPERATURE, 1.0),
            ConversionUnit("K", "Kelvin", "K", ConversionCategory.TEMPERATURE, 1.0),

            // Area (Base: Square Meter)
            ConversionUnit("SQ_M", "Square Meter", "m²", ConversionCategory.AREA, 1.0),
            ConversionUnit("SQ_KM", "Square Kilometer", "km²", ConversionCategory.AREA, 1000000.0),
            ConversionUnit("SQ_CM", "Square Centimeter", "cm²", ConversionCategory.AREA, 0.0001),
            ConversionUnit("HA", "Hectare", "ha", ConversionCategory.AREA, 10000.0),
            ConversionUnit("ACRE", "Acre", "ac", ConversionCategory.AREA, 4046.85642),
            ConversionUnit("SQ_MI", "Square Mile", "mi²", ConversionCategory.AREA, 2589988.11),
            ConversionUnit("SQ_YD", "Square Yard", "yd²", ConversionCategory.AREA, 0.83612736),
            ConversionUnit("SQ_FT", "Square Foot", "ft²", ConversionCategory.AREA, 0.09290304),
            ConversionUnit("SQ_IN", "Square Inch", "in²", ConversionCategory.AREA, 0.00064516),

            // Volume (Base: Liter)
            ConversionUnit("L", "Liter", "L", ConversionCategory.VOLUME, 1.0),
            ConversionUnit("ML", "Milliliter", "mL", ConversionCategory.VOLUME, 0.001),
            ConversionUnit("M3", "Cubic Meter", "m³", ConversionCategory.VOLUME, 1000.0),
            ConversionUnit("GAL_US", "US Gallon", "gal", ConversionCategory.VOLUME, 3.78541178),
            ConversionUnit("QT_US", "US Quart", "qt", ConversionCategory.VOLUME, 0.94635295),
            ConversionUnit("PT_US", "US Pint", "pt", ConversionCategory.VOLUME, 0.47317647),
            ConversionUnit("CUP_US", "US Cup", "cup", ConversionCategory.VOLUME, 0.24),
            ConversionUnit("FL_OZ_US", "US Fluid Ounce", "fl oz", ConversionCategory.VOLUME, 0.02957353),

            // Time (Base: Second)
            ConversionUnit("S", "Second", "s", ConversionCategory.TIME, 1.0),
            ConversionUnit("MS", "Millisecond", "ms", ConversionCategory.TIME, 0.001),
            ConversionUnit("MIN", "Minute", "min", ConversionCategory.TIME, 60.0),
            ConversionUnit("H", "Hour", "h", ConversionCategory.TIME, 3600.0),
            ConversionUnit("D", "Day", "d", ConversionCategory.TIME, 86400.0),
            ConversionUnit("WK", "Week", "wk", ConversionCategory.TIME, 604800.0),
            ConversionUnit("MO", "Month (30 days)", "mo", ConversionCategory.TIME, 2592000.0),
            ConversionUnit("YR", "Year (365 days)", "yr", ConversionCategory.TIME, 31536000.0),

            // Speed (Base: Meter per Second)
            ConversionUnit("M_S", "Meter per Second", "m/s", ConversionCategory.SPEED, 1.0),
            ConversionUnit("KM_H", "Kilometer per Hour", "km/h", ConversionCategory.SPEED, 0.27777778),
            ConversionUnit("MI_H", "Mile per Hour", "mph", ConversionCategory.SPEED, 0.44704),
            ConversionUnit("FT_S", "Foot per Second", "ft/s", ConversionCategory.SPEED, 0.3048),
            ConversionUnit("KN", "Knot", "kn", ConversionCategory.SPEED, 0.51444444),

            // Pressure (Base: Pascal)
            ConversionUnit("PA", "Pascal", "Pa", ConversionCategory.PRESSURE, 1.0),
            ConversionUnit("HPA", "Hectopascal", "hPa", ConversionCategory.PRESSURE, 100.0),
            ConversionUnit("KPA", "Kilopascal", "kPa", ConversionCategory.PRESSURE, 1000.0),
            ConversionUnit("BAR", "Bar", "bar", ConversionCategory.PRESSURE, 100000.0),
            ConversionUnit("PSI", "Pound per Square Inch", "psi", ConversionCategory.PRESSURE, 6894.75729),
            ConversionUnit("ATM", "Atmosphere", "atm", ConversionCategory.PRESSURE, 101325.0),

            // Energy (Base: Joule)
            ConversionUnit("J", "Joule", "J", ConversionCategory.ENERGY, 1.0),
            ConversionUnit("KJ", "Kilojoule", "kJ", ConversionCategory.ENERGY, 1000.0),
            ConversionUnit("CAL", "Gram calorie", "cal", ConversionCategory.ENERGY, 4.184),
            ConversionUnit("KCAL", "Kilocalorie", "kcal", ConversionCategory.ENERGY, 4184.0),
            ConversionUnit("WH", "Watt hour", "Wh", ConversionCategory.ENERGY, 3600.0),
            ConversionUnit("KWH", "Kilowatt hour", "kWh", ConversionCategory.ENERGY, 3600000.0),
            ConversionUnit("EV", "Electronvolt", "eV", ConversionCategory.ENERGY, 1.602176634e-19),

            // Power (Base: Watt)
            ConversionUnit("W", "Watt", "W", ConversionCategory.POWER, 1.0),
            ConversionUnit("KW", "Kilowatt", "kW", ConversionCategory.POWER, 1000.0),
            ConversionUnit("HP", "Horsepower", "hp", ConversionCategory.POWER, 745.699872),

            // Angle (Base: Degree)
            ConversionUnit("DEG", "Degree", "°", ConversionCategory.ANGLE, 1.0),
            ConversionUnit("RAD", "Radian", "rad", ConversionCategory.ANGLE, 57.2957795),
            ConversionUnit("GRAD", "Gradian", "grad", ConversionCategory.ANGLE, 0.9),

            // Data Storage (Base: Byte)
            ConversionUnit("B", "Byte", "B", ConversionCategory.DATA_STORAGE, 1.0),
            ConversionUnit("KB", "Kilobyte", "KB", ConversionCategory.DATA_STORAGE, 1024.0),
            ConversionUnit("MB", "Megabyte", "MB", ConversionCategory.DATA_STORAGE, 1048576.0),
            ConversionUnit("GB", "Gigabyte", "GB", ConversionCategory.DATA_STORAGE, 1073741824.0),
            ConversionUnit("TB", "Terabyte", "TB", ConversionCategory.DATA_STORAGE, 1099511627776.0),
            ConversionUnit("BIT", "Bit", "bit", ConversionCategory.DATA_STORAGE, 0.125),

            // Frequency (Base: Hertz)
            ConversionUnit("HZ", "Hertz", "Hz", ConversionCategory.FREQUENCY, 1.0),
            ConversionUnit("KHZ", "Kilohertz", "kHz", ConversionCategory.FREQUENCY, 1000.0),
            ConversionUnit("MHZ", "Megahertz", "MHz", ConversionCategory.FREQUENCY, 1000000.0),
            ConversionUnit("GHZ", "Gigahertz", "GHz", ConversionCategory.FREQUENCY, 1000000000.0),

            // Fuel Consumption (Reference Base: Liters per 100 Kilometers, handled specially)
            ConversionUnit("L100KM", "Liters per 100 km", "L/100km", ConversionCategory.FUEL_CONSUMPTION, 1.0),
            ConversionUnit("KM_L", "Kilometers per Liter", "km/L", ConversionCategory.FUEL_CONSUMPTION, 1.0),
            ConversionUnit("MPG_US", "Miles per Gallon (US)", "mpg (US)", ConversionCategory.FUEL_CONSUMPTION, 1.0),
            ConversionUnit("MPG_UK", "Miles per Gallon (UK)", "mpg (UK)", ConversionCategory.FUEL_CONSUMPTION, 1.0)
        )
    }
}
