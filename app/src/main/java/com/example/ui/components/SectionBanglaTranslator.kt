package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.InitialCatalog
import com.example.data.model.StoreConfig
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftBlueGlowBorder
import com.example.ui.theme.SoftBlueSurfaceTint
import com.example.ui.theme.TextSecondary

/**
 * Instant, zero-lag per-section English <-> Bangla translation helper.
 * Keeps the rest of the app unchanged while letting users translate any specific
 * informational box, support card, FAQ, policy, or instruction on tap.
 */
object SectionBanglaTranslator {

    private val dynamicPhraseMap: List<Pair<Regex, String>> = listOf(
        Regex("\\bOfficial MSR BD Game Top-Up Support Desk\\b", RegexOption.IGNORE_CASE) to "অফিশিয়াল MSR BD Game Top-Up সাপোর্ট ডেস্ক",
        Regex("\\bWelcome to MSR BD Game Top-Up!?\\b", RegexOption.IGNORE_CASE) to "MSR BD Game Top-Up-এ স্বাগতম!",
        Regex("\\bOfficial Roblox Robux & Plus Subscriptions\\b", RegexOption.IGNORE_CASE) to "অফিশিয়াল Roblox Robux এবং Plus সাবস্ক্রিপশন",
        Regex("\\bwith instant bKash verification\\b", RegexOption.IGNORE_CASE) to "দ্রুত bKash ভেরিফিকেশনসহ",
        Regex("\\bTransaction ID\\b", RegexOption.IGNORE_CASE) to "ট্রানজেকশন আইডি (TrxID)",
        Regex("\\bSend Money\\b", RegexOption.IGNORE_CASE) to "Send Money",
        Regex("\\bRoblox Username\\b", RegexOption.IGNORE_CASE) to "Roblox Username",
        Regex("\\bLoyalty Points\\b", RegexOption.IGNORE_CASE) to "লয়্যালটি পয়েন্ট",
        Regex("\\bMonthly Bonus Drops?\\b", RegexOption.IGNORE_CASE) to "মাসিক বোনাস ড্রপ",
        Regex("\\bPayment Verification\\b", RegexOption.IGNORE_CASE) to "পেমেন্ট যাচাইকরণ",
        Regex("\\bOrder ID\\b", RegexOption.IGNORE_CASE) to "অর্ডার আইডি",
        Regex("\\bPending\\b", RegexOption.IGNORE_CASE) to "পেন্ডিং (অপেক্ষমাণ)",
        Regex("\\bProcessing\\b", RegexOption.IGNORE_CASE) to "প্রসেসিং চলছে",
        Regex("\\bCompleted\\b", RegexOption.IGNORE_CASE) to "সম্পন্ন (Done)",
        Regex("\\bRejected\\b", RegexOption.IGNORE_CASE) to "বাতিল (Rejected)",
        Regex("\\bPlease\\b", RegexOption.IGNORE_CASE) to "অনুগ্রহ করে",
        Regex("\\bContact support\\b", RegexOption.IGNORE_CASE) to "সাপোর্টে যোগাযোগ করুন",
        Regex("\\bCheck back soon\\b", RegexOption.IGNORE_CASE) to "কিছুক্ষণ পর আবার দেখুন",
        Regex("\\bNo password needed\\b", RegexOption.IGNORE_CASE) to "কোনো পাসওয়ার্ড লাগবে না",
        Regex("\\bThank you\\b", RegexOption.IGNORE_CASE) to "ধন্যবাদ"
    )

    fun translateSupportInfo(english: String, phone: String): String {
        val defaultText = StoreConfig().supportInfo
        if (english.trim() == defaultText.trim()) {
            return "অফিশিয়াল MSR BD Game Top-Up সাপোর্ট ডেস্ক। পেমেন্ট যাচাই এবং অর্ডার ডেলিভারির জন্য প্রতিদিন সকাল ৯:০০টা থেকে রাত ১২:০০টা পর্যন্ত চালু থাকে।"
        }
        return translateDynamicEnglish(english) + "\n(অফিশিয়াল সাপোর্ট নম্বর: $phone)"
    }

    fun translatePaymentInstructions(english: String, recipientNumber: String, method: String): String {
        val defaultText = StoreConfig().paymentInstructions
        if (english.trim() == defaultText.trim()) {
            return "$method অ্যাপ খুলুন -> Send Money অপশনে যান -> অফিশিয়াল নম্বর $recipientNumber লিখুন -> সঠিক মোট টাকা (Total Amount) পাঠান -> পাওয়া Transaction ID (TrxID) কপি করে নিচে দিন।"
        }
        return translateDynamicEnglish(english)
    }

