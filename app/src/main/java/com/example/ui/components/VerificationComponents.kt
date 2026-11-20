package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.repository.PaymentInputValidator
import com.example.data.repository.RobloxLookupResult
import com.example.ui.theme.BlackCardBorder
import com.example.ui.theme.BlackSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RobloxUsernameVerifyWidget(
    usernameInput: String,
    lookupResult: RobloxLookupResult,
    onCheckClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onCheckClick,
            enabled = lookupResult !is RobloxLookupResult.Loading && usernameInput.trim().isNotEmpty(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = BlackSurfaceElevated,
                contentColor = ElectricCyan,
                disabledContainerColor = BlackSurfaceElevated.copy(alpha = 0.5f),
                disabledContentColor = TextMuted
            ),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .testTag("check_roblox_username_button")
        ) {
            if (lookupResult is RobloxLookupResult.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = ElectricCyan,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Roblox-এ গেম ID চেক করা হচ্ছে...", style = MaterialTheme.typography.labelLarge)
            } else {
                Icon(
                    imageVector = Icons.Filled.PersonSearch,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "আপনার গেম ID-এর নাম চেক করুন",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
            }
        }

        when (lookupResult) {
            is RobloxLookupResult.Found -> {
                Surface(
                    color = SuccessGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SuccessGreen.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .testTag("roblox_verified_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!lookupResult.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(lookupResult.avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Roblox Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, SuccessGreen, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Game Name: ${lookupResult.displayName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                if (lookupResult.hasVerifiedBadge) {
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = "Verified Roblox User",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Username: @${lookupResult.username}  •  ID: ${lookupResult.userId}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                                color = SuccessGreen
                            )
                        }
                    }
                }
            }
            is RobloxLookupResult.NotFound -> {
                Surface(
                    color = ErrorRed.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ErrorRed.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .testTag("roblox_not_found_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "\"${lookupResult.queriedUsername}\" নামের কোনো গেম ID Roblox-এ পাওয়া যায়নি! সঠিক Username দিন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorRed
                        )
                    }
                }
            }
            is RobloxLookupResult.Error -> {
                Surface(
                    color = BlackSurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BlackCardBorder, RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = lookupResult.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            else -> Unit
        }
    }
}

@Composable
fun LivePhoneValidationHint(
    phoneInput: String,
    modifier: Modifier = Modifier
) {
    val trimmed = phoneInput.trim()
    val result = PaymentInputValidator.validateBdPhoneNumber(trimmed)
    val digitsCount = trimmed.count { it.isDigit() }
    val statusColor = when {
        trimmed.isEmpty() -> TextMuted
        result.isValid -> SuccessGreen
        else -> ErrorRed
    }
    val hintText = when {
        trimmed.isEmpty() -> "১১ ডিজিটের নম্বর দিন (যেমন: 017XXXXXXXX) — ০/১১"
        result.isValid -> "✓ ${result.message}"
        else -> "${result.message} ($digitsCount/11)"
    }
    Text(
        text = hintText,
        style = MaterialTheme.typography.labelSmall,
        color = statusColor,
        modifier = modifier.padding(start = 4.dp)
    )
}

@Composable
fun LiveTrxIdValidationHint(
    method: String,
    trxInput: String,
    modifier: Modifier = Modifier
) {
    val trimmed = trxInput.trim()
    val result = PaymentInputValidator.validateTransactionId(method, trimmed)
    val isNagad = method.equals("Nagad", ignoreCase = true)
    val statusColor = when {
        trimmed.isEmpty() -> TextMuted
        result.isValid -> SuccessGreen
        else -> ErrorRed
    }
    val hintText = when {
        trimmed.isEmpty() -> if (isNagad) {
            "Nagad TxnID সাধারণত ৮–১০ অক্ষরের হয় (স্পেস ছাড়া, যেমন: 74CLKT6G)"
        } else {
            "bKash TrxID ঠিক ১০ অক্ষরের হয় (স্পেস ছাড়া, যেমন: BKA9X23M10)"
        }
        result.isValid -> "✓ ${result.message}"
        else -> result.message
    }
    Text(
        text = hintText,
        style = MaterialTheme.typography.labelSmall,
        color = statusColor,
        modifier = modifier.padding(start = 4.dp)
    )
}
