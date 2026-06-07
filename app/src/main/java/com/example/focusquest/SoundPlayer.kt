package com.example.focusquest

import android.content.Context
import android.media.MediaPlayer

class SoundPlayer {

    companion object {
        const val BROWN_NOISE  = "brown_noise"
        const val RAIN         = "rain"
        const val LOFI         = "lofi"
        const val WHITE_NOISE  = "white_noise"

        fun resIdFor(context: Context, name: String): Int? = when (name) {
            BROWN_NOISE -> R.raw.sound_brown_noise
            RAIN        -> R.raw.sound_rain
            LOFI        -> R.raw.sound_lofi
            WHITE_NOISE -> R.raw.sound_white_noise
            else        -> null
        }
    }

    private var player: MediaPlayer? = null
    private var currentSound: String? = null
    private var context: Context? = null

    fun init(ctx: Context) {
        context = ctx.applicationContext
    }

    fun play(soundName: String) {
        val ctx = context ?: return
        if (currentSound == soundName && player?.isPlaying == true) return
        stop()
        val resId = resIdFor(ctx, soundName) ?: return
        currentSound = soundName
        player = MediaPlayer.create(ctx, resId)?.apply {
            isLooping = true
            start()
        }
    }

    fun pause() {
        try { if (player?.isPlaying == true) player?.pause() } catch (_: Exception) {}
    }

    fun resume() {
        val name = currentSound ?: return
        if (player == null) {
            play(name)
        } else {
            try { player?.start() } catch (_: Exception) { play(name) }
        }
    }

    fun stop() {
        currentSound = null
        try { player?.stop() } catch (_: Exception) {}
        player?.release()
        player = null
    }
}
