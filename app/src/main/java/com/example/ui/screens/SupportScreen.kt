package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.FaqItem
import com.example.data.model.StoreConfig
import com.example.ui.components.BanglaTranslatePill
import com.example.ui.components.MsrScrollableColumn
import com.example.ui.components.SectionBanglaTranslator
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private val WhatsAppGreen = Color(0xFF25D366)

@Composable
fun SupportScreen(
    storeConfig: StoreConfig,
    faqs: List<FaqItem>,
    onGoToOrders: () -> Unit,
    onCopyFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    MsrScrollableColumn(modifier = modifier) {
        // Customer Service Info Card
        var supportDeskBangla by remember(storeConfig.supportInfo) { mutableStateOf(false) }
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.HeadsetMic, contentDescription = null, tint = PureWhite)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (supportDeskBangla) "কাস্টমার সাপোর্ট ডেস্ক" else "Customer Support Desk",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }
                    BanglaTranslatePill(
                        isBangla = supportDeskBangla,
                        onToggle = { supportDeskBangla = !supportDeskBangla },
                        compact = true,
                        testTag = "support_desk_bangla_toggle"
                    )
                }
                Text(
                    text = if (supportDeskBangla) {
                        SectionBanglaTranslator.translateSupportInfo(storeConfig.supportInfo, storeConfig.supportPhone)
                    } else {
                        storeConfig.supportInfo
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Official Support & bKash Number",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = storeConfig.supportPhone,
                            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = MonoFontFamily),
                            color = ElectricCyan
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    clipboard.setText(AnnotatedString(storeConfig.supportPhone))
                                    onCopyFeedback("Copied support number: ${storeConfig.supportPhone}")
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Copy Number", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${storeConfig.supportPhone}"))
                                    context.startActivity(dialIntent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                            ) {
                                Icon(Icons.Filled.Call, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Call", color = ElectricCyan)
                            }
                        }
                    }
                }
            }
        }

        // Official WhatsApp Community Card
        var whatsappCardBangla by remember { mutableStateOf(false) }
        Surface(
            color = DarkSurfaceElevated.copy(alpha = 0.96f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, WhatsAppGreen.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .clickable {
                    runCatching { uriHandler.openUri(storeConfig.whatsappCommunityUrl) }
                }
                .testTag("whatsapp_community_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Groups, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (whatsappCardBangla) "অফিশিয়াল হোয়াটসঅ্যাপ কমিউনিটি" else "Official WhatsApp Community",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (whatsappCardBangla) {
                                "বোনাস ড্রপ অ্যালার্ট, দ্রুত অর্ডার সাপোর্ট ও আপডেট পান"
                            } else {
                                "Get Bonus Drop alerts, fast order support & updates"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    BanglaTranslatePill(
                        isBangla = whatsappCardBangla,
                        onToggle = { whatsappCardBangla = !whatsappCardBangla },
                        compact = true,
                        testTag = "whatsapp_card_bangla_toggle"
                    )
                }
                Button(
                    onClick = {
                        runCatching { uriHandler.openUri(storeConfig.whatsappCommunityUrl) }
                            .onFailure { onCopyFeedback("Could not open WhatsApp link") }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhatsAppGreen,
                        contentColor = PitchBlack
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("join_whatsapp_community_button")
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Join WhatsApp Community", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = {
                        val digits = storeConfig.supportPhone.filter { it.isDigit() }
                        val intl = if (digits.startsWith("0")) "88$digits" else digits
                        runCatching { uriHandler.openUri("https://wa.me/$intl") }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Chat on WhatsApp (${storeConfig.supportPhone})", color = SuccessGreen)
                }
            }
        }

        // Order Assistance Shortcut (Directly below Direct WhatsApp Chat)
        var orderHelpBangla by remember { mutableStateOf(false) }
        Surface(
            color = DarkSurface.copy(alpha = 0.94f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onGoToOrders)
                .testTag("order_help_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = ElectricCyan)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (orderHelpBangla) "অর্ডার নিয়ে সাহায্য প্রয়োজন?" else "Need Help With an Order?",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                            Text(
                                text = if (orderHelpBangla) {
                                    "সাপোর্টে মেসেজ দেওয়ার আগে My Orders থেকে আপনার Order ID কপি করে নিন।"
                                } else {
                                    "Copy your Order ID from My Orders before messaging support"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (orderHelpBangla) "অর্ডার" else "Orders",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BanglaTranslatePill(
                        isBangla = orderHelpBangla,
                        onToggle = { orderHelpBangla = !orderHelpBangla },
                        testTag = "order_help_bangla_toggle"
                    )
                }
            }
        }

        // Share & Download App Card
        val downloadLink = storeConfig.appDownloadUrl.trim()
        if (downloadLink.isNotBlank()) {
            var shareCardBangla by remember { mutableStateOf(false) }
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
                    .testTag("share_download_app_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Share, contentDescription = null, tint = ElectricCyan)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (shareCardBangla) "অ্যাপ ডাউনলোড ও শেয়ার করুন" else "Download & Share App",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (shareCardBangla) {
                                    "বন্ধুদের সাথে অফিশিয়াল MSR BD Game Top-Up অ্যাপ লিংক শেয়ার করুন"
                                } else {
                                    "Share official MSR BD Game Top-Up app link with friends"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        BanglaTranslatePill(
                            isBangla = shareCardBangla,
                            onToggle = { shareCardBangla = !shareCardBangla },
                            compact = true,
                            testTag = "share_card_bangla_toggle"
                        )
                    }

                    Surface(
                        color = DarkSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = downloadLink,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonoFontFamily),
                            color = ElectricCyan,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboard.setText(AnnotatedString(downloadLink))
                                onCopyFeedback("App link copied!")
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite,
                                contentColor = PureBlack
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Copy Link", fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "MSR BD Game Top-Up — Official Roblox & Game Top-Up App: $downloadLink"
                                    )
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share MSR BD Game Top-Up"))
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share", color = ElectricCyan, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                runCatching { uriHandler.openUri(downloadLink) }
                                    .onFailure { onCopyFeedback("Could not open app link") }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = "Open Link", tint = ElectricCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // FAQ Accordion
        SectionTitle(title = "Frequently Asked Questions", subtitle = "Quick answers • Tap বাংলা on any question")
        faqs.filter { it.active }.forEach { faq ->
            var expanded by remember(faq.id) { mutableStateOf(false) }
            var faqBangla by remember(faq.id) { mutableStateOf(false) }
            val shownQuestion = if (faqBangla) {
                SectionBanglaTranslator.translateFaqQuestion(faq.id, faq.question)
            } else {
                faq.question
            }
            val shownAnswer = if (faqBangla) {
                SectionBanglaTranslator.translateFaqAnswer(faq.id, faq.answer)
            } else {
                faq.answer
            }
            Surface(
                color = DarkSurface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { expanded = !expanded }
                    .testTag("faq_card_${faq.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = shownQuestion,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        BanglaTranslatePill(
                            isBangla = faqBangla,
                            onToggle = { faqBangla = !faqBangla },
                            compact = true,
                            testTag = "faq_bangla_toggle_${faq.id}"
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = ElectricCyan
                        )
                    }
                    AnimatedVisibility(visible = expanded) {
                        Text(
                            text = shownAnswer,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
