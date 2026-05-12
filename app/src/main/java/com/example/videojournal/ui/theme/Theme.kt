package com.example.videojournal.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = JournalBlue,
    secondary = JournalGreen,
    tertiary = JournalAmber,
    background = JournalDark,
    surface = ColorTokens.DarkSurface,
)

private val LightColorScheme = lightColorScheme(
    primary = JournalBlue,
    secondary = JournalGreen,
    tertiary = JournalAmber,
    background = JournalSurface,
    surface = ColorTokens.LightSurface,
)

private object ColorTokens {
    val LightSurface = androidx.compose.ui.graphics.Color.White
    val DarkSurface = androidx.compose.ui.graphics.Color(0xFF171D23)
}

@Composable
fun VideoJournalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
