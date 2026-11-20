package com.example.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

sealed interface RobloxLookupResult {
    data object Idle : RobloxLookupResult
    data object Loading : RobloxLookupResult
    data class Found(
        val userId: Long,
        val username: String,
        val displayName: String,
        val hasVerifiedBadge: Boolean,
        val avatarUrl: String?
    ) : RobloxLookupResult
    data class NotFound(val queriedUsername: String) : RobloxLookupResult
    data class Error(val message: String) : RobloxLookupResult
}

object RobloxAccountVerifier {
    private const val USERS_LOOKUP_URL = "https://users.roblox.com/v1/usernames/users"
    private const val AVATAR_HEADSHOT_URL = "https://thumbnails.roblox.com/v1/users/avatar-headshot"
    private val ROBLOX_USERNAME_REGEX = Regex("^[A-Za-z0-9_]{3,20}$")

    fun isValidUsernameSyntax(raw: String): Boolean {
        val trimmed = raw.trim()
        if (!ROBLOX_USERNAME_REGEX.matches(trimmed)) return false
        if (trimmed.startsWith("_") || trimmed.endsWith("_")) return false
        if (trimmed.count { it == '_' } > 1) return false
        return true
    }

    suspend fun lookupUsername(rawUsername: String): RobloxLookupResult = withContext(Dispatchers.IO) {
        val clean = rawUsername.trim()
        if (clean.isEmpty()) {
            return@withContext RobloxLookupResult.Error("আগে আপনার Roblox Username লিখুন।")
        }
        if (!isValidUsernameSyntax(clean)) {
            return@withContext RobloxLookupResult.NotFound(clean)
        }

        try {
            val payload = JSONObject().apply {
                put("usernames", JSONArray().apply { put(clean) })
                put("excludeBannedUsers", false)
            }.toString()

            val conn = (URL(USERS_LOOKUP_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
            }

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload)
                writer.flush()
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val responseText = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
            conn.disconnect()

            if (code !in 200..299) {
                return@withContext RobloxLookupResult.Error("Roblox সার্ভার থেকে তথ্য আনা যায়নি (HTTP $code)। আবার চেষ্টা করুন।")
            }

            val root = JSONObject(responseText)
            val dataArray = root.optJSONArray("data")
            if (dataArray == null || dataArray.length() == 0) {
                return@withContext RobloxLookupResult.NotFound(clean)
            }

            val first = dataArray.getJSONObject(0)
            val userId = first.getLong("id")
            val name = first.optString("name", clean)
            val displayName = first.optString("displayName", name)
            val hasVerifiedBadge = first.optBoolean("hasVerifiedBadge", false)
            val avatarUrl = fetchHeadshotUrl(userId)

            RobloxLookupResult.Found(
                userId = userId,
                username = name,
                displayName = displayName,
                hasVerifiedBadge = hasVerifiedBadge,
                avatarUrl = avatarUrl
            )
        } catch (e: Exception) {
            RobloxLookupResult.Error("ইন্টারনেট বা Roblox সংযোগে সমস্যা হয়েছে। অনুগ্রহ করে আবার চেষ্টা করুন।")
        }
    }

