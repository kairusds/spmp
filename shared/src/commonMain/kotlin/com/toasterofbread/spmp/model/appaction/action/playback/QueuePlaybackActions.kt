package com.toasterofbread.spmp.model.appaction.action.playback

import kotlinx.serialization.Serializable
import com.toasterofbread.spmp.model.state.OldPlayerStateImpl

@Serializable
class ShuffleQueuePlaybackAppAction: PlaybackAction {
    override fun getType(): PlaybackAction.Type =
        PlaybackAction.Type.SHUFFLE_QUEUE

    override suspend fun execute(player: OldPlayerStateImpl) {
        player.withPlayer{
            undoableAction {
                shuffleQueue(start = current_song_index + 1)
            }
        }
    }
}

@Serializable
class ClearQueuePlaybackAppAction: PlaybackAction {
    override fun getType(): PlaybackAction.Type =
        PlaybackAction.Type.CLEAR_QUEUE

    override suspend fun execute(player: OldPlayerStateImpl) {
        player.withPlayer {
            undoableAction {
                clearQueue(keep_current = player.status.m_song_count > 1)
            }
        }
    }
}
