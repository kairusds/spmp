package com.toasterofbread.spmp.platform

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.toasterofbread.spmp.db.Database
import com.toasterofbread.spmp.model.settings.Settings
import com.toasterofbread.spmp.model.settings.category.AccentColourSource
import com.toasterofbread.spmp.platform.download.PlayerDownloadManager
import com.toasterofbread.spmp.service.playercontroller.PlayerState
import dev.toastbits.composekit.commonsettings.impl.group.theme.ContextThemeManager
import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.composekit.settings.PlatformSettings
import dev.toastbits.composekit.settings.PlatformSettingsListener
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.util.platform.Platform
import dev.toastbits.ytmkt.model.YtmApi
import kotlinx.coroutines.launch

expect class AppContext: PlatformContext {
    val database: Database
    val download_manager: PlayerDownloadManager
    val ytapi: YtmApi
    val theme: AppThemeManager
    val settings: Settings

    fun getPrefs(): PlatformSettings
}

class AppThemeManager(
    private val context: AppContext
): ContextThemeManager(context.settings, context) {
    private var accent_colour_source: AccentColourSource? by mutableStateOf(null)

    override fun selectAccentColour(values: ThemeValues, contextualColour: Color?): Color =
        when(accent_colour_source ?: AccentColourSource.THEME) {
            AccentColourSource.THEME -> values.accent
            AccentColourSource.THUMBNAIL -> contextualColour ?: values.accent
        }

    fun onCurrentThumbnailColourChanged(thumbnail_colour: Color?) {
        onContextualColourChanged(thumbnail_colour)
    }

    private val prefs_listener: PlatformSettingsListener =
        PlatformSettingsListener { key ->
            when (key) {
                context.settings.theme.ACCENT_COLOUR_SOURCE.key -> {
                    context.coroutineScope.launch {
                        accent_colour_source = context.settings.theme.ACCENT_COLOUR_SOURCE.get()
                    }
                }
            }
        }

    init {
        val prefs: PlatformSettings = context.getPrefs()
        prefs.addListener(prefs_listener)

        context.coroutineScope.launch {
            accent_colour_source = context.settings.theme.ACCENT_COLOUR_SOURCE.get()
        }
    }
}

@Composable
fun PlayerState.getDefaultHorizontalPadding(): Dp =
    if (Platform.DESKTOP.isCurrent() && FormFactor.observe().value == FormFactor.LANDSCAPE) 30.dp else 10.dp
@Composable
fun PlayerState.getDefaultVerticalPadding(): Dp =
    if (Platform.DESKTOP.isCurrent() && FormFactor.observe().value == FormFactor.LANDSCAPE) 30.dp else 10.dp

@Composable
fun PlayerState.getDefaultPaddingValues(): PaddingValues = PaddingValues(horizontal = getDefaultHorizontalPadding(), vertical = getDefaultVerticalPadding())

suspend fun AppContext.getUiLanguage(): String =
    settings.system.LANG_UI.get().ifEmpty { getDefaultLanguage() }

@Composable
fun AppContext.observeUiLanguage(): State<String> {
    val lang_ui: String by settings.system.LANG_UI.observe()
    return remember { derivedStateOf {
        lang_ui.ifEmpty { getDefaultLanguage() }
    } }
}

suspend fun AppContext.getDataLanguage(): String =
    settings.system.LANG_DATA.get().ifEmpty { getDefaultLanguage() }
        .let { if (it == "en-GB") "en-US" else it }

@Composable
fun AppContext.observeDataLanguage(): State<String> {
    val lang_data: String by settings.system.LANG_DATA.observe()
    return remember { derivedStateOf {
        lang_data.ifEmpty { getDefaultLanguage() }
    } }
}

fun AppContext.getDefaultLanguage(): String =
    Locale.current.run {
        "$language-$region"
    }

fun <T> Result<T>.getOrNotify(context: AppContext, error_key: String): T? =
    fold(
        { return@fold it },
        {
            context.sendNotification(it)
            return@fold null
        }
    )
