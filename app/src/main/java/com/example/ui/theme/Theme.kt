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

private val BlackColorScheme = darkColorScheme(
    primary = BlackPrimary,
    secondary = BlackSecondary,
    tertiary = BlackTertiary,
    background = BlackBackground,
    surface = BlackSurface
)

private val BlueLightColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    tertiary = BlueTertiary,
    background = BlueBackground,
    surface = BlueSurface
)

private val BlueDarkColorScheme = darkColorScheme(
    primary = BlueDarkPrimary,
    secondary = BlueDarkSecondary,
    tertiary = BlueDarkTertiary,
    background = BlueDarkBackground,
    surface = BlueDarkSurface
)

private val GreenLightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    tertiary = GreenTertiary,
    background = GreenBackground,
    surface = GreenSurface
)

private val GreenDarkColorScheme = darkColorScheme(
    primary = GreenDarkPrimary,
    secondary = GreenDarkSecondary,
    tertiary = GreenDarkTertiary,
    background = GreenDarkBackground,
    surface = GreenDarkSurface
)

private val PurpleThemeLightColorScheme = lightColorScheme(
    primary = PurpleThemePrimary,
    secondary = PurpleThemeSecondary,
    tertiary = PurpleThemeTertiary,
    background = PurpleThemeBackground,
    surface = PurpleThemeSurface
)

private val PurpleThemeDarkColorScheme = darkColorScheme(
    primary = PurpleThemeDarkPrimary,
    secondary = PurpleThemeDarkSecondary,
    tertiary = PurpleThemeDarkTertiary,
    background = PurpleThemeDarkBackground,
    surface = PurpleThemeDarkSurface
)

private val OrangeLightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    secondary = OrangeSecondary,
    tertiary = OrangeTertiary,
    background = OrangeBackground,
    surface = OrangeSurface
)

private val OrangeDarkColorScheme = darkColorScheme(
    primary = OrangeDarkPrimary,
    secondary = OrangeDarkSecondary,
    tertiary = OrangeDarkTertiary,
    background = OrangeDarkBackground,
    surface = OrangeDarkSurface
)

private val RedLightColorScheme = lightColorScheme(
    primary = RedPrimary,
    secondary = RedSecondary,
    tertiary = RedTertiary,
    background = RedBackground,
    surface = RedSurface
)

private val RedDarkColorScheme = darkColorScheme(
    primary = RedDarkPrimary,
    secondary = RedDarkSecondary,
    tertiary = RedDarkTertiary,
    background = RedDarkBackground,
    surface = RedDarkSurface
)

private val TealLightColorScheme = lightColorScheme(
    primary = TealPrimary,
    secondary = TealSecondary,
    tertiary = TealTertiary,
    background = TealBackground,
    surface = TealSurface
)

private val TealDarkColorScheme = darkColorScheme(
    primary = TealDarkPrimary,
    secondary = TealDarkSecondary,
    tertiary = TealDarkTertiary,
    background = TealDarkBackground,
    surface = TealDarkSurface
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
      themeName.equals("AMOLED Black", ignoreCase = true) -> BlackColorScheme
      themeName.equals("Blue", ignoreCase = true) -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
      themeName.equals("Green", ignoreCase = true) -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
      themeName.equals("Purple", ignoreCase = true) -> if (darkTheme) PurpleThemeDarkColorScheme else PurpleThemeLightColorScheme
      themeName.equals("Orange", ignoreCase = true) -> if (darkTheme) OrangeDarkColorScheme else OrangeLightColorScheme
      themeName.equals("Red", ignoreCase = true) -> if (darkTheme) RedDarkColorScheme else RedLightColorScheme
      themeName.equals("Teal", ignoreCase = true) -> if (darkTheme) TealDarkColorScheme else TealLightColorScheme
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