    fun translatePrivacyPolicy(english: String): String {
        val defaultText = StoreConfig().privacyPolicyText
        if (english.trim() == defaultText.trim()) {
            return "MSR BD Game Top-Up শুধুমাত্র আপনার Google অ্যাকাউন্টের নাম, ইমেইল, গেম আইডি/Username এবং অর্ডার যাচাইয়ের জন্য bKash/Nagad পেমেন্ট তথ্য সংগ্রহ করে। আমরা কখনোই আপনার Roblox, Google বা ব্যক্তিগত কোনো পাসওয়ার্ড চাই না এবং সংরক্ষণও করি না।"
        }
        return translateDynamicEnglish(english)
    }

    fun translateTerms(english: String): String {
        val defaultText = StoreConfig().termsText
        if (english.trim() == defaultText.trim()) {
            return "সব ডিজিটাল টপ-আপ ও সাবস্ক্রিপশন ওনার (Owner) আপনার Transaction ID ম্যানুয়ালি যাচাই করার পর ডেলিভারি দেওয়া হয়। শুধুমাত্র সফল (Done) হওয়া অর্ডার থেকেই লয়্যালটি পয়েন্ট যোগ হয়। অর্ডার দেওয়ার আগে অবশ্যই আপনার Roblox Username ভালোভাবে যাচাই করে নিন।"
        }
        return translateDynamicEnglish(english)
    }

    fun translateRewardDescription(english: String, config: StoreConfig): String {
        val defaultText = StoreConfig().rewardDescription
        if (english.trim() == defaultText.trim()) {
            return "প্রতি ৳১ টাকা সফল (Done) খরচে ১টি লয়্যালটি পয়েন্ট পাবেন। পয়েন্ট জমিয়ে Silver (${config.silverThreshold}), Gold (${config.goldThreshold}) এবং Platinum (${config.platinumThreshold}) VIP মেম্বারশিপ আনলক করুন।"
        }
        return translateDynamicEnglish(english)
    }

    fun translateAnnouncement(english: String): String {
        val defaultText = StoreConfig().announcementBanner
        if (english.trim() == defaultText.trim()) {
            return "MSR BD Game Top-Up-এ স্বাগতম! দ্রুত bKash ও Nagad ভেরিফিকেশনসহ অফিশিয়াল Roblox Robux ও Plus সাবস্ক্রিপশন নিন।"
        }
        return translateDynamicEnglish(english)
    }

    fun translateMaintenance(english: String): String {
        val defaultText = StoreConfig().maintenanceMessage
        if (english.trim() == defaultText.trim()) {
            return "অ্যাপটি আরও দ্রুত ও উন্নত করতে সাময়িক আপগ্রেডের কাজ চলছে। অনুগ্রহ করে কিছুক্ষণ পর আবার চেষ্টা করুন।"
        }
        return translateDynamicEnglish(english)
    }

    fun translateGameDescription(english: String, gameName: String): String {
        val defaultRoblox = InitialCatalog.defaultGames.first().description
        if (english.trim() == defaultRoblox.trim()) {
            return "দ্রুত bKash ও Nagad পেমেন্ট ভেরিফিকেশনসহ অফিশিয়াল Roblox Robux টপ-আপ ও Plus সাবস্ক্রিপশন।"
        }
        if (english.isBlank() || english.contains("Official Packages", ignoreCase = true)) {
            return "$gameName-এর অফিশিয়াল প্যাকেজ • bKash ও Nagad পেমেন্ট"
        }
        return translateDynamicEnglish(english)
    }

    fun translateFaqQuestion(faqId: String, englishQuestion: String): String {
        return when (faqId) {
            "faq_1" -> "আমি কীভাবে Roblox টপ-আপের পেমেন্ট করব?"
            "faq_2" -> "আপনাদের কি কখনো আমার Roblox বা Google পাসওয়ার্ড লাগবে?"
            "faq_3" -> "পেমেন্ট ভেরিফাই হতে কত সময় লাগে?"
            "faq_4" -> "লয়্যালটি পয়েন্ট এবং মেম্বারশিপ লেভেল কীভাবে কাজ করে?"
            else -> when {
                englishQuestion.contains("pay", ignoreCase = true) ->
                    "আমি কীভাবে পেমেন্ট করব? (${translateDynamicEnglish(englishQuestion)})"
                englishQuestion.contains("password", ignoreCase = true) ->
                    "পাসওয়ার্ড কি লাগবে? — ${translateDynamicEnglish(englishQuestion)}"
                else -> translateDynamicEnglish(englishQuestion)
            }
        }
    }

