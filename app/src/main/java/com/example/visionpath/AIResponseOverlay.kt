package com.example.visionpath

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card

@Composable
fun AIResponseOverlay(
    currentMode: String,
    navigationResponse: String,
    chatResponse: String,
    readingModeResult: String,
    tts: TextToSpeech?,
    lastSpokenIndex: Int,
    response: String
) {
    val context = LocalContext.current
    var isConnected = remember { mutableStateOf(isInternetAvailable(context)) }
    var currentIndex by remember { mutableStateOf(lastSpokenIndex) } // Track the current sentence index
    val sentences = response
        .replace("..", ".")  // Fix multiple periods
        .replace(" .", ".")  // Fix space before period
        .replace("  ", " ")  // Fix double spaces
        .split(".")         // Split into sentences
        .map { it.trim() }  // Trim each sentence
        .filter { it.isNotEmpty() }  // Remove empty sentences
    var lastSpokenText by remember { mutableStateOf("") } // Track the last spoken text

    // Format text for natural speech
    fun formatTextForSpeech(text: String): String {
        return text
            .replace("  ", " ")
            .replace(" .", ".")
            .replace("..", ".")
            .trim()
    }

    // Continuously check internet connectivity
    LaunchedEffect(Unit) {
        while (true) {
            isConnected.value = isInternetAvailable(context)
            delay(3000) // Check every 3 seconds
        }
    }

    // Skip to the next sentence every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // Wait for 5 seconds
            if (isConnected.value && sentences.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % sentences.size
                val newText = formatTextForSpeech(sentences[currentIndex])
                if (newText.isNotEmpty() && newText != lastSpokenText) {
                    tts?.speak(newText, TextToSpeech.QUEUE_FLUSH, null, null)
                    lastSpokenText = newText
                }
            }
        }
    }

    LaunchedEffect(response) {
        if (sentences.isNotEmpty()) {
            val newText = formatTextForSpeech(sentences[currentIndex])
            if (newText.isNotEmpty() && newText != lastSpokenText) {
                tts?.speak(newText, TextToSpeech.QUEUE_ADD, null, null)
                lastSpokenText = newText
            }
        }
    }

    LaunchedEffect(currentMode, navigationResponse, chatResponse, readingModeResult) {
        when (currentMode) {
            "navigation" -> {
                val newText = formatTextForSpeech(navigationResponse.substring(lastSpokenIndex))
                tts?.speak(newText, TextToSpeech.QUEUE_ADD, null, null)
            }
            "assistant" -> {
                // Don't automatically speak in assistant mode
            }
            "reading" -> {
                // Don't automatically speak in reading mode
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isConnected.value) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(5.dp),  // Changed from MaterialTheme.shapes.medium
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You are not connected to the internet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    tts?.speak("You are not connected to the internet", TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(5.dp),  // Changed from MaterialTheme.shapes.medium
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)  // Made more transparent
                )
            ) {
                val scrollState = rememberScrollState()

                LaunchedEffect(scrollState.maxValue) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .verticalScroll(scrollState)
                ) {
                    when (currentMode) {
                        "reading" -> {
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),  // Made more transparent
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = formatTextForSpeech(readingModeResult),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f),  // Added text transparency
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                        "assistant" -> {
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),  // Made more transparent
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = formatTextForSpeech(chatResponse),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f),  // Added text transparency
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                        "navigation" -> {
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),  // Made more transparent
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = formatTextForSpeech(navigationResponse),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f),  // Added text transparency
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
