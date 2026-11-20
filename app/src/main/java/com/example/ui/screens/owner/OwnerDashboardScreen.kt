package com.example.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Order
import com.example.data.model.UserProfile
import com.example.ui.components.MsrGlobalHeader
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.OwnerViewModel
import com.example.ui.viewmodel.StoreCatalogData
import com.example.ui.viewmodel.UiState

enum class OwnerTab(val label: String, val icon: ImageVector) {
    OVERVIEW("Overview & Settings", Icons.Filled.Analytics),
    ORDERS("Orders", Icons.Filled.ReceiptLong),
    GAMES("Games & Cats", Icons.Filled.Category),
    PRODUCTS("Products & Subs", Icons.Filled.Inventory),
    OFFERS("Offers & FAQs", Icons.Filled.LocalOffer),
    CUSTOMERS("Customers", Icons.Filled.People)
}

@Composable
fun OwnerDashboardScreen(
    ownerViewModel: OwnerViewModel,
    catalogState: UiState<StoreCatalogData>,
    isVerifiedOwner: Boolean,
    initialTab: OwnerTab = OwnerTab.OVERVIEW,
    onBack: () -> Unit
) {
    if (!isVerifiedOwner) {
        Scaffold(
            containerColor = DeepNavyBg,
            topBar = { MsrGlobalHeader(pageTitle = "Unauthorized", showBackButton = true, onBackClick = onBack) }
        ) { inner ->
            Box(Modifier.fillMaxSize().padding(inner).padding(24.dp)) {
                Text("Access restricted to verified application owner.", color = TextPrimary)
            }
        }
        return
    }

    var activeTab by remember(initialTab) { mutableStateOf(initialTab) }
    val allOrdersState by ownerViewModel.allOrdersState.collectAsStateWithLifecycle()
    val allCustomersState by ownerViewModel.allCustomersState.collectAsStateWithLifecycle()
    val isBusy by ownerViewModel.isBusy.collectAsStateWithLifecycle()
    val feedback by ownerViewModel.adminFeedback.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedback) {
        val msg = feedback
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            ownerViewModel.clearFeedback()
        }
    }

    val catalog = (catalogState as? UiState.Success)?.data ?: StoreCatalogData()
    val orders: List<Order> = (allOrdersState as? UiState.Success)?.data ?: emptyList()
    val customers: List<UserProfile> = (allCustomersState as? UiState.Success)?.data ?: emptyList()
    val pendingOrderCount = remember(orders) {
        orders.count { it.status == "PENDING" || it.status == "PAYMENT_VERIFICATION" }
    }

    Scaffold(
        containerColor = DeepNavyBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                MsrGlobalHeader(
                    pageTitle = "Owner Admin Dashboard",
                    showBackButton = true,
                    onBackClick = onBack,
                    isOwner = true
                )
                if (isBusy) {
                    LinearProgressIndicator(
                        color = GoldAccent,
                        trackColor = DarkSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                ) {
                    items(OwnerTab.entries) { tab ->
                        val chipLabel = if (tab == OwnerTab.ORDERS && pendingOrderCount > 0) {
                            "${tab.label} ($pendingOrderCount)"
                        } else {
                            tab.label
                        }
                        FilterChip(
                            selected = activeTab == tab,
                            onClick = { activeTab = tab },
                            label = { Text(chipLabel) },
                            leadingIcon = { Icon(tab.icon, contentDescription = null) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = PitchBlack,
                                selectedLeadingIconColor = PitchBlack,
                                containerColor = DeepNavyBg,
                                labelColor = TextSecondary,
                                iconColor = ElectricCyan
                            ),
                            modifier = Modifier.testTag("owner_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                OwnerTab.OVERVIEW -> OwnerOverviewAndSettingsTab(
                    ownerViewModel = ownerViewModel,
                    catalog = catalog,
                    orders = orders,
                    customers = customers
                )
                OwnerTab.ORDERS -> OwnerOrdersTab(
                    ownerViewModel = ownerViewModel,
                    allOrdersState = allOrdersState,
                    storeConfig = catalog.storeConfig
                )
                OwnerTab.GAMES -> OwnerGamesAndCategoriesTab(
                    ownerViewModel = ownerViewModel,
                    games = catalog.games,
                    categories = catalog.categories
                )
                OwnerTab.PRODUCTS -> OwnerProductsTab(
                    ownerViewModel = ownerViewModel,
                    games = catalog.games,
                    categories = catalog.categories,
                    products = catalog.products
                )
                OwnerTab.OFFERS -> OwnerOffersAndFaqsTab(
                    ownerViewModel = ownerViewModel,
                    offers = catalog.bonusOffers,
                    products = catalog.products,
                    faqs = catalog.faqs
                )
                OwnerTab.CUSTOMERS -> OwnerCustomersTab(
                    ownerViewModel = ownerViewModel,
                    allCustomersState = allCustomersState,
                    allOrders = orders,
                    storeConfig = catalog.storeConfig
                )
            }
        }
    }
}