    fun translateFaqAnswer(faqId: String, englishAnswer: String): String {
        return when (faqId) {
            "faq_1" -> "আপনার পছন্দের Robux বা Subscription প্যাকেজ বেছে নিন, তারপর 01354246861 নম্বরে সঠিক টাকা bKash/Nagad Send Money করুন। এরপর চেকআউট পেজে আপনার Roblox Username, পেমেন্ট নম্বর এবং Transaction ID (TrxID) দিয়ে অর্ডার সাবমিট করুন।"
            "faq_2" -> "একদমই না! MSR BD Game Top-Up কখনোই আপনার Roblox পাসওয়ার্ড, Google পাসওয়ার্ড বা কোনো ব্যক্তিগত পাসওয়ার্ড চায় না। অর্ডার পাঠানোর জন্য শুধুমাত্র আপনার পাবলিক Roblox Username প্রয়োজন হয়।"
            "faq_3" -> "অর্ডার দেওয়ার পর সেটি Pending অবস্থায় থাকে। আমাদের টিম আপনার Transaction ID যাচাই করে পেমেন্ট ভেরিফিকেশন, প্রসেসিং এবং সবশেষে Done (সম্পন্ন) করে দেয়।"
            "faq_4" -> "প্রতি ৳১ টাকা সফল ও ভেরিফাইড খরচের জন্য ১টি লয়্যালটি পয়েন্ট পাবেন। Pending বা বাতিল (Rejected) অর্ডারে পয়েন্ট যোগ হয় না। ১,০০০ পয়েন্টে SILVER, ৫,০০০ পয়েন্টে GOLD এবং ১২,৩০০ পয়েন্টে PLATINUM লেভেল আনলক হয়।"
            else -> translateDynamicEnglish(englishAnswer)
        }
    }

    fun translateDynamicEnglish(english: String): String {
        val trimmed = english.trim()
        if (trimmed.isEmpty()) return ""
        // If already mostly Bangla script, return as-is cleanly
        val banglaChars = trimmed.count { it in '\u0980'..'\u09FF' }
        if (banglaChars > trimmed.length / 3) return trimmed

        var translated = trimmed
        dynamicPhraseMap.forEach { (regex, bangla) ->
            translated = regex.replace(translated, bangla)
        }
        return if (translated != trimmed) {
            translated
        } else {
            "বাংলা অনুবাদ: $trimmed"
        }
    }
}

/**
 * Compact capsule pill with >=48dp accessible touch height, designed to sit cleanly
 * beside action labels (e.g., next to Help/Orders or at the end of English descriptions).
 */
@Composable
fun BanglaTranslatePill(
    isBangla: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    testTag: String? = null
) {
    val label = if (isBangla) {
        if (compact) "EN" else "English"
    } else {
        if (compact) "বাংলা" else "বাংলা অনুবাদ"
    }
    val accessibilityText = if (isBangla) "Switch back to English" else "বাংলায় অনুবাদ দেখুন"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(50))
            .clickable(
                role = Role.Button,
                onClick = onToggle
            )
            .semantics { contentDescription = accessibilityText }
            .let { if (testTag != null) it.testTag(testTag) else it }
    ) {
        Surface(
            color = if (isBangla) PureWhite.copy(alpha = 0.14f) else SoftBlueSurfaceTint,
            shape = RoundedCornerShape(50),
            border = BorderStroke(
                width = 1.dp,
                color = if (isBangla) PureWhite.copy(alpha = 0.55f) else SoftBlueGlowBorder.copy(alpha = 0.50f)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(
                    horizontal = if (compact) 9.dp else 11.dp,
                    vertical = if (compact) 4.dp else 5.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Translate,
                    contentDescription = null,
                    tint = if (isBangla) PureWhite else ElectricCyan,
                    modifier = Modifier.size(if (compact) 13.dp else 14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isBangla) PureWhite else ElectricCyan,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Self-contained block displaying English text with an independent "বাংলা অনুবাদ" / "English"
 * toggle right at the bottom-end of that text, translating ONLY this block when tapped.
 */
@Composable
fun TranslatableTextBlock(
    englishText: String,
    banglaText: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    color: Color = TextSecondary,
    textAlign: TextAlign = TextAlign.Start,
    centerToggle: Boolean = false,
    testTagPrefix: String? = null
) {
    var showBangla by remember(englishText) { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (centerToggle) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = if (showBangla) banglaText else englishText,
            style = style,
            color = color,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = if (centerToggle) Alignment.Center else Alignment.CenterEnd
        ) {
            BanglaTranslatePill(
                isBangla = showBangla,
                onToggle = { showBangla = !showBangla },
                testTag = testTagPrefix?.let { "${it}_bangla_toggle" }
            )
        }
    }
}
