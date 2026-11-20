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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.StoreConfig
import com.example.data.model.UserProfile
import com.example.ui.components.MsrScrollableColumn
import com.example.ui.components.OrderStatusChip
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.OwnerViewModel
import com.example.ui.viewmodel.StoreFormatters
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OwnerOrdersTab(
    ownerViewModel: OwnerViewModel,
    allOrdersState: UiState<List<Order>>,
    storeConfig: StoreConfig
) {
    val search by ownerViewModel.orderSearch.collectAsStateWithLifecycle()
    val statusFilter by ownerViewModel.orderStatusFilter.collectAsStateWithLifecycle()
    var managingOrder by remember { mutableStateOf<Order?>(null) }
    val dateFmt = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }

    MsrScrollableColumn {
        OutlinedTextField(
            value = search,
            onValueChange = ownerViewModel::setOrderSearch,
            label = { Text("Search Order ID, Customer, Username, TrxID") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("owner_order_search_input")
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = statusFilter == null,
                onClick = { ownerViewModel.setOrderStatusFilter(null) },
                label = { Text("All") }
            )
            OrderStatus.entries.forEach { st ->
                FilterChip(
                    selected = statusFilter == st,
                    onClick = { ownerViewModel.setOrderStatusFilter(st) },
                    label = { Text(st.label) }
                )
            }
        }

        when (allOrdersState) {
            is UiState.Loading -> Text("Loading all orders...", color = TextSecondary)
            is UiState.Error -> Text(allOrdersState.message, color = ErrorRed)
            is UiState.Success -> {
                val filtered = allOrdersState.data.filter { o ->
                    val matchesStatus = statusFilter == null || o.status == statusFilter!!.name
                    val matchesQuery = search.isBlank() ||
                        o.orderCode.contains(search, true) ||
                        o.customerName.contains(search, true) ||
                        o.customerEmail.contains(search, true) ||
                        o.targetAccountInput.contains(search, true) ||
                        o.transactionId.contains(search, true) ||
                        o.paymentSenderNumber.contains(search, true)
                    matchesStatus && matchesQuery
                }

                if (filtered.isEmpty()) {
                    Text("No orders match the current search/filter.", color = TextSecondary)
                } else {
                    filtered.forEach { order ->
                        val dateStr = order.createdAt?.toDate()?.let { dateFmt.format(it) } ?: "Pending"
                        Surface(
                            color = DarkSurface.copy(alpha = 0.96f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = order.orderCode,
                                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = MonoFontFamily),
                                        color = ElectricCyan
                                    )
                                    OrderStatusChip(statusRaw = order.status)
                                }
                                Text(
                                    text = "${order.productName} • ${StoreFormatters.formatBdt(order.totalAmountBdt)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Customer: ${order.customerName} (${order.customerEmail})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${order.targetAccountLabel}: ${order.targetAccountInput}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${order.paymentMethod} Sender: ${order.paymentSenderNumber} | TrxID: ${order.transactionId}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                                    color = GoldAccent
                                )
                                Text(
                                    text = "Date: $dateStr | Points Awarded: ${if (order.pointsAwarded) "Yes" else "No"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (order.status != OrderStatus.COMPLETED.name && order.status != OrderStatus.REJECTED.name) {
                                        Button(
                                            onClick = {
                                                ownerViewModel.updateOrderStatus(
                                                    order = order,
                                                    status = OrderStatus.COMPLETED,
                                                    adminNote = order.adminNote.ifBlank { "Verified & Done" },
                                                    storeConfig = storeConfig
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = PitchBlack),
                                            modifier = Modifier
                                                .weight(1f)
                                                .defaultMinSize(minHeight = 48.dp)
                                                .testTag("mark_done_order_btn_${order.id}")
                                        ) {
                                            Text("Mark Done")
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = { managingOrder = order },
                                        modifier = Modifier
                                            .weight(1f)
                                            .defaultMinSize(minHeight = 48.dp)
                                            .testTag("manage_order_btn_${order.id}")
                                    ) {
                                        Text("Status / Note", color = ElectricCyan)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (managingOrder != null) {
        val target = managingOrder!!
        var note by remember(target.id) { mutableStateOf(target.adminNote) }
        var selectedStatus by remember(target.id) { mutableStateOf(OrderStatus.fromRaw(target.status)) }

        AlertDialog(
            onDismissRequest = { managingOrder = null },
            containerColor = DarkSurface,
            title = { Text("Update ${target.orderCode}", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${target.productName} • ${StoreFormatters.formatBdt(target.totalAmountBdt)}\nTrxID: ${target.transactionId} (${target.paymentSenderNumber})",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElectricCyan
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OrderStatus.entries.forEach { st ->
                            FilterChip(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = st },
                                label = { Text(st.label) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Admin Verification Note (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (selectedStatus == OrderStatus.COMPLETED && !target.pointsAwarded) {
                        Text(
                            text = "Completing this order will automatically award ${target.totalAmountBdt} loyalty points to ${target.customerName}.",
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessGreen
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        ownerViewModel.updateOrderStatus(target, selectedStatus, note, storeConfig)
                        managingOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = PitchBlack)
                ) {
                    Text("Save Status")
                }
            },
            dismissButton = {
                TextButton(onClick = { managingOrder = null }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

@Composable
fun OwnerCustomersTab(
    ownerViewModel: OwnerViewModel,
    allCustomersState: UiState<List<UserProfile>>,
    allOrders: List<Order>,
    storeConfig: StoreConfig
) {
    var editingCustomer by remember { mutableStateOf<UserProfile?>(null) }
    var search by remember { mutableStateOf("") }

    MsrScrollableColumn {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Search Customers by Name or Email") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        when (allCustomersState) {
            is UiState.Loading -> Text("Loading registered customers...", color = TextSecondary)
            is UiState.Error -> Text(allCustomersState.message, color = ErrorRed)
            is UiState.Success -> {
                val customers = allCustomersState.data.filter {
                    search.isBlank() || it.displayName.contains(search, true) || it.email.contains(search, true)
                }
                if (customers.isEmpty()) {
                    Text("No registered customers found.", color = TextSecondary)
                } else {
                    customers.forEach { customer ->
                        val custOrders = allOrders.filter { it.userId == customer.userId }
                        Surface(
                            color = DarkSurface.copy(alpha = 0.96f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(customer.displayName.ifBlank { "MSR Gamer" }, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                    Text(customer.membershipTier, style = MaterialTheme.typography.labelLarge, color = GoldAccent)
                                }
                                Text(customer.email, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                HorizontalDivider(color = DarkCardBorder)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Points: ${StoreFormatters.formatNumber(customer.points)}", style = MaterialTheme.typography.labelLarge, color = ElectricCyan)
                                    Text("Spent: ${StoreFormatters.formatBdt(customer.totalVerifiedSpending)}", style = MaterialTheme.typography.labelLarge, color = SuccessGreen)
                                    Text("Orders: ${custOrders.size}", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                                }
                                OutlinedButton(
                                    onClick = { editingCustomer = customer },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Text("Adjust Loyalty Points / Rewards", color = ElectricCyan)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingCustomer != null) {
        val target = editingCustomer!!
        var deltaText by remember { mutableStateOf("50") }
        var reason by remember { mutableStateOf("Owner Loyalty Bonus") }

        AlertDialog(
            onDismissRequest = { editingCustomer = null },
            containerColor = DarkSurface,
            title = { Text("Adjust Points: ${target.displayName}", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current Points: ${target.points}", color = GoldAccent)
                    OutlinedTextField(
                        value = deltaText,
                        onValueChange = { deltaText = it },
                        label = { Text("Points Delta (e.g. 100 or -50)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val delta = deltaText.trim().toLongOrNull() ?: 0L
                        if (delta != 0L) {
                            ownerViewModel.adjustCustomerPoints(target, delta, reason, storeConfig)
                        }
                        editingCustomer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = PitchBlack)
                ) { Text("Apply") }
            },
            dismissButton = {
                TextButton(onClick = { editingCustomer = null }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}
