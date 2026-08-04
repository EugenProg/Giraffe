package com.kogen.giraffe.ui.common.presentation

import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kz.evko.kogen_di.annotations.KoGenComponent
import kotlin.time.Duration.Companion.milliseconds

data class AudioPlaybackState(
    val filePath: String? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
)

@KoGenComponent(true)
class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    fun play(filePath: String) {
        if (_state.value.filePath == filePath && mediaPlayer != null) {
            resume()
            return
        }

        release()

        val player = MediaPlayer().apply {
            setDataSource(filePath)
            setOnCompletionListener {
                stopProgressLoop()
                _state.value = _state.value.copy(isPlaying = false, currentPositionMs = 0)
            }
            prepare()
            start()
        }
        mediaPlayer = player

        _state.value = AudioPlaybackState(
            filePath = filePath,
            isPlaying = true,
            currentPositionMs = 0,
            durationMs = player.duration,
        )
        startProgressLoop()
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
        }
        stopProgressLoop()
        _state.value = _state.value.copy(isPlaying = false)
    }

    fun resume() {
        mediaPlayer?.let {
            it.start()
            _state.value = _state.value.copy(isPlaying = true)
            startProgressLoop()
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        _state.value = _state.value.copy(currentPositionMs = positionMs)
    }

    fun release() {
        stopProgressLoop()
        mediaPlayer?.release()
        mediaPlayer = null
        _state.value = AudioPlaybackState()
    }

    private fun startProgressLoop() {
        stopProgressLoop()
        progressJob = scope.launch {
            while (true) {
                val pos = mediaPlayer?.currentPosition ?: break
                _state.value = _state.value.copy(currentPositionMs = pos)
                delay(100.milliseconds)
            }
        }
    }

    private fun stopProgressLoop() {
        progressJob?.cancel()
        progressJob = null
    }
}