    private fun fetchHeadshotUrl(userId: Long): String? {
        return try {
            val url = URL("$AVATAR_HEADSHOT_URL?userIds=$userId&size=150x150&format=Png&isCircular=true")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Accept", "application/json")
            }
            val code = conn.responseCode
            val text = if (code in 200..299) {
                conn.inputStream.bufferedReader().use(BufferedReader::readText)
            } else {
                ""
            }
            conn.disconnect()
            if (text.isBlank()) return null
            val arr = JSONObject(text).optJSONArray("data") ?: return null
            if (arr.length() == 0) return null
            val item = arr.getJSONObject(0)
            if (item.optString("state") == "Completed") {
                item.optString("imageUrl").takeIf { it.isNotBlank() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}

object PaymentInputValidator {
    data class ValidationResult(
        val isValid: Boolean,
        val normalizedValue: String = "",
        val message: String = ""
    )

    private val BD_MOBILE_REGEX = Regex("^01[3-9]\\d{8}$")
    private val ALPHANUMERIC_UPPER_REGEX = Regex("^[A-Z0-9]+$")

    fun validateBdPhoneNumber(raw: String): ValidationResult {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(false, message = "১১ ডিজিটের মোবাইল নম্বর দিন (যেমন: 017XXXXXXXX)।")
        }
        if (trimmed.any { it.isWhitespace() }) {
            return ValidationResult(false, message = "নম্বরে কোনো স্পেস রাখবেন না — একসাথে ১১ ডিজিট দিন।")
        }
        if (!trimmed.all { it.isDigit() }) {
            return ValidationResult(false, message = "শুধুমাত্র ইংরেজি সংখ্যা (0-9) দিয়ে ১১ ডিজিট লিখুন।")
        }
        if (trimmed.length != 11) {
            return ValidationResult(
                isValid = false,
                message = "নম্বরটি ঠিক ১১ ডিজিটের হতে হবে (এখন ${trimmed.length}/11 ডিজিট হয়েছে)।"
            )
        }
        if (!BD_MOBILE_REGEX.matches(trimmed)) {
            return ValidationResult(
                isValid = false,
                message = "সঠিক বাংলাদেশি মোবাইল নম্বর দিন (013–019 দিয়ে শুরু ১১ ডিজিট)।"
            )
        }
        return ValidationResult(
            isValid = true,
            normalizedValue = trimmed,
            message = "১১ ডিজিট সঠিক আছে ($trimmed)"
        )
    }

    fun validateTransactionId(method: String, raw: String): ValidationResult {
        val trimmed = raw.trim()
        val isNagad = method.equals("Nagad", ignoreCase = true)
        if (trimmed.isEmpty()) {
            val hint = if (isNagad) "৮–১০ অক্ষরের" else "১০ অক্ষরের"
            return ValidationResult(false, message = "সঠিক $hint $method Transaction ID লিখুন।")
        }
        if (trimmed.any { it.isWhitespace() }) {
            return ValidationResult(
                isValid = false,
                message = "Transaction ID-তে কোনো স্পেস থাকে না (যেমন: 'ETF5Y FEG' ভুল)। একসাথে লিখুন।"
            )
        }
        val upper = trimmed.uppercase(Locale.US)
        if (!ALPHANUMERIC_UPPER_REGEX.matches(upper)) {
            return ValidationResult(
                isValid = false,
                message = "Transaction ID শুধুমাত্র ইংরেজি অক্ষর (A-Z) ও সংখ্যা (0-9) হতে হবে।"
            )
        }

        val validLength = if (isNagad) upper.length in 8..10 else upper.length == 10
        if (!validLength) {
            val expectedLabel = if (isNagad) "৮ থেকে ১০" else "ঠিক ১০"
            val exampleLabel = if (isNagad) "74CLKT6G" else "BKA9X23M10"
            return ValidationResult(
                isValid = false,
                message = "$method Transaction ID $expectedLabel অক্ষরের হয় (যেমন: $exampleLabel)। এখন ${upper.length} অক্ষর আছে।"
            )
        }

        val hasLetter = upper.any { it in 'A'..'Z' }
        val hasDigit = upper.any { it in '0'..'9' }
        if (!hasLetter || !hasDigit) {
            return ValidationResult(
                isValid = false,
                message = "আসল $method Transaction ID-তে অক্ষর ও সংখ্যা উভয়ই থাকে (যেমন: ${if (isNagad) "74CLKT6G" else "AFE8DCEWA2"})।"
            )
        }

        return ValidationResult(
            isValid = true,
            normalizedValue = upper,
            message = "সঠিক $method Transaction ID ফরম্যাট ($upper)"
        )
    }
}
