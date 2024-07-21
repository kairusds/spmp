import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.settings.ui.ThemeValues

@Composable
internal actual fun PlatformTheme(theme: ThemeValues, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalScrollbarStyle provides LocalScrollbarStyle.current.copy(
            hoverColor = theme.accent.copy(alpha = 0.75f),
            unhoverColor = theme.on_background.copy(alpha = 0.2f),
            hoverDurationMillis = 100,
            thickness = 7.dp
        )
    ) {
        content()
    }
}
