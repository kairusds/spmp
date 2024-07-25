package com.toasterofbread.spmp.ui.layout.nowplaying.maintab

import LocalPlayerState
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import dev.toastbits.composekit.utils.common.getValue
import dev.toastbits.composekit.utils.common.thenIf
import dev.toastbits.composekit.utils.modifier.bounceOnClick
import com.toasterofbread.spmp.model.mediaitem.loader.SongLyricsLoader
import com.toasterofbread.spmp.model.mediaitem.song.Song
import com.toasterofbread.spmp.ui.component.HorizontalLyricsLineDisplay
import LocalAppState
import com.toasterofbread.spmp.ui.theme.appHover
import com.toasterofbread.spmp.ui.layout.contentbar.layoutslot.LandscapeLayoutSlot
import com.toasterofbread.spmp.ui.layout.contentbar.DisplayBar

@Composable
internal fun LargeBottomBar(
    background_colour: Color,
    modifier: Modifier = Modifier,
    inset_start: Dp = Dp.Unspecified,
    inset_end: Dp = Dp.Unspecified,
    inset_depth: Dp = 0.dp,
) {
    val state: SpMp.State = LocalAppState.current

    Row(
        modifier.alpha(0.5f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .thenIf(inset_depth > 0.dp) {
                    width(inset_end + inset_start)
                    .align(Alignment.Top)
                }
        ) {
            LandscapeLayoutSlot.PLAYER_BOTTOM_START.DisplayBar(
                0.dp,
                container_modifier = Modifier
                    .thenIf(
                        inset_depth > 0.dp,
                        elseAction = {
                            fillMaxWidth(0.5f)
                        }
                    ) {
                        offset(x = inset_start, y = inset_depth)
                        .width(inset_end - inset_start)
                    },
                getParentBackgroundColour = { background_colour }
            )
        }

        Row(
            Modifier.fillMaxWidth().weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton({}) {}

            LandscapeLayoutSlot.PLAYER_BOTTOM_END.DisplayBar(
                0.dp,
                container_modifier = Modifier.fillMaxWidth().weight(1f)
            )

            IconButton({ state.ui.player_expansion.toggle() }, Modifier.bounceOnClick().appHover(true)) {
                Icon(Icons.Default.KeyboardArrowDown, null)
            }
        }
    }
}
