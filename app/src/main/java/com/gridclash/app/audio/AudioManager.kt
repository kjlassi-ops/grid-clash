package com.gridclash.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import android.util.Log

/**
 * Gestion du son et des vibrations.
 * SoundPool pour les SFX courts (latence faible), MediaPlayer pour la musique de fond.
 *
 * Les fichiers audio sont optionnels : si absents du dossier res/raw/, le jeu
 * continue silencieusement (graceful fallback).
 */
class AudioManager(private val context: Context) {

    // ─── SFX ──────────────────────────────────────────────────────────────────

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var soundClick:    Int = 0
    private var soundWin:      Int = 0
    private var soundLose:     Int = 0
    private var soundDraw:     Int = 0
    private var soundConnect:  Int = 0

    // ─── Musique ──────────────────────────────────────────────────────────────

    private var mediaPlayer: MediaPlayer? = null
    private var musicResId: Int           = 0

    // ─── États ────────────────────────────────────────────────────────────────

    var soundEnabled:     Boolean = true
    var musicEnabled:     Boolean = true
    var vibrationEnabled: Boolean = true

    // ─── Init ─────────────────────────────────────────────────────────────────

    init {
        loadSounds()
    }

    private fun loadSounds() {
        soundClick   = loadRaw("sfx_click")
        soundWin     = loadRaw("sfx_win")
        soundLose    = loadRaw("sfx_lose")
        soundDraw    = loadRaw("sfx_draw")
        soundConnect = loadRaw("sfx_connect")
        musicResId   = getRawId("music_bg")
    }

    private fun loadRaw(name: String): Int {
        val id = getRawId(name)
        return if (id != 0) soundPool.load(context, id, 1) else 0
    }

    private fun getRawId(name: String): Int =
        context.resources.getIdentifier(name, "raw", context.packageName)

    // ─── API publique ─────────────────────────────────────────────────────────

    fun playClick()   = playSound(soundClick)
    fun playWin()     = playSound(soundWin)
    fun playLose()    = playSound(soundLose)
    fun playDraw()    = playSound(soundDraw)
    fun playConnect() = playSound(soundConnect)

    private fun playSound(soundId: Int) {
        if (!soundEnabled || soundId == 0) return
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    fun vibrate(durationMs: Long = 30L) {
        if (!vibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v?.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            Log.w("AudioManager", "Vibration failed: ${e.message}")
        }
    }

    // ─── Musique de fond ──────────────────────────────────────────────────────

    fun startMusic() {
        if (!musicEnabled || musicResId == 0) return
        if (mediaPlayer != null) return // déjà en cours
        try {
            mediaPlayer = MediaPlayer.create(context, musicResId)?.apply {
                isLooping = true
                setVolume(0.35f, 0.35f)
                start()
            }
        } catch (e: Exception) {
            Log.w("AudioManager", "Music start failed: ${e.message}")
        }
    }

    fun stopMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    fun pauseMusic() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
    }

    fun resumeMusic() {
        if (!musicEnabled) return
        mediaPlayer?.takeIf { !it.isPlaying }?.start()
    }

    fun applySettings(sound: Boolean, music: Boolean, vibration: Boolean) {
        soundEnabled     = sound
        vibrationEnabled = vibration
        val wasPlaying   = musicEnabled
        musicEnabled     = music
        when {
            music && !wasPlaying -> startMusic()
            !music && wasPlaying -> stopMusic()
        }
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    fun release() {
        stopMusic()
        soundPool.release()
    }
}
