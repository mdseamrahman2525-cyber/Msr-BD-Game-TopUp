package com.example.ui.screens.owner

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.BonusOffer
import com.example.data.model.FaqItem
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.UserProfile
import com.example.ui.components.MsrScrollableColumn
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.OwnerViewModel
import com.example.ui.viewmodel.StoreCatalogData
import com.example.ui.viewmodel.StoreFormatters
import com.google.firebase.Timestamp
import java.util.Date

@Composable
fun OwnerOverviewAndSettingsTab(
    ownerViewModel: OwnerViewModel,
    catalog: StoreCatalogData,
    orders: List<Order>,
    customers: List<UserProfile>
) {
    val analytics = remember(orders) { ownerViewModel.computeSalesAnalytics(orders) }
    val cfg = catalog.storeConfig

    var bkashNumber by remember(cfg) { mutableStateOf(cfg.bkashNumber) }
    var nagadNumber by remember(cfg) { mutableStateOf(cfg.nagadNumber) }
    var paymentInstructions by remember(cfg) { mutableStateOf(cfg.paymentInstructions) }
    var supportPhone by remember(cfg) { mutableStateOf(cfg.supportPhone) }
    var whatsappUrl by remember(cfg) { mutableStateOf(cfg.whatsappCommunityUrl) }
    var appDownloadUrl by remember(cfg) { mutableStateOf(cfg.appDownloadUrl) }
    var supportInfo by remember(cfg) { mutableStateOf(cfg.supportInfo) }
    var maintenanceMode by remember(cfg) { mutableStateOf(cfg.maintenanceMode) }
    var maintenanceMessage by remember(cfg) { mutableStateOf(cfg.maintenanceMessage) }
    var announcementBanner by remember(cfg) { mutableStateOf(cfg.announcementBanner) }
    var silverText by remember(cfg) { mutableStateOf(cfg.silverThreshold.toString()) }
    var goldText by remember(cfg) { mutableStateOf(cfg.goldThreshold.toString()) }
    var platinumText by remember(cfg) { mutableStateOf(cfg.platinumThreshold.toString()) }
    var rewardDesc by remember(cfg) { mutableStateOf(cfg.rewardDescription) }

    var announcementTitle by remember { mutableStateOf("") }
    var announcementBody by remember { mutableStateOf("") }

    MsrScrollableColumn {
        // Sales Analytics Card
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Sales & Performance Analytics", style = MaterialTheme.typography.titleLarge, color = GoldAccent)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricBox("Verified Revenue", StoreFormatters.formatBdt(analytics.totalRevenueBdt), ElectricCyan)
                    MetricBox("Customers", "${customers.size}", GoldAccent)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricBox("Completed", "${analytics.completedOrders}", SuccessGreen)
                    MetricBox("Active Queue", "${analytics.pendingOrders}", ElectricBlue)
                    MetricBox("Rejected", "${analytics.rejectedOrders}", ErrorRed)
                }
                if (analytics.topProducts.isNotEmpty()) {
                    HorizontalDivider(color = DarkCardBorder)
                    Text("Top Completed Packages", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    analytics.topProducts.forEach { (name, count) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("$count sold", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                        }
                    }
                }
            }
        }

        // Publish Customer Announcement
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Send Customer Announcement", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                OutlinedTextField(
                    value = announcementTitle,
                    onValueChange = { announcementTitle = it },
                    label = { Text("Announcement Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = announcementBody,
                    onValueChange = { announcementBody = it },
                    label = { Text("Announcement Message") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (announcementTitle.isNotBlank() && announcementBody.isNotBlank()) {
                            ownerViewModel.publishAnnouncement(
                                announcementTitle,
                                announcementBody,
                                customers.map { it.userId }
                            )
                            announcementTitle = ""
                            announcementBody = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = PitchBlack),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Publish Announcement & Notify All")
                }
            }
        }

        // Store Config (Payment, Support, Loyalty Thresholds, Maintenance Mode)
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Payment, Support & App Settings", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Maintenance Mode", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("Temporarily pause customer purchases", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(checked = maintenanceMode, onCheckedChange = { maintenanceMode = it })
                }

                OutlinedTextField(value = maintenanceMessage, onValueChange = { maintenanceMessage = it }, label = { Text("Maintenance Message") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = announcementBanner, onValueChange = { announcementBanner = it }, label = { Text("Home Banner Text") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bkashNumber, onValueChange = { bkashNumber = it }, label = { Text("bKash Payment Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = nagadNumber, onValueChange = { nagadNumber = it }, label = { Text("Nagad Payment Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = paymentInstructions, onValueChange = { paymentInstructions = it }, label = { Text("Payment Instructions") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = supportPhone, onValueChange = { supportPhone = it }, label = { Text("Support Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = whatsappUrl, onValueChange = { whatsappUrl = it }, label = { Text("WhatsApp Community URL") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = appDownloadUrl, onValueChange = { appDownloadUrl = it }, label = { Text("App Download / Share URL") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = supportInfo, onValueChange = { supportInfo = it }, label = { Text("Support Desk Info") }, modifier = Modifier.fillMaxWidth())

                Text("Membership Tier Thresholds (Points)", style = MaterialTheme.typography.titleSmall, color = GoldAccent)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = silverText,
                        onValueChange = { silverText = it },
                        label = { Text("Silver") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = goldText,
                        onValueChange = { goldText = it },
                        label = { Text("Gold") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = platinumText,
                        onValueChange = { platinumText = it },
                        label = { Text("Platinum") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(value = rewardDesc, onValueChange = { rewardDesc = it }, label = { Text("Loyalty Rewards Description") }, modifier = Modifier.fillMaxWidth())

                Button(
                    onClick = {
                        ownerViewModel.saveStoreConfig(
                            cfg.copy(
                                bkashNumber = bkashNumber,
                                nagadNumber = nagadNumber,
                                paymentInstructions = paymentInstructions,
                                supportPhone = supportPhone,
                                whatsappCommunityUrl = whatsappUrl,
                                appDownloadUrl = appDownloadUrl.trim(),
                                supportInfo = supportInfo,
                                maintenanceMode = maintenanceMode,
                                maintenanceMessage = maintenanceMessage,
                                announcementBanner = announcementBanner,
                                silverThreshold = silverText.toLongOrNull() ?: cfg.silverThreshold,
                                goldThreshold = goldText.toLongOrNull() ?: cfg.goldThreshold,
                                platinumThreshold = platinumText.toLongOrNull() ?: cfg.platinumThreshold,
                                rewardDescription = rewardDesc
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = PitchBlack),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 50.dp)
                        .testTag("save_store_config_button")
                ) {
                    Text("Save All Settings")
                }

                OutlinedButton(
                    onClick = { ownerViewModel.reseedDefaultCatalog() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Restore Official Initial Roblox Catalog", color = ElectricCyan)
                }
            }
        }
    }
}

@Composable
fun OwnerOffersAndFaqsTab(
    ownerViewModel: OwnerViewModel,
    offers: List<BonusOffer>,
    products: List<Product>,
    faqs: List<FaqItem>
) {
    var editingOffer by remember { mutableStateOf<BonusOffer?>(null) }
    var editingFaq by remember { mutableStateOf<FaqItem?>(null) }

    MsrScrollableColumn {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Exclusive Bonus Drops", style = MaterialTheme.typography.titleLarge, color = GoldAccent)
            Button(
                onClick = {
                    val firstProd = products.firstOrNull()
                    editingOffer = BonusOffer(
                        productId = firstProd?.id ?: "",
                        productName = firstProd?.name ?: "100 Robux",
                        originalPriceBdt = firstProd?.priceBdt ?: 220L,
                        discountedPriceBdt = ((firstProd?.priceBdt ?: 220L) - 20L).coerceAtLeast(1L),
                        dropDayOfMonth = 10L
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = PitchBlack),
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add Drop")
            }
        }

        if (offers.isEmpty()) {
            Text("No bonus offers created yet. Tap Add Drop to publish a 10th, 20th, or 30th Bonus Drop offer.", color = TextSecondary)
        } else {
            offers.forEach { offer ->
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(offer.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text(
                                text = "${offer.productName} • ${StoreFormatters.formatBdt(offer.discountedPriceBdt)} (was ${StoreFormatters.formatBdt(offer.originalPriceBdt)}) • ${offer.dropDayOfMonth}th",
                                style = MaterialTheme.typography.bodySmall,
                                color = GoldAccent
                            )
                        }
                        Switch(
                            checked = offer.active,
                            onCheckedChange = { ownerViewModel.saveBonusOffer(offer.copy(active = it)) }
                        )
                        IconButton(onClick = { editingOffer = offer }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit offer", tint = ElectricCyan)
                        }
                        IconButton(onClick = { ownerViewModel.deleteBonusOffer(offer.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete offer", tint = ErrorRed)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("FAQ Management", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Button(
                onClick = { editingFaq = FaqItem(displayOrder = (faqs.size + 1).toLong()) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = PitchBlack),
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add FAQ")
            }
        }

        faqs.forEach { faq ->
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(faq.question, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text(faq.answer, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2)
                    }
                    IconButton(onClick = { editingFaq = faq }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit FAQ", tint = ElectricCyan)
                    }
                    IconButton(onClick = { ownerViewModel.deleteFaq(faq.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete FAQ", tint = ErrorRed)
                    }
                }
            }
        }
    }

    if (editingOffer != null) {
        BonusOfferEditorDialog(
            initial = editingOffer!!,
            products = products,
            onDismiss = { editingOffer = null },
            onSave = {
                ownerViewModel.saveBonusOffer(it)
                editingOffer = null
            }
        )
    }

    if (editingFaq != null) {
        FaqEditorDialog(
            initial = editingFaq!!,
            onDismiss = { editingFaq = null },
            onSave = {
                ownerViewModel.saveFaq(it)
                editingFaq = null
            }
        )
    }
}

@Composable
private fun MetricBox(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily), color = color)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BonusOfferEditorDialog(
    initial: BonusOffer,
    products: List<Product>,
    onDismiss: () -> Unit,
    onSave: (BonusOffer) -> Unit
) {
    var title by remember { mutableStateOf(initial.title) }
    var desc by remember { mutableStateOf(initial.description) }
    var selectedProductId by remember { mutableStateOf(initial.productId) }
    var prodName by remember { mutableStateOf(initial.productName) }
    var origPrice by remember { mutableStateOf(initial.originalPriceBdt.toString()) }
    var discPrice by remember { mutableStateOf(initial.discountedPriceBdt.toString()) }
    var dropDay by remember { mutableStateOf(initial.dropDayOfMonth) }
    var durationDays by remember { mutableStateOf("2") }
    var active by remember { mutableStateOf(initial.active) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Bonus Drop Offer", color = GoldAccent) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Offer Title *") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Offer Description") })
                Text("Select Package", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    products.take(6).forEach { p ->
                        FilterChip(
                            selected = selectedProductId == p.id,
                            onClick = {
                                selectedProductId = p.id
                                prodName = p.name
                                origPrice = p.priceBdt.toString()
                            },
                            label = { Text(p.name) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = origPrice,
                        onValueChange = { origPrice = it },
                        label = { Text("Original ৳") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = discPrice,
                        onValueChange = { discPrice = it },
                        label = { Text("Discounted ৳") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("Monthly Schedule Day", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10L, 20L, 30L).forEach { day ->
                        FilterChip(
                            selected = dropDay == day,
                            onClick = { dropDay = day },
                            label = { Text("${day}th") }
                        )
                    }
                }
                OutlinedTextField(
                    value = durationDays,
                    onValueChange = { durationDays = it },
                    label = { Text("Offer Window (Days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Active", color = TextPrimary, modifier = Modifier.weight(1f))
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val oPrice = origPrice.toLongOrNull() ?: 0L
                    val dPrice = discPrice.toLongOrNull() ?: 0L
                    val days = (durationDays.toLongOrNull() ?: 2L).coerceIn(1L, 30L)
                    if (title.isNotBlank() && oPrice > 0L && dPrice > 0L) {
                        val nowMs = System.currentTimeMillis()
                        onSave(
                            initial.copy(
                                title = title.trim(),
                                description = desc.trim(),
                                productId = selectedProductId,
                                productName = prodName.ifBlank { "Roblox Package" },
                                originalPriceBdt = oPrice,
                                discountedPriceBdt = dPrice,
                                dropDayOfMonth = dropDay,
                                startDate = initial.startDate ?: Timestamp(Date(nowMs)),
                                endDate = Timestamp(Date(nowMs + days * 86400000L)),
                                active = active
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = PitchBlack)
            ) { Text("Save Offer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}

@Composable
private fun FaqEditorDialog(initial: FaqItem, onDismiss: () -> Unit, onSave: (FaqItem) -> Unit) {
    var question by remember { mutableStateOf(initial.question) }
    var answer by remember { mutableStateOf(initial.answer) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(if (initial.id.isBlank()) "Add FAQ" else "Edit FAQ", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = question, onValueChange = { question = it }, label = { Text("Question *") })
                OutlinedTextField(value = answer, onValueChange = { answer = it }, label = { Text("Answer *") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (question.trim().length >= 3 && answer.trim().length >= 3) {
                        onSave(initial.copy(question = question.trim(), answer = answer.trim()))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = PitchBlack)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}
