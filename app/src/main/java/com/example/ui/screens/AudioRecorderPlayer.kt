package com.example.ui.screens

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null

    fun start(outputFile: File) {
        try {
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            Log.d("AudioRecorder", "Gravação iniciada: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Erro ao iniciar gravação", e)
        }
    }

    fun stop() {
        try {
            recorder?.stop()
            Log.d("AudioRecorder", "Gravação parada")
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Erro ao parar gravação", e)
        } finally {
            recorder?.release()
            recorder = null
        }
    }
}

class AudioPlayer {
    private var player: MediaPlayer? = null
    private var currentPlayingPath: String? = null

    fun play(filePath: String, onFinished: () -> Unit) {
        if (isPlaying() && currentPlayingPath == filePath) {
            stop()
            return
        }
        stop()
        try {
            currentPlayingPath = filePath
            player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    onFinished()
                    stop()
                }
                start()
            }
            Log.d("AudioPlayer", "Reproduzindo áudio: $filePath")
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Erro ao reproduzir áudio", e)
        }
    }

    fun stop() {
        try {
            player?.stop()
            Log.d("AudioPlayer", "Reprodução parada")
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Erro ao parar reprodução", e)
        } finally {
            player?.release()
            player = null
            currentPlayingPath = null
        }
    }

    fun isPlaying(): Boolean {
        return try {
            player?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }

    fun getPlayingPath(): String? {
        return if (isPlaying()) currentPlayingPath else null
    }
}
