package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AppNotification
import com.example.data.model.Category
import com.example.data.model.Game
import com.example.data.model.MembershipTier
import com.example.data.model.OwnerOrderMessage
import com.example.data.model.PointsEntry
import com.example.data.model.Product
import com.example.data.model.StoreConfig
import com.example.data.model.UserProfile
import com.example.ui.components.BanglaTranslatePill
import com.example.ui.components.MsrGlobalHeader
import com.example.ui.components.MsrLogoImage
import com.example.ui.components.MsrScrollableColumn
import com.example.ui.components.SectionBanglaTranslator
import com.example.ui.components.TranslatableTextBlock
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PlatinumAccent
import com.example.ui.theme.SilverAccent
import com.example.ui.theme.SubtlePurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.StoreCatalogData
import com.example.ui.viewmodel.StoreFormatters
import com.example.ui.viewmodel.UiState
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AccountScreen(
    firebaseUser: FirebaseUser,
    userProfile: UserProfile?,
    storeConfig: StoreConfig,
    catalog: StoreCatalogData?,
    pointsHistoryState: UiState<List<PointsEntry>>,
    unreadNotifications: Int,
    isVerifiedOwner: Boolean,
    onOpenOwnerDashboard: () -> Unit,
    onOpenNotifications: () -> Unit,
    onBuyFavorite: (Product, Game, Category?) -> Unit,
    onDeleteAccount: () -> Unit,
    onSignOut: () -> Unit,
    onCopyFeedback: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val points = userProfile?.points ?: 0L
    val spending = userProfile?.totalVerifiedSpending ?: 0L
    val tier = MembershipTier.computeTier(
        points,
        storeConfig.silverThreshold,
        storeConfig.goldThreshold,
        storeConfig.platinumThreshold
    )

    val (nextTierLabel, nextThreshold) = when (tier) {
        MembershipTier.STANDARD -> "SILVER" to storeConfig.silverThreshold
        MembershipTier.SILVER -> "GOLD" to storeConfig.goldThreshold
        MembershipTier.GOLD -> "PLATINUM" to storeConfig.platinumThreshold
        MembershipTier.PLATINUM -> "MAX PLATINUM" to storeConfig.platinumThreshold
    }
    val progress = if (nextThreshold <= 0L) 1f else (points.toFloat() / nextThreshold.toFloat()).coerceIn(0f, 1f)
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    MsrScrollableColumn(modifier = modifier) {
        // Profile & Google Account Card
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val photoUrl = userProfile?.photoUrl?.takeIf { it.isNotBlank() } ?: firebaseUser.photoUrl?.toString()
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .border(2.dp, ElectricCyan, CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userProfile?.displayName?.ifBlank { null } ?: firebaseUser.displayName ?: "MSR Gamer",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = firebaseUser.email ?: userProfile?.email ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TierPill(tier = tier)
                        if (isVerifiedOwner) {
                            Surface(
                                color = GoldAccent.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.border(1.dp, GoldAccent, RoundedCornerShape(12.dp))
                            ) {
                                Text(
                                    text = "VERIFIED OWNER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Owner Admin Dashboard Entry (ONLY visible to verified owner mdseamrahman2525@gmail.com)
        if (isVerifiedOwner) {
            Button(
                onClick = onOpenOwnerDashboard,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = PitchBlack),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 54.dp)
                    .testTag("account_open_owner_dashboard_button")
            ) {
                Icon(Icons.Filled.AdminPanelSettings, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Open Owner Admin Dashboard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Points, Total Spending & Membership Progress Card
        Surface(
            color = DarkSurfaceElevated,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
                .testTag("loyalty_membership_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Stars, contentDescription = null, tint = GoldAccent)
                    Spacer(Modifier.width(8.dp))
                    Text("Loyalty Points & Membership", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Current Points", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(
                            text = StoreFormatters.formatNumber(points),
                            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = MonoFontFamily),
                            color = GoldAccent
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Verified Spending", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(
                            text = StoreFormatters.formatBdt(spending),
                            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = MonoFontFamily),
                            color = ElectricCyan
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    color = GoldAccent,
                    trackColor = DarkSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                )

                var loyaltyBangla by remember(storeConfig.rewardDescription) { mutableStateOf(false) }
                val remaining = (nextThreshold - points).coerceAtLeast(0L)
                Text(
                    text = if (loyaltyBangla) {
                        if (tier == MembershipTier.PLATINUM) "আপনি PLATINUM — সর্বোচ্চ VIP লেভেলে পৌঁছে গেছেন!"
                        else "$nextTierLabel লেভেলে পৌঁছাতে আর $remaining পয়েন্ট প্রয়োজন (মোট ${StoreFormatters.formatNumber(nextThreshold)} pts)"
                    } else {
                        if (tier == MembershipTier.PLATINUM) "You have reached PLATINUM — Highest VIP Tier!"
                        else "$remaining more points to reach $nextTierLabel (${StoreFormatters.formatNumber(nextThreshold)} pts)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = if (loyaltyBangla) {
                        SectionBanglaTranslator.translateRewardDescription(storeConfig.rewardDescription, storeConfig)
                    } else {
                        storeConfig.rewardDescription
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    BanglaTranslatePill(
                        isBangla = loyaltyBangla,
                        onToggle = { loyaltyBangla = !loyaltyBangla },
                        testTag = "loyalty_bangla_toggle"
                    )
                }

                HorizontalDivider(color = DarkCardBorder)
                Text(
                    text = "Points History",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                when (pointsHistoryState) {
                    is UiState.Success -> {
                        val entries = pointsHistoryState.data
                        if (entries.isEmpty()) {
                            TranslatableTextBlock(
                                englishText = "No loyalty points earned yet. Complete verified orders to earn 1 point per ৳1.",
                                banglaText = "এখনো কোনো লয়্যালটি পয়েন্ট যোগ হয়নি। অর্ডার ভেরিফাই হয়ে Done হলে প্রতি ৳১-এ ১ পয়েন্ট পাবেন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                testTagPrefix = "points_empty"
                            )
                        } else {
                            entries.take(8).forEach { entry ->
                                val d = entry.createdAt?.toDate()?.let { dateFmt.format(it) } ?: ""
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(entry.reason, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                        if (d.isNotBlank()) {
                                            Text(d, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        }
                                    }
                                    Text(
                                        text = if (entry.pointsDelta >= 0) "+${entry.pointsDelta} pts" else "${entry.pointsDelta} pts",
                                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                                        color = if (entry.pointsDelta >= 0) SuccessGreen else ErrorRed
                                    )
                                }
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        // Saved Favorites
        val favIds = userProfile?.favoriteProductIds ?: emptyList()
        if (favIds.isNotEmpty() && catalog != null) {
            val favProducts = catalog.products.filter { favIds.contains(it.id) && it.active }
            if (favProducts.isNotEmpty()) {
                Surface(
                    color = DarkSurface.copy(alpha = 0.94f),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Favorite, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Favorite Packages", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        }
                        favProducts.forEach { fav ->
                            val game = catalog.games.firstOrNull { it.id == fav.gameId } ?: Game(id = fav.gameId, name = "Roblox")
                            val cat = catalog.categories.firstOrNull { it.id == fav.categoryId }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onBuyFavorite(fav, game, cat) }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(fav.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text(
                                    text = "${StoreFormatters.formatBdt(fav.priceBdt)} • Buy",
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                                    color = ElectricCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        // Account Settings & Legal Links
        Surface(
            color = DarkSurface.copy(alpha = 0.95f),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                AccountMenuItem(
                    icon = Icons.Filled.Notifications,
                    title = "Notifications",
                    subtitle = if (unreadNotifications > 0) "$unreadNotifications unread updates" else "View order & offer alerts",
                    onClick = onOpenNotifications
                )
                if (storeConfig.appDownloadUrl.isNotBlank()) {
                    HorizontalDivider(color = DarkCardBorder.copy(alpha = 0.5f))
                    AccountMenuItem(
                        icon = Icons.Filled.Share,
                        title = "Share / Download App Link",
                        subtitle = "Copy or share official MSR BD Game Top-Up link",
                        onClick = {
                            val link = storeConfig.appDownloadUrl.trim()
                            clipboard.setText(AnnotatedString(link))
                            onCopyFeedback("App link copied! Opening share options...")
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "MSR BD Game Top-Up — Official Roblox & Game Top-Up App: $link"
                                )
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share MSR BD Game Top-Up"))
                        }
                    )
                }
                HorizontalDivider(color = DarkCardBorder.copy(alpha = 0.5f))
                AccountMenuItem(
                    icon = Icons.Filled.Policy,
                    title = "Privacy Policy",
                    subtitle = "How your Google & order data is protected",
                    onClick = { showPrivacyDialog = true }
                )
                HorizontalDivider(color = DarkCardBorder.copy(alpha = 0.5f))
                AccountMenuItem(
                    icon = Icons.Filled.Description,
                    title = "Terms & Conditions",
                    subtitle = "Top-up delivery, bKash & loyalty rules",
                    onClick = { showTermsDialog = true }
                )
                HorizontalDivider(color = DarkCardBorder.copy(alpha = 0.5f))
                AccountMenuItem(
                    icon = Icons.Filled.Info,
                    title = "About MSR BD Game Top-Up",
                    subtitle = "Official brand info & version",
                    onClick = { showAboutDialog = true }
                )
                HorizontalDivider(color = DarkCardBorder.copy(alpha = 0.5f))
                AccountMenuItem(
                    icon = Icons.Filled.DeleteForever,
                    title = "Delete Account",
                    subtitle = "Permanently remove profile from device & Firebase",
                    tint = ErrorRed,
                    onClick = { showDeleteConfirm = true }
                )
            }
        }

        OutlinedButton(
            onClick = onSignOut,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 50.dp)
                .testTag("logout_button")
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = ErrorRed)
            Spacer(Modifier.width(8.dp))
            Text("Sign Out", color = ErrorRed, style = MaterialTheme.typography.titleSmall)
        }

        Spacer(modifier = Modifier.height(14.dp))
    }

    if (showPrivacyDialog) {
        InfoDialog(
            title = "Privacy Policy",
            banglaTitle = "গোপনীয়তা নীতি (Privacy Policy)",
            content = storeConfig.privacyPolicyText,
            banglaContent = SectionBanglaTranslator.translatePrivacyPolicy(storeConfig.privacyPolicyText),
            onDismiss = { showPrivacyDialog = false }
        )
    }
    if (showTermsDialog) {
        InfoDialog(
            title = "Terms & Conditions",
            banglaTitle = "শর্তাবলী (Terms & Conditions)",
            content = storeConfig.termsText,
            banglaContent = SectionBanglaTranslator.translateTerms(storeConfig.termsText),
            onDismiss = { showTermsDialog = false }
        )
    }
    if (showAboutDialog) {
        var aboutBangla by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = DarkSurface,
            title = {
                Text(
                    if (aboutBangla) "MSR BD Game Top-Up সম্পর্কে" else "About MSR BD Game Top-Up",
                    color = TextPrimary
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MsrLogoImage(size = 88.dp)
                    Text(
                        text = "MSR BD GAME TOP-UP",
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricCyan
                    )
                    Text(
                        text = if (aboutBangla) {
                            "অফিশিয়াল ডিজিটাল গেমিং টপ-আপ ও সাবস্ক্রিপশন প্ল্যাটফর্ম।\nঅফিশিয়াল bKash/Nagad ও সাপোর্ট: ${storeConfig.supportPhone}"
                        } else {
                            "Official digital gaming top-up & subscription platform.\nOfficial bKash & Support: ${storeConfig.supportPhone}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    BanglaTranslatePill(
                        isBangla = aboutBangla,
                        onToggle = { aboutBangla = !aboutBangla },
                        testTag = "about_dialog_bangla_toggle"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close", color = ElectricCyan) }
            }
        )
    }
    if (showDeleteConfirm) {
        var deleteBangla by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = DarkSurface,
            title = {
                Text(
                    if (deleteBangla) "অ্যাকাউন্ট মুছে ফেলবেন?" else "Delete Account?",
                    color = ErrorRed
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (deleteBangla) {
                            "এটি আপনার প্রোফাইল ডকুমেন্ট মুছে দেবে এবং আপনাকে সাইন আউট করবে। নোট: হিসাবরক্ষণের জন্য পূর্বের ভেরিফাইড অর্ডার রেকর্ড সংরক্ষিত থাকে।"
                        } else {
                            "This deletes your profile document and signs you out. Note: existing verified order records are preserved for accounting compliance."
                        },
                        color = TextSecondary
                    )
                    BanglaTranslatePill(
                        isBangla = deleteBangla,
                        onToggle = { deleteBangla = !deleteBangla },
                        testTag = "delete_dialog_bangla_toggle"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text(if (deleteBangla) "মুছে ফেলুন" else "Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(if (deleteBangla) "বাতিল" else "Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun NotificationsScreen(
    notificationsState: UiState<List<AppNotification>>,
    isVerifiedOwner: Boolean,
    ownerOrderMessagesState: UiState<List<OwnerOrderMessage>> = UiState.Success(emptyList()),
    onBack: () -> Unit,
    onMarkRead: (String) -> Unit,
    onMarkOrderMessageRead: (String) -> Unit = {},
    onOpenOwnerDashboard: () -> Unit,
    onOpenOwnerOrders: () -> Unit = onOpenOwnerDashboard
) {
    val dateFmt = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }

    Scaffold(
        containerColor = DeepNavyBg,
        topBar = {
            MsrGlobalHeader(
                pageTitle = if (isVerifiedOwner) "Inbox & Notifications" else "Notifications",
                showBackButton = true,
                onBackClick = onBack,
                isOwner = isVerifiedOwner,
                onOwnerDashboardClick = onOpenOwnerDashboard
            )
        }
    ) { innerPadding ->
        MsrScrollableColumn(modifier = Modifier.padding(innerPadding)) {
            if (isVerifiedOwner) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = GoldAccent)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Customer Order Messages",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(onClick = onOpenOwnerOrders) {
                        Text("Open All Orders", color = ElectricCyan)
                    }
                }

                when (ownerOrderMessagesState) {
                    is UiState.Success -> {
                        val orderMsgs = ownerOrderMessagesState.data
                        if (orderMsgs.isEmpty()) {
                            Surface(
                                color = DarkSurface,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                            ) {
                                Text(
                                    text = "No incoming customer order messages yet. Every order placed by any customer arrives here automatically.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
                            orderMsgs.forEach { msg ->
                                val d = msg.createdAt?.toDate()?.let { dateFmt.format(it) } ?: "Just now"
                                Surface(
                                    color = if (msg.read) DarkSurface else DarkSurfaceElevated,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            if (msg.read) DarkCardBorder else GoldAccent,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            if (!msg.read) onMarkOrderMessageRead(msg.id)
                                            onOpenOwnerOrders()
                                        }
                                        .testTag("owner_order_msg_${msg.id}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "New Order • ${msg.orderCode}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = MonoFontFamily),
                                                color = if (msg.read) ElectricCyan else GoldAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (!msg.read) {
                                                Text(
                                                    text = "NEW ORDER",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = SuccessGreen,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${msg.productName} • ${StoreFormatters.formatBdt(msg.totalAmountBdt)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Customer: ${msg.customerName} (${msg.customerEmail})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "${msg.targetAccountLabel}: ${msg.targetAccountInput}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${msg.paymentMethod} From: ${msg.paymentSenderNumber}  |  TrxID: ${msg.transactionId}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                                            color = ElectricCyan
                                        )
                                        Text(
                                            text = "$d • Tap to Verify & Fulfill",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is UiState.Error -> Text(ownerOrderMessagesState.message, color = ErrorRed)
                    is UiState.Loading -> Text("Loading incoming order messages...", color = TextSecondary)
                }

                HorizontalDivider(color = DarkCardBorder)
                Text(
                    text = "Store Updates & Alerts",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
            }

            when (notificationsState) {
                is UiState.Success -> {
                    val items = notificationsState.data
                    if (items.isEmpty()) {
                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                        ) {
                            TranslatableTextBlock(
                                englishText = "You're all caught up! Order updates, Exclusive Bonus Drops, and store announcements will appear here.",
                                banglaText = "কোনো নতুন নোটিফিকেশন নেই! অর্ডারের আপডেট, এক্সক্লুসিভ বোনাস ড্রপ এবং স্টোরের ঘোষণা এখানে দেখতে পাবেন।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(16.dp),
                                testTagPrefix = "notifications_empty"
                            )
                        }
                    } else {
                        items.forEach { notif ->
                            val d = notif.createdAt?.toDate()?.let { dateFmt.format(it) } ?: ""
                            Surface(
                                color = if (notif.read) DarkSurface.copy(alpha = 0.85f) else DarkSurfaceElevated,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (notif.read) DarkCardBorder else ElectricBlue,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { if (!notif.read) onMarkRead(notif.id) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(notif.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                        if (!notif.read) {
                                            Text("NEW", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                                        }
                                    }
                                    Text(notif.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    if (d.isNotBlank()) {
                                        Text(d, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> Text(notificationsState.message, color = ErrorRed)
                is UiState.Loading -> Text("Loading notifications...", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun TierPill(tier: MembershipTier) {
    val color = when (tier) {
        MembershipTier.PLATINUM -> PlatinumAccent
        MembershipTier.GOLD -> GoldAccent
        MembershipTier.SILVER -> SilverAccent
        MembershipTier.STANDARD -> ElectricCyan
    }
    Surface(
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
    ) {
        Text(
            text = "${tier.label} TIER",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun AccountMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: androidx.compose.ui.graphics.Color = ElectricCyan,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun InfoDialog(
    title: String,
    banglaTitle: String,
    content: String,
    banglaContent: String,
    onDismiss: () -> Unit
) {
    var showBangla by remember(content) { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(if (showBangla) banglaTitle else title, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (showBangla) banglaContent else content,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    BanglaTranslatePill(
                        isBangla = showBangla,
                        onToggle = { showBangla = !showBangla },
                        testTag = "info_dialog_bangla_toggle"
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK", color = ElectricCyan) }
        }
    )
}
