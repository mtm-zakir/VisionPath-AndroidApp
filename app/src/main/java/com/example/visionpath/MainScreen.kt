package com.example.visionpath

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.util.Locale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import android.speech.tts.UtteranceProgressListener
import com.example.visionpath.components.PulsingCircles
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.delay
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainPage(navController: NavHostController) {
    val context = LocalContext.current
    val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        if (allGranted) {
            // Initialize TTS and speech recognition only after permissions are granted
            initializeVoiceFeatures()
        }
    }

    // Check permissions on first launch
    LaunchedEffect(Unit) {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    val haptic = LocalHapticFeedback.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isListening by remember { mutableStateOf(true) }
    var isFirstActivation by remember { mutableStateOf(true) }
    var instructionsGiven by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val timeoutDuration = 20000L // 20 seconds in milliseconds
    var lastVoiceInputTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val noVoiceTimeoutDuration = 20000L // 20 seconds timeout
    val animatedScale by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    // Add timeout checker
    LaunchedEffect(isListening) {
        while (isListening) {
            delay(10000) // Check every 10 second
            if (System.currentTimeMillis() - lastInteractionTime > timeoutDuration) {
                // Reset interaction time
                lastInteractionTime = System.currentTimeMillis()

                // Restart the process
                tts?.speak(
                    "Say Hey Metro for Instructions, and Say Start Metro, or double tap anywhere to start Blind Mode.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "timeout_restart"
                )
            }
        }
    }

    // Add voice input timeout checker
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Check every second
            if (isListening && !isSpeaking && System.currentTimeMillis() - lastVoiceInputTime > noVoiceTimeoutDuration) {
                // Reset timer
                lastVoiceInputTime = System.currentTimeMillis()
                // Repeat welcome message
                tts?.speak(
                    "Welcome to VisionPath. Say Hey Metro for Instructions, and Say Start Metro, or double tap anywhere to start Blind Mode.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "timeout_restart"
                )
            }
        }
    }

    // Voice activation setup
    DisposableEffect(Unit) {
        // Initialize TTS immediately
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.apply {
                    // Configure TTS settings immediately
                    language = Locale.US
                    setSpeechRate(0.9f)
                    setPitch(1.0f)  // Normal pitch

                    // Set listener before speaking
                    setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                isSpeaking = true
                            }
                        }

                        override fun onDone(utteranceId: String?) {
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                isSpeaking = false
                                // Vibrate after 1 second when transitioning to listening mode
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }, 1000)
                            }
                        }

                        override fun onError(utteranceId: String?) {
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                isSpeaking = false
                            }
                        }
                    })

                    // Speak immediately after configuration
                    speak(
                        "Welcome to VisionPath. Say Hey Metro for Instructions, and Say Start Metro, or double tap anywhere to start Blind Mode.",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "initial_greeting"
                    )
                }
            }
        }

        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Hey Metro for instructions and Say Start Metro, or double tap anywhere to start Blind Mode")
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                // Update last interaction time when there's any voice input
                lastInteractionTime = System.currentTimeMillis()
                lastVoiceInputTime = System.currentTimeMillis() // Reset timer on voice input
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { result ->
                    when {
                        result.lowercase().contains("hey metro") -> {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isSpeaking = true
                            if (isFirstActivation) {
                                // First-time instructions
                                tts?.speak(
                                    "Let me explain how to use the app. The default screen is blind mode. " +
                                    "In blind mode screen, double tap anywhere to enter or exit assistant mode. " +
                                    "Also in blind mode screen, long press and hold to enter or exit reading mode. " +
                                    "Okay! Now, say Start Metro, or double tap anywhere in screen to start blind mode.",
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "instructions"
                                )
                                isFirstActivation = false
                                instructionsGiven = true
                            } else if (instructionsGiven) {
                                // Open Blind Mode if instructions have been given
                                tts?.speak(
                                    "Starting Blind Mode",
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "start_blind_mode"
                                )
                                isListening = false
                                // Short delay to allow TTS to complete before Starting Blind Mode
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    navController.navigate("blindMode")
                                }, 1000)
                            }
                            // Remove the delayed isSpeaking = false handlers since we're using UtteranceProgressListener
                            speechRecognizer.startListening(speechIntent)
                        }
                        result.lowercase().contains("start metro") -> {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isSpeaking = true
                            tts?.speak(
                                "Starting Blind Mode",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "start_blind_mode"
                            )
                            isListening = false
                            // Short delay to allow TTS to complete before Starting Blind Mode
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                navController.navigate("blindMode")
                            }, 100)
                            // Remove the delayed isSpeaking = false handlers since we're using UtteranceProgressListener
                        }
                        else -> {}
                    }
                }
                // Restart listening if we're still in listening mode
                if (isListening) {
                    speechRecognizer.startListening(speechIntent)
                }
            }

            override fun onReadyForSpeech(p0: Bundle?) {
                lastInteractionTime = System.currentTimeMillis()
            }
            override fun onBeginningOfSpeech() {
                lastInteractionTime = System.currentTimeMillis()
                lastVoiceInputTime = System.currentTimeMillis() // Reset timer when speech begins
                // Remove vibration from here as we're handling it in TTS onDone
            }
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {
                // Add haptic feedback when done listening
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                // Only restart if we're still listening
                if (isListening) {
                    speechRecognizer.startListening(speechIntent)
                }
            }
            override fun onError(p0: Int) {
                // Restart listening on error if we're still in listening mode
                if (isListening) {
                    speechRecognizer.startListening(speechIntent)
                }
            }
            override fun onPartialResults(p0: Bundle?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })

        // Start listening
        speechRecognizer.startListening(speechIntent)

        onDispose {
            isListening = false
            speechRecognizer.destroy()
            tts?.stop()
            tts?.shutdown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),  // Removed padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E0039), // Dark purple
                                Color(0xFFB2186B)  // Pink
                            )
                        )
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isSpeaking = true
                                tts?.speak(
                                    "Starting Blind Mode",
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "start_blind_mode"
                                )
                                isListening = false
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    navController.navigate("blindMode")
                                }, 300)
                                // Remove the delayed isSpeaking = false handlers since we're using UtteranceProgressListener
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PulsingCircles(
                        isSpeaking = isSpeaking,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Text(
                        text = if (isSpeaking) "Speaking..." else "Listening...",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 40.sp
                    )
                }
            }
        }

        // Copyright text at bottom
        Text(
            text = "Developed by Team Metro Dev",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

fun initializeVoiceFeatures() {
    TODO("Not yet implemented")
}
