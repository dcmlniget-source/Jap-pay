package com.example.util

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

object SoundPlayer {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var tts: TextToSpeech? = null
    private var ttsInitialized = false

    private fun initTts(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                    tts?.setPitch(1.35f)
                    tts?.setSpeechRate(1.1f)
                    ttsInitialized = true
                }
            }
        }
    }

    private fun tryPlayFromAssets(context: Context, filename: String): Boolean {
        return try {
            val afd: AssetFileDescriptor = context.assets.openFd(filename)
            val player = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepare()
                setOnCompletionListener { it.release() }
                start()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Played when money is received / deposit approved ("Wow! Congratulations!")
     */
    fun playWowSound(context: Context) {
        vibrate(context, longArrayOf(0, 120, 80, 250))
        initTts(context)

        scope.launch {
            // First check if asset or raw file exists
            if (tryPlayFromAssets(context, "myinstants_1.mp3") || tryPlayFromAssets(context, "wow.mp3")) {
                return@launch
            }

            // Play voice synthesis "Wow! Congratulations!"
            try {
                if (ttsInitialized) {
                    tts?.speak("Wow! Congratulations!", TextToSpeech.QUEUE_FLUSH, null, "wow_tag")
                }
            } catch (_: Exception) {}

            // Play celebratory synthesized fanfare chimes
            try {
                val sampleRate = 44100
                val durationSec = 1.0
                val numSamples = (durationSec * sampleRate).toInt()
                val samples = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = if (t < 0.25) 523.25 else if (t < 0.5) 659.25 else if (t < 0.75) 783.99 else 1046.50
                    val envelope = (1.0 - (t / durationSec)) * 0.9
                    val wave = (sin(2.0 * Math.PI * freq * t) * 0.8 + sin(2.0 * Math.PI * (freq * 2.0) * t) * 0.2) * envelope
                    samples[i] = (wave * 26000).toInt().coerceIn(-32768, 32767).toShort()
                }
                playBuffer(samples, sampleRate)
            } catch (_: Exception) {}
        }
    }

    /**
     * Played when money is transferred (Funny meme crying sound "myinstants.mp3")
     */
    fun playCryingSound(context: Context) {
        vibrate(context, longArrayOf(0, 150, 80, 150, 80, 300))
        scope.launch {
            // First check if asset or raw file exists
            if (tryPlayFromAssets(context, "myinstants.mp3") || tryPlayFromAssets(context, "crying.mp3")) {
                return@launch
            }

            try {
                val sampleRate = 44100
                val durationSec = 1.8
                val numSamples = (durationSec * sampleRate).toInt()
                val samples = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    // Expressive meme crying wail modulation
                    val subCycle = (t * 3.2) % 1.0
                    val wobble = sin(2.0 * Math.PI * 14.0 * t) * 50.0
                    val baseFreq = 580.0 - (subCycle * 220.0) + wobble
                    val envelope = (1.0 - (subCycle * 0.65)) * sin(Math.PI * (i.toDouble() / numSamples))
                    val wave = (sin(2.0 * Math.PI * baseFreq * t) * 0.85 + sin(2.0 * Math.PI * (baseFreq * 0.5) * t) * 0.15) * envelope
                    samples[i] = (wave * 28000).toInt().coerceIn(-32768, 32767).toShort()
                }
                playBuffer(samples, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playCoinChime(context: Context) {
        vibrate(context, longArrayOf(0, 50))
        scope.launch {
            try {
                val sampleRate = 44100
                val durationSec = 0.4
                val numSamples = (durationSec * sampleRate).toInt()
                val samples = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = if (t < 0.15) 1200.0 else 1800.0
                    val envelope = Math.exp(-t * 8.0)
                    val wave = sin(2.0 * Math.PI * freq * t) * envelope
                    samples[i] = (wave * 24000).toInt().coerceIn(-32768, 32767).toShort()
                }
                playBuffer(samples, sampleRate)
            } catch (_: Exception) {}
        }
    }

    private fun playBuffer(samples: ShortArray, sampleRate: Int) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(samples.size * 2, minBufferSize)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(samples, 0, samples.size)
        track.play()
        Thread.sleep((samples.size * 1000L / sampleRate) + 100)
        track.release()
    }

    private fun vibrate(context: Context, pattern: LongArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                v?.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }
}

