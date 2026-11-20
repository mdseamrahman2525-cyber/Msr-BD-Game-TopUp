package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Category
import com.example.data.model.Game
import com.example.data.model.InitialCatalog
import com.example.data.model.Product
import com.example.ui.components.BanglaTranslatePill
import com.example.ui.components.MsrGlobalHeader
import com.example.ui.components.MsrScrollableColumn
import com.example.ui.components.ProductVisualBadge
import com.example.ui.components.SectionBanglaTranslator
import com.example.ui.components.TranslatableTextBlock
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftBlueGlowBorder
import com.example.ui.theme.SoftBlueSurfaceTint
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.StoreCatalogData
import com.example.ui.viewmodel.StoreFormatters
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun BonusOffersScreen(
    catalogState: UiState<StoreCatalogData>,
    isVerifiedOwner: Boolean,
    unreadNotifications: Int,
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenOwnerDashboard: () -> Unit,
    onBuyOffer: (Product, Game, Category?, Long?, String?) -> Unit
) {
    Scaffold(
        containerColor = DeepNavyBg,
        topBar = {
            MsrGlobalHeader(
                pageTitle = "Monthly Bonus Drops",
                showBackButton = true,
                onBackClick = onBack,
                unreadNotificationCount = unreadNotifications,
                onNotificationsClick = onOpenNotifications,
                isOwner = isVerifiedOwner,
                onOwnerDashboardClick = onOpenOwnerDashboard
            )
        }
    ) { innerPadding ->
        when (catalogState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PureWhite)
                }
            }
            is UiState.Error -> {
                MsrScrollableColumn(modifier = Modifier.padding(innerPadding)) {
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
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
                val offers = remember(catalog.bonusOffers) {
                    catalog.bonusOffers.filter { it.active }
                }
                val activeProducts = remember(catalog.products) {
                    catalog.products.filter { it.active }
                }
                val activeGames = remember(catalog.games) {
                    catalog.games.filter { it.active }.ifEmpty { InitialCatalog.defaultGames }
                }
                val offerCheckMillis = remember(offers) { System.currentTimeMillis() }
                val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
                val nextDrop = remember(offerCheckMillis) {
                    StoreFormatters.computeNextBonusDrop(offerCheckMillis)
                }

                MsrScrollableColumn(modifier = Modifier.padding(innerPadding)) {
                    // Schedule & Countdown Hero Card
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = SoftBlueGlowBorder.copy(alpha = 0.40f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .testTag("bonus_offers_header_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = GoldAccent)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "3x Monthly Extra Bonus Drops",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "10th • 20th • 30th of every month",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ElectricCyan
                                    )
                                }
                            }

                            BonusDropCountdownBanner()

                            TranslatableTextBlock(
                                englishText = "Every month on the 10th, 20th & 30th, MSR releases limited-time extra bonus discounts. Tap Claim Offer below to pay via bKash or Nagad.",
                                banglaText = "প্রতি মাসের ১০, ২০ এবং ৩০ তারিখে MSR সীমিত সময়ের জন্য অতিরিক্ত বোনাস ডিসকাউন্ট প্রকাশ করে। bKash বা Nagad দিয়ে কিনতে নিচের Claim Offer-এ ট্যাপ করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                testTagPrefix = "bonus_header"
                            )
                        }
                    }

                    if (offers.isEmpty()) {
                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
                                .testTag("bonus_offers_empty_state")
                        ) {
                            var emptyBonusBangla by remember { mutableStateOf(false) }
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalOffer,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    text = if (emptyBonusBangla) "এই মুহূর্তে কোনো সক্রিয় বোনাস ড্রপ নেই" else "No Active Bonus Drop Right Now",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (emptyBonusBangla) {
                                        "পরবর্তী ড্রপ খুলবে ${nextDrop.nextDateLabel} তারিখে — ১০, ২০ বা ৩০ তারিখে আবার এসে ওনার-প্রকাশিত বোনাস ডিসকাউন্ট দেখে নিন!"
                                    } else {
                                        "Next Drop opens on ${nextDrop.nextDateLabel} — check back on the 10th, 20th, or 30th for owner-published extra bonus discounts!"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                BanglaTranslatePill(
                                    isBangla = emptyBonusBangla,
                                    onToggle = { emptyBonusBangla = !emptyBonusBangla },
                                    testTag = "bonus_empty_bangla_toggle"
                                )
                            }
                        }
                    } else {
                        offers.forEach { offer ->
                            val startMs = offer.startDate?.toDate()?.time ?: 0L
                            val endMs = offer.endDate?.toDate()?.time ?: Long.MAX_VALUE
                            val isExpired = offerCheckMillis > endMs
                            val isLive = offer.active && offerCheckMillis in startMs..endMs
                            val linkedProduct = activeProducts.firstOrNull { it.id == offer.productId }
                                ?: Product(
                                    id = offer.productId.ifBlank { offer.id },
                                    gameId = activeGames.firstOrNull()?.id ?: InitialCatalog.GAME_ROBLOX_ID,
                                    name = offer.productName,
                                    quantityLabel = offer.productName,
                                    priceBdt = offer.discountedPriceBdt
                                )
                            val linkedGame = activeGames.firstOrNull { it.id == linkedProduct.gameId }
                                ?: activeGames.firstOrNull()
                                ?: Game(id = InitialCatalog.GAME_ROBLOX_ID, name = "Roblox")
                            val linkedCategory = catalog.categories.firstOrNull { it.id == linkedProduct.categoryId }
                            val savingsBdt = (offer.originalPriceBdt - offer.discountedPriceBdt).coerceAtLeast(0L)

                            Surface(
                                color = DarkSurfaceElevated,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = SoftBlueGlowBorder.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .testTag("bonus_offer_card_${offer.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ProductVisualBadge(
                                            imageUrl = offer.imageUrl,
                                            categoryType = linkedProduct.categoryType,
                                            size = 52.dp
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = offer.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = offer.productName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                        Surface(
                                            color = if (isLive) SuccessGreen.copy(alpha = 0.16f) else SoftBlueSurfaceTint,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.border(
                                                1.dp,
                                                if (isLive) SuccessGreen.copy(alpha = 0.5f) else DarkCardBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                        ) {
                                            Text(
                                                text = if (isExpired) "EXPIRED" else if (isLive) "ACTIVE" else "SCHEDULED",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isLive) SuccessGreen else ElectricCyan,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    if (offer.description.isNotBlank()) {
                                        TranslatableTextBlock(
                                            englishText = offer.description,
                                            banglaText = SectionBanglaTranslator.translateDynamicEnglish(offer.description),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            testTagPrefix = "bonus_offer_${offer.id}"
                                        )
                                    }

                                    val startLabel = offer.startDate?.toDate()?.let { dateFmt.format(it) } ?: "Drop ${offer.dropDayOfMonth}th"
                                    val endLabel = offer.endDate?.toDate()?.let { dateFmt.format(it) } ?: "Limited"
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Drop Day: ${offer.dropDayOfMonth}th • $startLabel - $endLabel",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                        if (savingsBdt > 0L) {
                                            Surface(
                                                color = GoldAccent.copy(alpha = 0.16f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Stars,
                                                        contentDescription = null,
                                                        tint = GoldAccent,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(Modifier.width(3.dp))
                                                    Text(
                                                        text = "Save ${StoreFormatters.formatBdt(savingsBdt)}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = GoldAccent,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = StoreFormatters.formatBdt(offer.discountedPriceBdt),
                                                style = MaterialTheme.typography.titleLarge.copy(fontFamily = MonoFontFamily),
                                                color = PureWhite,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = StoreFormatters.formatBdt(offer.originalPriceBdt),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = MonoFontFamily,
                                                    textDecoration = TextDecoration.LineThrough
                                                ),
                                                color = TextMuted
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                onBuyOffer(
                                                    linkedProduct,
                                                    linkedGame,
                                                    linkedCategory,
                                                    offer.discountedPriceBdt,
                                                    offer.title
                                                )
                                            },
                                            enabled = !isExpired && offer.active,
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PureWhite,
                                                contentColor = PureBlack,
                                                disabledContainerColor = DarkSurface,
                                                disabledContentColor = TextMuted
                                            ),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                            modifier = Modifier
                                                .defaultMinSize(minHeight = 48.dp)
                                                .testTag("claim_bonus_offer_${offer.id}")
                                        ) {
                                            Text(
                                                text = "Claim Offer",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
