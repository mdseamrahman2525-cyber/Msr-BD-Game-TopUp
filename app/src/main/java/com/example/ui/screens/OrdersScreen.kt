package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.ui.components.BanglaTranslatePill
import com.example.ui.components.MsrScrollingBackdropForLazyList
import com.example.ui.components.OrderStatusChip
import com.example.ui.components.SectionBanglaTranslator
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.StoreFormatters
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrdersScreen(
    ordersState: UiState<List<Order>>,
    onShopNow: () -> Unit,
    onCopyFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var selectedStatus by remember { mutableStateOf<OrderStatus?>(null) }
    var invoiceOrder by remember { mutableStateOf<Order?>(null) }
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }

    Box(modifier = modifier.fillMaxSize()) {
        MsrScrollingBackdropForLazyList(listState = listState)

        when (ordersState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ElectricBlue)
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = ordersState.message, color = ErrorRed)
                }
            }
            is UiState.Success -> {
                val allOrders = ordersState.data
                val filteredOrders = remember(allOrders, selectedStatus) {
                    if (selectedStatus == null) allOrders
                    else allOrders.filter { it.status == selectedStatus!!.name }
                }

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        SectionTitle(
                            title = "My Orders & Tracking",
                            subtitle = "Live order progress & digital invoices"
                        )
                    }

                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedStatus == null,
                                    onClick = { selectedStatus = null },
                                    label = { Text("All (${allOrders.size})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PureWhite,
                                        selectedLabelColor = PureBlack,
                                        containerColor = DarkSurface,
                                        labelColor = TextSecondary
                                    ),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                )
                            }
                            items(OrderStatus.entries) { status ->
                                FilterChip(
                                    selected = selectedStatus == status,
                                    onClick = { selectedStatus = status },
                                    label = { Text(status.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PureWhite,
                                        selectedLabelColor = PureBlack,
                                        containerColor = DarkSurface,
                                        labelColor = TextSecondary
                                    ),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                )
                            }
                        }
                    }

                    if (filteredOrders.isEmpty()) {
                        item {
                            Surface(
                                color = DarkSurface.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    var ordersEmptyBangla by remember { mutableStateOf(false) }
                                    Icon(
                                        imageVector = Icons.Filled.ShoppingBag,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Text(
                                        text = if (ordersEmptyBangla) {
                                            if (allOrders.isEmpty()) "এখনো কোনো অর্ডার নেই" else "মিলে যাওয়া কোনো অর্ডার নেই"
                                        } else {
                                            if (allOrders.isEmpty()) "No Orders Yet" else "No Matching Orders"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Text(
                                        text = if (ordersEmptyBangla) {
                                            if (allOrders.isEmpty()) {
                                                "আপনার Roblox Robux এবং Plus Subscription অর্ডারগুলো এখানে লাইভ bKash/Nagad ভেরিফিকেশন স্ট্যাটাসসহ দেখতে পাবেন।"
                                            } else {
                                                "এই স্ট্যাটাস ফিল্টারে কোনো অর্ডার পাওয়া যায়নি।"
                                            }
                                        } else {
                                            if (allOrders.isEmpty()) {
                                                "Your Roblox Robux and Plus Subscription orders will appear here with live bKash verification status."
                                            } else {
                                                "No orders found in this status filter."
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    BanglaTranslatePill(
                                        isBangla = ordersEmptyBangla,
                                        onToggle = { ordersEmptyBangla = !ordersEmptyBangla },
                                        testTag = "orders_empty_bangla_toggle"
                                    )
                                    if (allOrders.isEmpty()) {
                                        Button(
                                            onClick = onShopNow,
                                            colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Text("Browse Services", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredOrders, key = { it.id }) { order ->
                            val dateStr = order.createdAt?.toDate()?.let { dateFmt.format(it) } ?: "Just now"
                            Surface(
                                color = DarkSurface.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { invoiceOrder = order }
                                    .testTag("order_card_${order.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = order.orderCode,
                                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = MonoFontFamily),
                                                color = ElectricCyan
                                            )
                                            Text(
                                                text = dateStr,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                        OrderStatusChip(statusRaw = order.status)
                                    }

                                    HorizontalDivider(color = DarkCardBorder)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = order.productName,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${order.targetAccountLabel}: ${order.targetAccountInput}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = "${order.paymentMethod} ${order.paymentSenderNumber} • TrxID: ${order.transactionId}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                                                color = TextMuted
                                            )
                                        }
                                        Text(
                                            text = StoreFormatters.formatBdt(order.totalAmountBdt),
                                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = MonoFontFamily),
                                            color = ElectricCyan
                                        )
                                    }

                                    OrderProgressTracker(status = OrderStatus.fromRaw(order.status))

                                    if (order.adminNote.isNotBlank()) {
                                        var noteBangla by remember(order.id, order.adminNote) { mutableStateOf(false) }
                                        Surface(
                                            color = DarkSurfaceElevated,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (noteBangla) {
                                                        "ওনারের নোট: ${SectionBanglaTranslator.translateDynamicEnglish(order.adminNote)}"
                                                    } else {
                                                        "Owner Note: ${order.adminNote}"
                                                    },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextPrimary,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                BanglaTranslatePill(
                                                    isBangla = noteBangla,
                                                    onToggle = { noteBangla = !noteBangla },
                                                    compact = true,
                                                    testTag = "order_note_bangla_toggle_${order.id}"
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        OutlinedButton(
                                            onClick = { invoiceOrder = order },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Icon(Icons.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("View Invoice", color = ElectricCyan)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (invoiceOrder != null) {
        OrderInvoiceDialog(
            order = invoiceOrder!!,
            onDismiss = { invoiceOrder = null },
            onCopyFeedback = onCopyFeedback
        )
    }
}

@Composable
private fun OrderProgressTracker(status: OrderStatus) {
    if (status == OrderStatus.REJECTED) {
        var rejectedBangla by remember { mutableStateOf(false) }
        Surface(
            color = ErrorRed.copy(alpha = 0.14f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (rejectedBangla) {
                        "অর্ডার বাতিল হয়েছে — সাহায্যের জন্য আপনার Order ID সহ সাপোর্টে যোগাযোগ করুন।"
                    } else {
                        "Order Rejected — Contact support with your Order ID if you need assistance."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = ErrorRed,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(6.dp))
                BanglaTranslatePill(
                    isBangla = rejectedBangla,
                    onToggle = { rejectedBangla = !rejectedBangla },
                    compact = true,
                    testTag = "order_rejected_bangla_toggle"
                )
            }
        }
        return
    }

    val steps = listOf(
        OrderStatus.PENDING,
        OrderStatus.PAYMENT_VERIFICATION,
        OrderStatus.PROCESSING,
        OrderStatus.COMPLETED
    )
    val currentIdx = steps.indexOf(status).coerceAtLeast(0)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { idx, step ->
            val reached = idx <= currentIdx
            val dotColor = if (status == OrderStatus.COMPLETED) SuccessGreen else if (reached) ElectricBlue else DarkCardBorder
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (step) {
                        OrderStatus.PENDING -> "Pending"
                        OrderStatus.PAYMENT_VERIFICATION -> "Verify"
                        OrderStatus.PROCESSING -> "Process"
                        OrderStatus.COMPLETED -> "Done"
                        else -> ""
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (reached) TextPrimary else TextMuted
                )
            }
        }
    }
}

@Composable
private fun OrderInvoiceDialog(
    order: Order,
    onDismiss: () -> Unit,
    onCopyFeedback: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }
    val dateStr = order.createdAt?.toDate()?.let { dateFmt.format(it) } ?: "Pending sync"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Order Invoice", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryRow("Order ID", order.orderCode, highlight = true)
                SummaryRow("Status", OrderStatus.fromRaw(order.status).label)
                SummaryRow("Date", dateStr)
                HorizontalDivider(color = DarkCardBorder)
                SummaryRow("Game", order.gameName)
                SummaryRow("Category", order.categoryName)
                SummaryRow("Product", order.productName)
                SummaryRow("Quantity", order.quantityLabel)
                SummaryRow(order.targetAccountLabel, order.targetAccountInput)
                HorizontalDivider(color = DarkCardBorder)
                SummaryRow("Payment", order.paymentMethod)
                SummaryRow("Paid To", order.paymentRecipientNumber)
                SummaryRow("Sender", order.paymentSenderNumber)
                SummaryRow("TrxID", order.transactionId)
                SummaryRow("Total", StoreFormatters.formatBdt(order.totalAmountBdt), highlight = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val invoiceText = buildString {
                        appendLine("MSR BD GAME TOP-UP — INVOICE")
                        appendLine("Order ID: ${order.orderCode}")
                        appendLine("Status: ${OrderStatus.fromRaw(order.status).label}")
                        appendLine("Product: ${order.productName} (${order.quantityLabel})")
                        appendLine("${order.targetAccountLabel}: ${order.targetAccountInput}")
                        appendLine("${order.paymentMethod} Sender: ${order.paymentSenderNumber}")
                        appendLine("TrxID: ${order.transactionId}")
                        appendLine("Total: ${StoreFormatters.formatBdt(order.totalAmountBdt)}")
                    }
                    clipboard.setText(AnnotatedString(invoiceText))
                    onCopyFeedback("Invoice copied to clipboard")
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = PitchBlack)
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy Invoice")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}
