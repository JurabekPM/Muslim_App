package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.SystemClock
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.TasbihFeedbackOption

private const val completionVibrationMillis = 165L

@Composable
internal fun rememberTasbexFeedback(feedback: Set<TasbihFeedbackOption>): TasbexFeedback {
    val view = LocalView.current
    val player = remember(view) { TasbexFeedback(view) }
    SideEffect {
        player.setAudioEnabled(
            TasbihFeedbackOption.TapSound in feedback || TasbihFeedbackOption.CompletionSound in feedback,
        )
    }
    DisposableEffect(player) { onDispose { player.close() } }
    return player
}

internal class TasbexFeedback(private val view: View) {
    private val context = view.context.applicationContext
    private val audioManager by lazy { context.getSystemService(AudioManager::class.java) }
    private var pool: SoundPool? = null
    private var tapSample = 0
    private var completionSample = 0
    private val loadedSamples = mutableSetOf<Int>()
    private var completionVibrationEnd = 0L
    private var closed = false

    fun setAudioEnabled(enabled: Boolean) {
        if (!enabled) {
            releaseAudio()
        } else if (pool == null && !closed) {
            val soundPool = SoundPool.Builder().setMaxStreams(2)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                .build()
            pool = soundPool
            soundPool.setOnLoadCompleteListener { source, sample, status ->
                if (source == pool && status == 0) loadedSamples += sample
            }
            tapSample = soundPool.load(context, R.raw.tasbex_tap, 1)
            completionSample = soundPool.load(context, R.raw.tasbex_complete, 1)
        }
    }

    fun play(feedback: Set<TasbihFeedbackOption>, completed: Boolean) {
        if (closed || !view.isAttachedToWindow) return
        if (completed && TasbihFeedbackOption.CompletionSound in feedback) {
            playSound(completionSample, volume = 0.66f, priority = 2)
        } else if (TasbihFeedbackOption.TapSound in feedback) {
            playSound(tapSample, volume = 0.44f, priority = 1)
        }
        if (completed && TasbihFeedbackOption.CompletionVibration in feedback) {
            vibrateOnCompletion()
        } else if (TasbihFeedbackOption.TapVibration in feedback && SystemClock.uptimeMillis() >= completionVibrationEnd) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    fun close() {
        closed = true
        releaseAudio()
    }

    private fun releaseAudio() {
        pool?.release()
        pool = null
        loadedSamples.clear()
    }

    private fun playSound(sample: Int, volume: Float, priority: Int) {
        if (sample in loadedSamples && audioManager?.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            pool?.play(sample, volume, volume, priority, 0, 1f)
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrateOnCompletion() {
        if (!view.isHapticFeedbackEnabled ||
            Settings.System.getInt(context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 0
        ) return

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            context.getSystemService(Vibrator::class.java)
        } ?: return
        if (!vibrator.hasVibrator()) return
        completionVibrationEnd = SystemClock.uptimeMillis() + completionVibrationMillis

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(
                completionVibrationMillis,
                if (vibrator.hasAmplitudeControl()) 200 else VibrationEffect.DEFAULT_AMPLITUDE,
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                vibrator.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH))
            } else {
                vibrator.vibrate(effect)
            }
        } else {
            vibrator.vibrate(completionVibrationMillis)
        }
    }
}
