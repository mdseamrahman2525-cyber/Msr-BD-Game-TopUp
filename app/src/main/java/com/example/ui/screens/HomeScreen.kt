package com.example.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.InitialCatalog
import com.example.data.model.MembershipTier
import com.example.data.model.UserProfile
import com.example.ui.components.BanglaTranslatePill
import com.example.ui.components.MsrScrollableColumn
import com.example.ui.components.SectionBanglaTranslator
import com.example.ui.components.TranslatableTextBlock
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftBlueGlowBorder
import com.example.ui.theme.SoftBlueSurfaceTint
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.StoreCatalogData
import com.example.ui.viewmodel.StoreFormatters
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun HomeScreen(
    catalogState: UiState<StoreCatalogData>,
    selectedGameId: String?,
    userProfile: UserProfile?,
    isVerifiedOwner: Boolean,
    unreadOwnerOrderMessages: Int = 0,
    onSelectGame: (String) -> Unit,
    onOpenCategory: (gameId: String, categoryId: String) -> Unit,
    onOpenBonusOffers: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenOwnerDashboard: () -> Unit,
    onOpenOwnerOrders: () -> Unit = onOpenOwnerDashboard,
    modifier: Modifier = Modifier
) {
    when (catalogState) {
        is UiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PureWhite)
            }
        }
        is UiState.Error -> {
            MsrScrollableColumn(modifier = modifier) {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
                ) {
                    Text(
                        text = catalogState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        is UiState.Success -> {
            val catalog = catalogState.data
            val config = catalog.storeConfig

            if (config.maintenanceMode && !isVerifiedOwner) {
                MsrScrollableColumn(modifier = modifier) {
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, WarningAmber.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Build,
                                contentDescription = "Maintenance Mode",
                                tint = WarningAmber,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "Scheduled Maintenance",
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary
                            )
                            TranslatableTextBlock(
                                englishText = config.maintenanceMessage,
                                banglaText = SectionBanglaTranslator.translateMaintenance(config.maintenanceMessage),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                centerToggle = true,
                                testTagPrefix = "maintenance"
                            )
                        }
                    }
                }
                return
            }

            val activeGames = remember(catalog.games) {
                catalog.games.filter { it.active }.sortedBy { it.displayOrder }
                    .ifEmpty { InitialCatalog.defaultGames }
            }
            val activeCategories = remember(catalog.categories) {
                catalog.categories.filter { it.active }.sortedBy { it.displayOrder }
            }
            val activeProducts = remember(catalog.products) {
                catalog.products.filter { it.active }.sortedBy { it.displayOrder }
            }
            val activeOffers = remember(catalog.bonusOffers) { catalog.bonusOffers.filter { it.active } }

            // Slot #1 stays open by default unless customer selects another active game
            val currentGame = activeGames.firstOrNull { it.id == selectedGameId }
                ?: activeGames.first()

            val gameCategories = remember(activeCategories, activeProducts, currentGame.id) {
                val matched = activeCategories.filter { cat ->
                    cat.gameId == currentGame.id && (
                        cat.id == InitialCatalog.CAT_ROBUX_ID ||
                            cat.id == InitialCatalog.CAT_ROBLOX_PLUS_ID ||
                            activeProducts.any { it.categoryId == cat.id }
                        )
                }
                if (currentGame.id == InitialCatalog.GAME_ROBLOX_ID && matched.isEmpty()) {
                    listOf(
                        Category(
                            id = InitialCatalog.CAT_ROBUX_ID,
                            gameId = currentGame.id,
                            name = "Robux",
                            type = CategoryType.CURRENCY.name
                        ),
                        Category(
                            id = InitialCatalog.CAT_ROBLOX_PLUS_ID,
                            gameId = currentGame.id,
                            name = "Subscription",
                            type = CategoryType.SUBSCRIPTION.name
                        )
                    )
                } else {
                    matched
                }
            }

            MsrScrollableColumn(modifier = modifier) {
                if (isVerifiedOwner) {
                    val hasUnreadOrders = unreadOwnerOrderMessages > 0
                    Surface(
                        color = if (hasUnreadOrders) SuccessGreen.copy(alpha = 0.14f) else GoldAccent.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (hasUnreadOrders) SuccessGreen.copy(alpha = 0.65f) else GoldAccent.copy(alpha = 0.45f),
                                RoundedCornerShape(14.dp)
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = if (hasUnreadOrders) onOpenOwnerOrders else onOpenOwnerDashboard)
                            .testTag("home_owner_dashboard_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = null,
                                tint = if (hasUnreadOrders) SuccessGreen else GoldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (hasUnreadOrders) {
                                        "$unreadOwnerOrderMessages New Customer Order${if (unreadOwnerOrderMessages > 1) "s" else ""}!"
                                    } else {
                                        "Owner Admin Dashboard"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (hasUnreadOrders) SuccessGreen else GoldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (hasUnreadOrders) {
                                        "Tap to verify payment & mark orders Done"
                                    } else {
                                        "Add games, packages, numbers & manage orders"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = if (hasUnreadOrders) "ORDERS" else "OPEN",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (hasUnreadOrders) SuccessGreen else GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 1ST ELEMENT: STYLED CAPSULE SCROLLABLE TOP GAME SELECTOR BAR
                Surface(
                    color = SoftBlueSurfaceTint,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = SoftBlueGlowBorder.copy(alpha = 0.30f),
                            shape = RoundedCornerShape(50)
                        )
                        .testTag("home_game_selector_bar")
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(activeGames, key = { it.id }) { game ->
                            val isSelected = game.id == currentGame.id
                            val pillShape = RoundedCornerShape(50)
                            Box(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clip(pillShape)
                                    .background(
                                        brush = if (isSelected) {
                                            Brush.horizontalGradient(listOf(PureWhite, Color(0xFFE4F0FF)))
                                        } else {
                                            Brush.horizontalGradient(listOf(Color(0xFF14192A), Color(0xFF171E33)))
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) {
                                            SoftBlueGlowBorder.copy(alpha = 0.78f)
                                        } else {
                                            SoftBlueGlowBorder.copy(alpha = 0.25f)
                                        },
                                        shape = pillShape
                                    )
                                    .clickable { onSelectGame(game.id) }
                                    .padding(horizontal = 18.dp, vertical = 10.dp)
                                    .testTag("home_game_chip_${game.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.SportsEsports,
                                            contentDescription = null,
                                            tint = PureBlack,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(ElectricBlue)
                                        )
                                    }
                                    Text(
                                        text = game.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) PureBlack else TextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // CLEAN SLEEK TYPOGRAPHY HERO (No Roblox Logo Image)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF171F35), PureBlack)
                            )
                        )
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(22.dp))
                        .testTag("home_selected_game_hero")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = SoftBlueSurfaceTint,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.border(
                                1.dp,
                                SoftBlueGlowBorder.copy(alpha = 0.40f),
                                RoundedCornerShape(50)
                            )
                        ) {
                            Text(
                                text = "ACTIVE STORE",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricBlue,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        if (currentGame.id != InitialCatalog.GAME_ROBLOX_ID && currentGame.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(currentGame.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = currentGame.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth(0.60f)
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                        }

                        Text(
                            text = currentGame.name.uppercase(Locale.US),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 32.sp,
                                letterSpacing = 5.sp
                            ),
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = currentGame.description.ifBlank {
                                "Official Packages • bKash & Nagad"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // NUMBERED SERVICE OPTION CARDS (1. Robux, 2. Subscription, 3. ...)
                if (gameCategories.isEmpty()) {
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            text = "Packages for ${currentGame.name} will be added here soon.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    val rows = gameCategories.chunked(2)
                    rows.forEachIndexed { rowIndex, rowCats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowCats.forEachIndexed { colIndex, cat ->
                                val numberIndex = rowIndex * 2 + colIndex + 1
                                val catProducts = activeProducts.filter { it.categoryId == cat.id }
                                val minPrice = catProducts.minOfOrNull { it.priceBdt }
                                val drawableRes: Int? = when {
                                    cat.id == InitialCatalog.CAT_ROBUX_ID -> R.drawable.msr_robux_option
                                    cat.id == InitialCatalog.CAT_ROBLOX_PLUS_ID -> R.drawable.msr_subscription_option
                                    else -> null
                                }
                                val displayName = when (cat.id) {
                                    InitialCatalog.CAT_ROBUX_ID -> "Robux"
                                    InitialCatalog.CAT_ROBLOX_PLUS_ID -> "Subscription"
                                    else -> cat.name
                                }
                                val testTag = when (cat.id) {
                                    InitialCatalog.CAT_ROBUX_ID -> "home_option_robux"
                                    InitialCatalog.CAT_ROBLOX_PLUS_ID -> "home_option_subscription"
                                    else -> "home_option_cat_${cat.id}"
                                }

                                GameOptionGridCard(
                                    indexNumber = numberIndex,
                                    title = displayName,
                                    metaLabel = if (minPrice != null) {
                                        "From ${StoreFormatters.formatBdt(minPrice)}"
                                    } else {
                                        "${catProducts.size} Packages"
                                    },
                                    drawableRes = drawableRes,
                                    onClick = { onOpenCategory(currentGame.id, cat.id) },
                                    testTag = testTag,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowCats.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Store Announcement
                val latestAnnouncement = catalog.announcements.firstOrNull { it.active }
                val bannerText = latestAnnouncement?.message ?: config.announcementBanner
                if (bannerText.isNotBlank()) {
                    var announcementBangla by remember(bannerText) { mutableStateOf(false) }
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Campaign,
                                contentDescription = "Announcement",
                                tint = ElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (announcementBangla) {
                                    SectionBanglaTranslator.translateAnnouncement(bannerText)
                                } else {
                                    bannerText
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BanglaTranslatePill(
                                isBangla = announcementBangla,
                                onToggle = { announcementBangla = !announcementBangla },
                                compact = true,
                                testTag = "home_announcement_bangla_toggle"
                            )
                        }
                    }
                }

                // Customer Loyalty Quick Strip
                if (userProfile != null) {
                    val tier = MembershipTier.fromRaw(userProfile.membershipTier)
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onOpenAccount)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.Stars, contentDescription = null, tint = GoldAccent)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${StoreFormatters.formatNumber(userProfile.points)} Loyalty Points",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${tier.label} • Spend: ${StoreFormatters.formatBdt(userProfile.totalVerifiedSpending)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Text(
                                text = "Account",
                                style = MaterialTheme.typography.labelLarge,
                                color = PureWhite
                            )
                        }
                    }
                }

                // ALWAYS-VISIBLE BOTTOM ENTRY CARD -> OPENS DEDICATED BONUS OFFERS SCREEN
                MonthlyBonusDropsEntryCard(
                    liveOfferCount = activeOffers.size,
                    onClick = onOpenBonusOffers
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun MonthlyBonusDropsEntryCard(
    liveOfferCount: Int,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                Brush.verticalGradient(
                    listOf(SoftBlueSurfaceTint, DarkSurface)
                )
            )
            .border(
                width = 1.2.dp,
                color = SoftBlueGlowBorder.copy(alpha = 0.50f),
                shape = cardShape
            )
            .clickable(onClick = onClick)
            .testTag("home_bonus_offers_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = GoldAccent.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.border(
                            1.dp,
                            GoldAccent.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalOffer,
                            contentDescription = "Monthly Bonus Drops",
                            tint = GoldAccent,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "MONTHLY BONUS DROPS",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "3x Every Month • 10th, 20th & 30th",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    color = if (liveOfferCount > 0) SuccessGreen.copy(alpha = 0.16f) else SoftBlueSurfaceTint,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = if (liveOfferCount > 0) SuccessGreen.copy(alpha = 0.5f) else DarkCardBorder,
                        shape = RoundedCornerShape(50)
                    )
                ) {
                    Text(
                        text = if (liveOfferCount > 0) "$liveOfferCount Live Deals" else "3x Drops",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (liveOfferCount > 0) SuccessGreen else ElectricCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            BonusDropCountdownBanner()

            var bonusIntroBangla by remember { mutableStateOf(false) }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (bonusIntroBangla) {
                        "প্রতি মাসের ১০, ২০ এবং ৩০ তারিখে ওনার-ঘোষিত বিশেষ বোনাস ডিসকাউন্ট দেখতে ও নিতে ট্যাপ করুন।"
                    } else {
                        "Tap to view & claim exclusive owner-released discounts available 3 times each month."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    BanglaTranslatePill(
                        isBangla = bonusIntroBangla,
                        onToggle = { bonusIntroBangla = !bonusIntroBangla },
                        testTag = "home_bonus_bangla_toggle"
                    )
                }
            }

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("home_open_bonus_offers_button")
            ) {
                Text(
                    text = "Open Bonus Offers",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun GameOptionGridCard(
    indexNumber: Int,
    title: String,
    metaLabel: String,
    @DrawableRes drawableRes: Int?,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 160.dp)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .background(PureBlack)
            ) {
                if (drawableRes != null) {
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SportsEsports,
                            contentDescription = title,
                            tint = PureWhite,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "$indexNumber.",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = PureBlack,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = metaLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan,
                        maxLines = 1
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open $title",
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun BonusDropCountdownBanner(
    modifier: Modifier = Modifier
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            nowMillis = System.currentTimeMillis()
        }
    }
    val countdown = StoreFormatters.computeNextBonusDrop(nowMillis)

    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (countdown.isDropDayToday) {
                        "DROP DAY LIVE (${countdown.nextDateLabel})"
                    } else {
                        "Next Drop: ${countdown.nextDateLabel}"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
            }
            Text(
                text = String.format(
                    Locale.US,
                    "%02dd %02dh %02dm %02ds",
                    countdown.days,
                    countdown.hours,
                    countdown.minutes,
                    countdown.seconds
                ),
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                color = GoldAccent
            )
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
