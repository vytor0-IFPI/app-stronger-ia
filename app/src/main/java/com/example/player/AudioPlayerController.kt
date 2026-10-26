package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import com.example.data.model.Episode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object AudioPlayerController {

    private const val TAG = "AudioPlayerController"
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _currentEpisode = MutableStateFlow<Episode?>(null)
    val currentEpisode: StateFlow<Episode?> = _currentEpisode.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun playEpisode(context: Context, episode: Episode) {
        val mp3Url = episode.mp3
        if (mp3Url.isNullOrBlank()) {
            _errorMessage.value = "Este episódio ainda não possui áudio MP3 disponível."
            return
        }

        _currentEpisode.value = episode
        _isLoading.value = true
        _errorMessage.value = null

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            stopProgressTracking()

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(mp3Url)
                setOnPreparedListener { mp ->
                    _isLoading.value = false
                    _durationMs.value = mp.duration.toLong().coerceAtLeast(0L)
                    mp.start()
                    _isPlaying.value = true
                    applySpeed(_playbackSpeed.value)
                    startProgressTracking()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPositionMs.value = _durationMs.value
                    stopProgressTracking()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                    _isLoading.value = false
                    _isPlaying.value = false
                    _errorMessage.value = "Não foi possível carregar o áudio. Verifique sua conexão com a internet."
                    stopProgressTracking()
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Error playing episode", e)
            _isLoading.value = false
            _isPlaying.value = false
            _errorMessage.value = "Erro ao iniciar o áudio: ${e.localizedMessage ?: "Tente novamente mais tarde."}"
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        try {
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
                stopProgressTracking()
            } else {
                player.start()
                _isPlaying.value = true
                applySpeed(_playbackSpeed.value)
                startProgressTracking()
            }
        } catch (e: Exception) {
            Log.e(TAG, "togglePlayPause error", e)
        }
    }

    fun seekTo(positionMs: Long) {
        val player = mediaPlayer ?: return
        try {
            val target = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(1L))
            player.seekTo(target.toInt())
            _currentPositionMs.value = target
        } catch (e: Exception) {
            Log.e(TAG, "seekTo error", e)
        }
    }

    fun skipForward(seconds: Int = 30) {
        val newPos = (_currentPositionMs.value + (seconds * 1000L)).coerceAtMost(_durationMs.value)
        seekTo(newPos)
    }

    fun rewindBack(seconds: Int = 10) {
        val newPos = (_currentPositionMs.value - (seconds * 1000L)).coerceAtLeast(0L)
        seekTo(newPos)
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applySpeed(speed)
    }

    private fun applySpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.playbackParams = player.playbackParams.apply {
                            this.speed = speed
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to apply playback speed: $speed", e)
            }
        }
    }

    fun closePlayer() {
        try {
            stopProgressTracking()
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing player", e)
        }
        _currentEpisode.value = null
        _isPlaying.value = false
        _isLoading.value = false
        _currentPositionMs.value = 0L
        _durationMs.value = 0L
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            _currentPositionMs.value = player.currentPosition.toLong()
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun formatDuration(ms: Long): String {
        val totalSecs = (ms / 1000L).coerceAtLeast(0L)
        val minutes = totalSecs / 60
        val seconds = totalSecs % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
