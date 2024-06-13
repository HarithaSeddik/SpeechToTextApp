package com.example.audio_transcription_app

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.StreamHandler

class MainActivity : FlutterActivity() {
    private lateinit var eventChannel: EventChannel
    private lateinit var audioRecord: AudioRecord
    private var isRecording = false
    private val audioSource = MediaRecorder.AudioSource.MIC
    private val sampleRate = 44100 // Example sample rate
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private var minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the EventChannel
        eventChannel = EventChannel(flutterEngine!!.dartExecutor.binaryMessenger, "audio_stream")
        eventChannel.setStreamHandler(
                object : StreamHandler {
                    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                        startRecording(events)
                    }

                    override fun onCancel(arguments: Any?) {
                        stopRecording()
                    }
                }
        )
    }

    private fun startRecording(events: EventChannel.EventSink?) {
        audioRecord =
                AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, minBufferSize)
        audioRecord.startRecording()
        isRecording = true

        Thread {
                    val audioBuffer =
                            ShortArray(
                                    minBufferSize
                            ) // Step 1: Create ShortArray to hold audio data
                    while (isRecording) {
                        val readResult =
                                audioRecord.read(
                                        audioBuffer,
                                        0,
                                        minBufferSize
                                ) // Step 2: Read audio data into ShortArray
                        if (readResult > 0) {
                            // Step 3: Convert ShortArray to ByteArray
                            val byteBuffer = ByteArray(audioBuffer.size * 2)
                            for (i in audioBuffer.indices) {
                                val value = audioBuffer[i].toInt()
                                byteBuffer[i * 2] = (value and 0x00FF).toByte() // Lower 8 bits
                                byteBuffer[i * 2 + 1] =
                                        (value shr 8 and 0xFF).toByte() // Upper 8 bits
                            }
                            // Step 4: Post ByteArray to main thread to send through EventChannel
                            Handler(Looper.getMainLooper()).post { events?.success(byteBuffer) }
                        }
                    }
                }
                .start()
    }

    private fun stopRecording() {
        if (isRecording) {
            isRecording = false
            audioRecord.stop()
            audioRecord.release()
        }
    }
}
