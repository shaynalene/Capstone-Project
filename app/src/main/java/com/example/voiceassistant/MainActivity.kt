package com.example.voiceassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var btnSpeak: Button
    private lateinit var tvResult: TextView
    private lateinit var tvSpeechInput: TextView
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSpeak = findViewById(R.id.btnSpeak)
        tvResult = findViewById(R.id.tvResult)
        tvSpeechInput = findViewById(R.id.tvSpeechInput)

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        // Initialize TextToSpeech
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language is not supported")
                }
            } else {
                Log.e(TAG, "Initialization failed")
            }
        }

        btnSpeak.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognition()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_REQUEST_RECORD_AUDIO)
            }
        }

        // Set up SpeechRecognizer listener
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "onReadyForSpeech")
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Optional: Handle UI changes or feedback based on microphone input
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Optional: Handle audio buffer received
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "onEndOfSpeech")
            }

            override fun onError(error: Int) {
                Log.e(TAG, "onError: $error")
                speechRecognizer.cancel()
            }

            override fun onResults(results: Bundle?) {
                results?.let {
                    val voiceCommands = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    voiceCommands?.let { commands ->
                        if (commands.isNotEmpty()) {
                            val voiceCommand = commands[0]
                            tvSpeechInput.text = voiceCommand // Update TextView with recognized speech
                            handleVoiceCommand(voiceCommand)
                        }
                    }
                }
                speechRecognizer.cancel()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Optional: Handle partial recognition results
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Optional: Handle events related to recognition
            }
        })
    }

    private fun startVoiceRecognition() {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        speechRecognizer.startListening(recognizerIntent)
    }

    private fun handleVoiceCommand(command: String) {
        val response = when {
            command.contains("hello", ignoreCase = true) -> "Hello! How can I assist you?"
            command.contains("time", ignoreCase = true) -> {
                val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                "The current time is $currentTime"
            }
            command.contains("weather", ignoreCase = true) -> "The weather today is sunny."
            else -> "Sorry, I didn't understand that."
        }

        // Display response in TextView and optionally speak it out loud
        tvResult.text = response
        speak(response)
    }

    private fun speak(text: String) {
        val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        if (result == TextToSpeech.ERROR) {
            Log.e(TAG, "Failed to speak: $text")
        }
    }

    override fun onDestroy() {
        // Shutdown SpeechRecognizer and TextToSpeech when closing the app
        speechRecognizer.destroy()

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startVoiceRecognition()
                } else {
                    Log.e(TAG, "Permission denied: RECORD_AUDIO")
                }
                return
            }
            else -> {
                // Handle other permissions if needed
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
    }
}

