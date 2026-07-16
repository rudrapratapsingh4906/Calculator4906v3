package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
  )

private val OceanLightColorScheme = lightColorScheme(
    primary = OceanPrimary,
    secondary = OceanSecondary,
    tertiary = OceanTertiary,
    background = OceanBackground,
    surface = OceanSurface
)

private val OceanDarkColorScheme = darkColorScheme(
    primary = OceanDarkPrimary,
    secondary = OceanDarkSecondary,
    tertiary = OceanDarkTertiary,
    background = OceanDarkBackground,
    surface = OceanDarkSurface
)

private val ForestLightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    secondary = ForestSecondary,
    tertiary = ForestTertiary,
    background = ForestBackground,
    surface = ForestSurface
)

private val ForestDarkColorScheme = darkColorScheme(
    primary = ForestDarkPrimary,
    secondary = ForestDarkSecondary,
    tertiary = ForestDarkTertiary,
    background = ForestDarkBackground,
    surface = ForestDarkSurface
)

@Composable
fun MyApplicationTheme(
  themeName: String = "Default",
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
      themeName.equals("Ocean", ignoreCase = true) -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
      themeName.equals("Forest", ignoreCase = true) -> if (darkTheme) ForestDarkColorScheme else ForestLightColorScheme
      else -> {
          if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
              val context = LocalContext.current
              if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
          } else {
              if (darkTheme) DarkColorScheme else LightColorScheme
          }
      }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
