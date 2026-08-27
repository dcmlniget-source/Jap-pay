package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundPlayer {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playCryingSound(context: Context) {
        vibrate(context, longArrayOf(0, 150, 80, 150, 80, 300))
        scope.launch {
            try {
                val sampleRate = 44100
                val durationSec = 1.3
                val numSamples = (durationSec * sampleRate).toInt()
                val samples = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    // Wailing sobbing crying effect with vibrato
                    val cycle = (t * 2.5) % 1.0
                    val freq = 620.0 - (cycle * 240.0) + (sin(2.0 * Math.PI * 16.0 * t) * 40.0)
                    val envelope = (1.0 - (cycle * 0.7)) * sin(Math.PI * (i.toDouble() / numSamples))
                    val wave = sin(2.0 * Math.PI * freq * t) * envelope
                    samples[i] = (wave * 26000).toInt().coerceIn(-32768, 32767).toShort()
                }
                playBuffer(samples, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playWowSound(context: Context) {
        vibrate(context, longArrayOf(0, 100, 50, 200))
        scope.launch {
            try {
                val sampleRate = 44100
                val durationSec = 0.9
                val numSamples = (durationSec * sampleRate).toInt()
                val samples = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 380.0 + (t * 520.0)
                    val envelope = (1.0 - (t / durationSec)) * 0.9
                    val wave = (sin(2.0 * Math.PI * freq * t) * 0.7 + sin(2.0 * Math.PI * (freq * 1.5) * t) * 0.3) * envelope
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
        Thread.sleep(1000)
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
