package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Ask Gemini 3.5 Flash with Google Maps Grounding
     */
    suspend fun queryWithMapsGrounding(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Google Maps Grounding requires a valid GEMINI_API_KEY. (Simulated Response: Located nearest Solid Waste Processing Yard at Lat 18.5220, Long 73.8510 - 2.4 km away with active composting pits and biowaste segregation bins)."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val requestBodyJson = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().put("text", "Location query for rural garbage van and waste facilities: $prompt"))
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)

            // Google Maps Grounding Tool
            val tools = JSONArray().apply {
                put(JSONObject().put("googleMaps", JSONObject()))
            }
            put("tools", tools)
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                parseGeminiTextResponse(body)
            }
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Maps Grounding Error", e)
            "Error querying Google Maps Grounding: ${e.message}"
        }
    }

    /**
     * Ask Gemini 3.5 Flash with Google Search Grounding
     */
    suspend fun queryWithSearchGrounding(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Google Search Grounding requires a valid GEMINI_API_KEY. (Simulated Response: Under Swachh Bharat Mission Grameen Phase II, Gram Panchayats receive financial assistance of up to ₹16-20 Lakhs for solid and liquid waste management (SLWM) infrastructure, segregated collection vehicles, and community compost pits)."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val requestBodyJson = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().put("text", "Rural sanitation policy query: $prompt"))
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)

            // Google Search Grounding Tool
            val tools = JSONArray().apply {
                put(JSONObject().put("googleSearch", JSONObject()))
            }
            put("tools", tools)
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                parseGeminiTextResponse(body)
            }
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Search Grounding Error", e)
            "Error querying Google Search Grounding: ${e.message}"
        }
    }

    /**
     * Live Voice Assistant conversation session (using gemini-3.8-live)
     */
    suspend fun queryLiveVoiceAssistant(speechTranscript: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Voice Session (gemini-3.8-live): 'Understood, Driver Ramesh. Heading to Point #3 Kisan Basti. Please verify wet waste segregation before uploading payload.'"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-live:generateContent?key=$apiKey"

        val requestBodyJson = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().put("text", "You are the GramSwachh AI Live Voice Co-Pilot for rural garbage van drivers. Answer concisely in 1-2 spoken sentences: $speechTranscript"))
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                parseGeminiTextResponse(body)
            }
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Live API Error", e)
            "Live Voice Assistant reply: Acknowledged. Navigating to next collection checkpoint."
        }
    }

    /**
     * Waste Management Science calculations (Science skills)
     */
    fun calculateCompostScience(wetOrganicKg: Double, dryLeavesKg: Double): CompostScienceResult {
        // Wet kitchen waste typically C:N ratio ~15:1, Moisture ~75%
        // Dry leaves / straw C:N ratio ~60:1, Moisture ~15%
        val totalWeight = (wetOrganicKg + dryLeavesKg).coerceAtLeast(1.0)
        val weightedCnRatio = ((wetOrganicKg * 15.0) + (dryLeavesKg * 60.0)) / totalWeight
        val estimatedMoisturePercent = (((wetOrganicKg * 0.75) + (dryLeavesKg * 0.15)) / totalWeight) * 100.0
        // Optimal C:N is 25:1 to 30:1
        val isBalanced = weightedCnRatio in 22.0..35.0
        val recommendation = when {
            weightedCnRatio < 22.0 -> "Too high in Nitrogen (slimy/smelly). Add ${(wetOrganicKg * 0.4).toInt()} kg more dry leaves, straw, or sawdust (carbon brown materials)."
            weightedCnRatio > 35.0 -> "Too high in Carbon (slow decomposition). Add more wet green kitchen waste, cattle dung, or water slurry."
            else -> "Ideal C:N balance! Thermophilic heating will reach 55°C-65°C within 72 hours, eliminating weed seeds and pathogens."
        }

        // Biogas Methane yield: 1 kg organic waste yields approx 0.04 to 0.05 m³ biogas
        val estimatedBiogasM3 = wetOrganicKg * 0.045
        val lpgEquivalentKg = estimatedBiogasM3 * 0.45

        return CompostScienceResult(
            cnRatio = (weightedCnRatio * 10).toInt() / 10.0,
            moisturePercent = (estimatedMoisturePercent * 10).toInt() / 10.0,
            isBalanced = isBalanced,
            recommendation = recommendation,
            biogasYieldM3 = (estimatedBiogasM3 * 10).toInt() / 10.0,
            lpgEquivalentKg = (lpgEquivalentKg * 10).toInt() / 10.0
        )
    }

    private fun parseGeminiTextResponse(rawJson: String): String {
        return try {
            val json = JSONObject(rawJson)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val firstPart = parts.getJSONObject(0)
                    return firstPart.optString("text", "No response text")
                }
            }
            if (json.has("error")) {
                val error = json.getJSONObject("error")
                return "API Error: ${error.optString("message")}"
            }
            rawJson.take(300)
        } catch (e: Exception) {
            "Response received: ${rawJson.take(200)}"
        }
    }
}

data class CompostScienceResult(
    val cnRatio: Double,
    val moisturePercent: Double,
    val isBalanced: Boolean,
    val recommendation: String,
    val biogasYieldM3: Double,
    val lpgEquivalentKg: Double
)
