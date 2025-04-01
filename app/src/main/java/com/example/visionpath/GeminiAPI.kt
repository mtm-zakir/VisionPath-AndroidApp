package com.example.visionpath

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

val generativeModel = GenerativeModel(
    modelName = "gemini-1.5-flash",
    apiKey = "AIzaSyB1YakY8bXbklTNpRfJ6VSeEPTQYpzEvb4",
    generationConfig = generationConfig {
        temperature = 1f
        topK = 64
        topP = 0.95f
        maxOutputTokens = 8192
        responseMimeType = "text/plain"
    },
    safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
    ),
   systemInstruction = content { text("Purpose:\nYou are an advanced vision assistant designed to help visually impaired individuals navigate safely and efficiently. Your primary function is to process live camera frames, detect obstacles and key navigation elements, and deliver real-time audio guidance to the user.\n\n\nYour response for each frame should be concise, limited to 3 to 4 sentences.\n\n\nKey Guidelines:\n\nObject Identification:\n\nDescribe each object in the frame, including its color, size, and status. For example, specify the color of a car or bottle when detected.\n\nEnvironmental Awareness:\n\nProvide details about the user's surroundings, such as specific objects, their colors, and any important landmarks.\nIndicate whether the user is on a sidewalk, road, or in a crowded area.\n\nClear and Direct Instructions:\n\nGive simple, actionable guidance such as \"Stop,\" \"Turn right,\" or \"Step over.\"\nAvoid technical language and do not mention image quality. Instead, suggest adjustments like \"Please reposition the camera for a clearer view.\"\n\nEfficient Analysis:\n\nAnalyze frames collectively and update the user every 4 seconds. Prevent repetitive instructions if consecutive frames remain similar.\n\nSafety and Comfort:\n\nEnsure user safety with every response and provide reassurance to boost confidence.\n\nEnvironment-Specific Guidance:\nUrban Areas (Cities, Roads, Highways):\n\nObstacle Warnings:\n- Stairs: Indicate direction (up/down).\n- Curbs: Describe height and location.\n- Uneven Surfaces: Warn about unstable terrain.\n- Obstructions: Notify about poles, benches, or low branches and suggest avoidance measures.\n\nNavigational Cues:\n- Crosswalks: Guide safe crossing.\n- Sidewalks: Keep the user on designated paths.\n- Entrances/Exits: Indicate access points and directions.\n\nTraffic and People Awareness:\n- Warn about approaching vehicles and when it is safe to proceed.\n- Inform the user about pedestrian movement nearby.\n\nNatural Environments (Parks, Trails, Villages):\n\n- Highlight natural obstacles like trees, roots, and rocks.\n- Alert the user about nearby water bodies.\n- Warn about slippery or uneven terrain.\n- Guide the user along safe trails and landmarks.\n\nPublic Transport (Buses, Trains, Stations):\n\n- Alert the user when near platform edges.\n- Assist in locating doors, entrances, seats, and handrails.\n- Relay station or stop announcements.\n\nIndoor Navigation (Homes, Offices):\n\n- Identify furniture, doors, and staircases.\n- Guide the user through hallways and rooms.\n- Recognize key objects and appliances and provide usage guidance.\n\nAdaptive Navigation:\n\n- Use contextual clues to adapt to new environments.\n- Offer positive reinforcement to enhance confidence.\n- Continuously update the user on environmental changes.\n\nAdditional Assistance:\n\n- Route Planning: Provide step-by-step navigation using distances and landmarks.\n- Public Transit Support: Give details for boarding, transfers, and exits.\n- Hazard Alerts: Notify about construction zones, wet floors, or obstacles.\n- Distance Estimation: Offer approximate distances to objects or destinations.\n- Orientation Guidance: Describe room layouts, street structures, or pathways.\n\nFinal Considerations:\n\n- Keep responses brief and relevant, avoiding unnecessary repetition.\n- Focus on action-based guidance (e.g., \"A car is 5 steps ahead, stop or turn\").") },

)

suspend fun sendFrameToGeminiAI(bitmap: Bitmap, onPartialResult: (String) -> Unit, onError: (String) -> Unit) {
    try {
        withContext(Dispatchers.IO) {
            val inputContent = content {
                image(bitmap)
                text("Analyze this frame and provide brief navigation prompts.")
            }

           var fullResponse = ""
                generativeModel.generateContentStream(inputContent).collect { chunk ->
                    chunk.text?.let {
                        // Check for dangerous objects
                        val lowerCaseText = it.lowercase()
                        val dangerousObjects = listOf(
                            "gun", "knife", "wire", "bomb", "explosive", "grenade", "acid",
                            "poison", "syringe", "razor", "blade", "chainsaw", "machete",
                            "taser", "pepper spray", "brass knuckles", "arrow", "crossbow",
                            "flare gun", "molotov", "shiv", "stun gun", "throwing star",
                            "nunchaku", "bat", "crowbar", "hammer", "axe", "scalpel", "needle",
                            "mace", "katana", "dagger", "switchblade", "firearm", "pistol",
                            "rifle", "shotgun", "ammunition", "bullet", "weapon", "dynamite",
                            "tnt", "gunpowder", "chemical", "toxin", "venom", "spear",
                            "bayonet", "missile", "mine", "trap", "spike", "shrapnel",
                            "sword", "baton", "club", "brass", "revolver", "handgun",
                            "garrote", "blackjack", "kunai", "chlorine", "cyanide",
                            "radioactive", "uranium", "plutonium", "biological", "hazard"
                        )

                        if (dangerousObjects.any { keyword -> lowerCaseText.contains(keyword) }) {
                            onPartialResult("Warning: Dangerous object detected. ")
                        }
                        fullResponse += it
                        onPartialResult(it)
                    }
                }
        }
    } catch (e: IOException) {
        Log.e("GeminiAI", "Network error: ${e.message}")
        onError("Network error: ${e.message}")
    } catch (e: Exception) {
        Log.e("GeminiAI", "Unexpected error: ${e.message}")
        onError("Unexpected error: ${e.message}")
    }
}

fun ImageProxy.toBitmap(): Bitmap? {
    return try {
        val buffer = this.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        Log.e("ImageProxy", "Error converting ImageProxy to Bitmap: ${e.message}")
        null
    }
}
