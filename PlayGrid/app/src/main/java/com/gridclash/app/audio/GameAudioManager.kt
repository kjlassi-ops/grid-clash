package com.gridclash.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.ToneGenerator

class GameAudioManager(context: Context) {

    private val appContext = context.applicationContext
    private val soundPool = SoundPool.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .setMaxStreams(4)
        .build()

    private val tone = ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 75)

    var soundEnabled: Boolean = true
    var musicEnabled: Boolean = true

    fun onCellPlayed() = playTone(ToneGenerator.TONE_PROP_BEEP)
    fun onWin() = playTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD)
    fun onLose() = playTone(ToneGenerator.TONE_SUP_ERROR)
    fun onDraw() = playTone(ToneGenerator.TONE_PROP_ACK)
    fun onConnect() = playTone(ToneGenerator.TONE_PROP_PROMPT)

    fun onAppResume() {
        // point d'extension: relancer MediaPlayer royalty-free (Pixabay/OpenGameArt/Freesound)
    }

    fun onAppPause() {
        // point d'extension: pause MediaPlayer
    }

    fun release() {
        runCatching { soundPool.release() }
        runCatching { tone.release() }
    }

    private fun playTone(toneType: Int) {
        if (!soundEnabled) return
        tone.startTone(toneType, 120)
    }
}
