package com.devpush.twinmind.presentation.memories

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class AudioPlayerHelper(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null

    fun play(filePath: String) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
        exoPlayer?.apply {
            val mediaItem = MediaItem.fromUri("file://$filePath")
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun stop() {
        exoPlayer?.stop()
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }

    // Optional: Add listener for playback state changes if needed later
    // fun setPlaybackStateListener(listener: Player.Listener) {
    // exoPlayer?.addListener(listener)
    // }
}
