package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.InitialCatalog
import com.example.data.model.Order
import com.example.data.model.StoreConfig
import com.example.data.repository.PaymentInputValidator
import com.example.data.repository.RobloxAccountVerifier
import com.example.data.repository.RobloxLookupResult
import com.example.ui.components.BanglaTranslatePill
import com.example.ui.components.LivePhoneValidationHint
import com.example.ui.components.LiveTrxIdValidationHint
import com.example.ui.components.MsrGlobalHeader
import com.example.ui.components.MsrScrollableColumn
import com.example.ui.components.OrderStatusChip
import com.example.ui.components.ProductVisualBadge
import com.example.ui.components.RobloxUsernameVerifyWidget
import com.example.ui.components.SectionBanglaTranslator
import com.example.ui.components.TranslatableTextBlock
import com.example.ui.theme.BlackCardBorder
import com.example.ui.theme.BlackSurface
import com.example.ui.theme.BlackSurfaceElevated
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.CheckoutSelection
import com.example.ui.viewmodel.StoreFormatters
import kotlinx.coroutines.launch

private val BkashPink = Color(0xFFE2136E)
private val NagadOrange = Color(0xFFF7941D)

@Composable
fun CheckoutScreen(
    selection: CheckoutSelection?,
    storeConfig: StoreConfig,
    isSubmitting: Boolean,
    isVerifiedOwner: Boolean,
    unreadNotifications: Int,
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenOwnerDashboard: () -> Unit,
    onCopyFeedback: (String) -> Unit,
    onSubmitOrder: (
        targetAccountLabel: String,
        targetAccountInput: String,
        paymentMethod: String,
        paymentRecipientNumber: String,
        paymentSenderNumber: String,
        transactionId: String
    ) -> Unit
) {
    val clipboard = LocalClipboardManager.current

    Scaffold(
        containerColor = PureBlack,
        topBar = {
            MsrGlobalHeader(
                pageTitle = "Secure Checkout",
                showBackButton = true,
                onBackClick = onBack,
                unreadNotificationCount = unreadNotifications,
                onNotificationsClick = onOpenNotifications,
                isOwner = isVerifiedOwner,
                onOwnerDashboardClick = onOpenOwnerDashboard,
                barColor = PureBlack
            )
        }
    ) { innerPadding ->
        if (selection == null) {
            MsrScrollableColumn(
                modifier = Modifier.padding(innerPadding),
                usePureBlackBg = true
            ) {
                Text("No product selected.", color = TextPrimary)
            }
            return@Scaffold
        }

        val product = selection.product
        val finalPrice = selection.discountedPriceBdt ?: product.priceBdt
        val requiredFields = remember(selection) {
            selection.category?.requiredInputFields?.filter { it.isNotBlank() }
                ?.ifEmpty { listOf("Roblox Username") }
                ?: listOf("Roblox Username")
        }
        val dynamicInputs = remember(selection) { mutableStateMapOf<String, String>() }
        val scope = rememberCoroutineScope()
        var robloxLookupResult by remember(selection) { mutableStateOf<RobloxLookupResult>(RobloxLookupResult.Idle) }
        var selectedPaymentMethod by remember { mutableStateOf("bKash") }
        var senderNumber by remember { mutableStateOf("") }
        var transactionId by remember { mutableStateOf("") }
        var paymentConfirmed by remember { mutableStateOf(false) }
        var validationError by remember { mutableStateOf<String?>(null) }
        val isRobloxStore = selection.game.id == InitialCatalog.GAME_ROBLOX_ID ||
            selection.game.name.contains("roblox", ignoreCase = true)
        val officialRecipient = if (selectedPaymentMethod == "Nagad") {
            storeConfig.nagadNumber.ifBlank { storeConfig.bkashNumber }
        } else {
            storeConfig.bkashNumber
        }

        MsrScrollableColumn(
            modifier = Modifier.padding(innerPadding),
            usePureBlackBg = true
        ) {
            // Order Summary Card (Pure Black Purchasing Area)
            Surface(
                color = BlackSurface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackCardBorder, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Order Summary",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProductVisualBadge(
                            imageUrl = product.imageUrl,
                            categoryType = product.categoryType,
                            size = 54.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhite
                            )
                            Text(
                                text = "${selection.game.name} • ${product.quantityLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            if (!selection.bonusOfferTitle.isNullOrBlank()) {
                                Text(
                                    text = "Bonus Drop: ${selection.bonusOfferTitle}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldAccent
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = BlackCardBorder)
                    SummaryRow("Selected Product", product.name)
                    SummaryRow("Quantity / Duration", product.quantityLabel)
                    SummaryRow("Product Price", StoreFormatters.formatBdt(finalPrice), highlight = true)
                    SummaryRow("Total Amount", StoreFormatters.formatBdt(finalPrice), highlight = true)
                }
            }

            // Payment Method Choice & Instructions Card (bKash or Nagad)
            Surface(
                color = BlackSurface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackCardBorder, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = if (selectedPaymentMethod == "Nagad") NagadOrange else BkashPink
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Payment Method: $selectedPaymentMethod",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("bKash" to BkashPink, "Nagad" to NagadOrange).forEach { (method, accent) ->
                            val isSelected = selectedPaymentMethod == method
                            Surface(
                                color = if (isSelected) BlackSurfaceElevated else BlackSurface,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) accent else BlackCardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedPaymentMethod = method }
                                    .testTag("checkout_method_${method.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(accent)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = method,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = PureWhite
                                    )
                                }
                            }
                        }
                    }
                    TranslatableTextBlock(
                        englishText = storeConfig.paymentInstructions,
                        banglaText = SectionBanglaTranslator.translatePaymentInstructions(
                            storeConfig.paymentInstructions,
                            officialRecipient,
                            selectedPaymentMethod
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        testTagPrefix = "checkout_instructions"
                    )
                    Surface(
                        color = BlackSurfaceElevated,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BlackCardBorder, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Official $selectedPaymentMethod Number",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = officialRecipient,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = MonoFontFamily),
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = {
                                    clipboard.setText(AnnotatedString(officialRecipient))
                                    onCopyFeedback("Copied $selectedPaymentMethod number: $officialRecipient")
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PureWhite,
                                    contentColor = PureBlack
                                ),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("copy_bkash_number_button")
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Copy", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Customer Fulfillment & Payment Details Form (Black Purchasing Area)
            Surface(
                color = BlackSurface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackCardBorder, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Order & Payment Verification Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite
                    )

                    var securityNoteBangla by remember { mutableStateOf(false) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SuccessGreen.copy(alpha = 0.10f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (securityNoteBangla) {
                                "আমরা কখনোই আপনার Roblox, Google বা ব্যক্তিগত কোনো পাসওয়ার্ড চাই না।"
                            } else {
                                "We NEVER ask for your Roblox, Google, or personal passwords."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        BanglaTranslatePill(
                            isBangla = securityNoteBangla,
                            onToggle = { securityNoteBangla = !securityNoteBangla },
                            compact = true,
                            testTag = "checkout_security_bangla_toggle"
                        )
                    }

                    requiredFields.forEachIndexed { idx, fieldLabel ->
                        val isRobloxField = isRobloxStore && (
                            fieldLabel.contains("username", ignoreCase = true) ||
                                fieldLabel.contains("roblox", ignoreCase = true)
                            )
                        OutlinedTextField(
                            value = dynamicInputs[fieldLabel] ?: "",
                            onValueChange = { newVal ->
                                dynamicInputs[fieldLabel] = newVal
                                if (isRobloxField) {
                                    robloxLookupResult = RobloxLookupResult.Idle
                                }
                                validationError = null
                            },
                            label = { Text("$fieldLabel *") },
                            placeholder = { Text("Enter your $fieldLabel") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = checkoutFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_target_input_$idx")
                        )
                        if (isRobloxField) {
                            RobloxUsernameVerifyWidget(
                                usernameInput = dynamicInputs[fieldLabel] ?: "",
                                lookupResult = robloxLookupResult,
                                onCheckClick = {
                                    val target = (dynamicInputs[fieldLabel] ?: "").trim()
                                    robloxLookupResult = RobloxLookupResult.Loading
                                    scope.launch {
                                        val res = RobloxAccountVerifier.lookupUsername(target)
                                        robloxLookupResult = res
                                        if (res is RobloxLookupResult.Found) {
                                            dynamicInputs[fieldLabel] = res.username
                                        }
                                    }
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = senderNumber,
                        onValueChange = {
                            senderNumber = it
                            validationError = null
                        },
                        label = { Text("Payment Sender Number ($selectedPaymentMethod, 11 Digits) *") },
                        placeholder = { Text("e.g. 017XXXXXXXX") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        colors = checkoutFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_sender_number_input")
                    )
                    LivePhoneValidationHint(phoneInput = senderNumber)

                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = {
                            transactionId = it.uppercase()
                            validationError = null
                        },
                        label = { Text("$selectedPaymentMethod Transaction ID (TrxID) *") },
                        placeholder = { Text(if (selectedPaymentMethod == "Nagad") "e.g. 74CLKT6G" else "e.g. BKA9X23M10") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = checkoutFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_trx_id_input")
                    )
                    LiveTrxIdValidationHint(
                        method = selectedPaymentMethod,
                        trxInput = transactionId
                    )

                    OutlinedTextField(
                        value = product.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selected Product") },
                        shape = RoundedCornerShape(12.dp),
                        colors = checkoutFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = StoreFormatters.formatBdt(finalPrice),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Total Amount") },
                        shape = RoundedCornerShape(12.dp),
                        colors = checkoutFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    var confirmTextBangla by remember { mutableStateOf(false) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { paymentConfirmed = !paymentConfirmed }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = paymentConfirmed,
                            onCheckedChange = { paymentConfirmed = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PureWhite,
                                checkmarkColor = PureBlack,
                                uncheckedColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("checkout_confirm_checkbox")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (confirmTextBangla) {
                                "আমি নিশ্চিত করছি যে $selectedPaymentMethod-এ $officialRecipient নম্বরে ${StoreFormatters.formatBdt(finalPrice)} পাঠিয়েছি এবং উপরের সব তথ্য সঠিক।"
                            } else {
                                "I confirm that I sent ${StoreFormatters.formatBdt(finalPrice)} via $selectedPaymentMethod to $officialRecipient and my details above are accurate."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        BanglaTranslatePill(
                            isBangla = confirmTextBangla,
                            onToggle = { confirmTextBangla = !confirmTextBangla },
                            compact = true,
                            testTag = "checkout_confirm_bangla_toggle"
                        )
                    }

                    if (validationError != null) {
                        Text(
                            text = validationError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorRed
                        )
                    }

                    Button(
                        onClick = {
                            val missingField = requiredFields.firstOrNull { (dynamicInputs[it] ?: "").trim().length < 2 }
                            val phoneValidation = PaymentInputValidator.validateBdPhoneNumber(senderNumber)
                            val trxValidation = PaymentInputValidator.validateTransactionId(selectedPaymentMethod, transactionId)
                            when {
                                missingField != null ->
                                    validationError = "Please enter a valid $missingField (at least 2 characters)."
                                robloxLookupResult is RobloxLookupResult.NotFound ->
                                    validationError = "এই Username টি Roblox-এ পাওয়া যায়নি। সঠিক Roblox Username দিয়ে চেক করুন।"
                                !phoneValidation.isValid ->
                                    validationError = phoneValidation.message
                                !trxValidation.isValid ->
                                    validationError = trxValidation.message
                                !paymentConfirmed ->
                                    validationError = "Please check the payment confirmation box before submitting."
                                else -> {
                                    validationError = null
                                    val combinedLabel = requiredFields.joinToString(" / ")
                                    val combinedValue = requiredFields.joinToString(" | ") { field ->
                                        if (requiredFields.size == 1) (dynamicInputs[field] ?: "").trim()
                                        else "$field: ${(dynamicInputs[field] ?: "").trim()}"
                                    }
                                    onSubmitOrder(
                                        combinedLabel,
                                        combinedValue,
                                        selectedPaymentMethod,
                                        officialRecipient,
                                        phoneValidation.normalizedValue,
                                        trxValidation.normalizedValue
                                    )
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = PureBlack,
                            disabledContainerColor = BlackSurfaceElevated,
                            disabledContentColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 52.dp)
                            .testTag("submit_order_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = PureBlack,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Submitting Order...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Filled.ReceiptLong, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Submit Order (${StoreFormatters.formatBdt(finalPrice)})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun OrderConfirmationScreen(
    order: Order?,
    isVerifiedOwner: Boolean,
    unreadNotifications: Int,
    onTrackOrders: () -> Unit,
    onBackToHome: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenOwnerDashboard: () -> Unit
) {
    val clipboard = LocalClipboardManager.current

    Scaffold(
        containerColor = PureBlack,
        topBar = {
            MsrGlobalHeader(
                pageTitle = "Order Submitted",
                unreadNotificationCount = unreadNotifications,
                onNotificationsClick = onOpenNotifications,
                isOwner = isVerifiedOwner,
                onOwnerDashboardClick = onOpenOwnerDashboard,
                barColor = PureBlack
            )
        }
    ) { innerPadding ->
        MsrScrollableColumn(
            modifier = Modifier.padding(innerPadding),
            usePureBlackBg = true
        ) {
            Surface(
                color = BlackSurface,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackCardBorder, RoundedCornerShape(20.dp))
                    .testTag("order_confirmation_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Order submitted",
                            tint = SuccessGreen,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = "Order Received!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhite
                    )
                    TranslatableTextBlock(
                        englishText = "Your order is now Pending payment verification by the owner.",
                        banglaText = "আপনার অর্ডারটি এখন ওনারের পেমেন্ট যাচাইয়ের জন্য Pending অবস্থায় আছে।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        centerToggle = true,
                        testTagPrefix = "confirmation_status"
                    )

                    if (order != null) {
                        OrderStatusChip(statusRaw = order.status)

                        Surface(
                            color = BlackSurfaceElevated,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
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
                                    Column {
                                        Text("Order ID", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        Text(
                                            text = order.orderCode,
                                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily),
                                            color = PureWhite
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { clipboard.setText(AnnotatedString(order.orderCode)) },
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Copy", color = PureWhite)
                                    }
                                }
                                HorizontalDivider(color = BlackCardBorder)
                                SummaryRow("Product", order.productName)
                                SummaryRow("Quantity", order.quantityLabel)
                                SummaryRow(order.targetAccountLabel, order.targetAccountInput)
                                SummaryRow("${order.paymentMethod} Sender", order.paymentSenderNumber)
                                SummaryRow("Transaction ID", order.transactionId)
                                SummaryRow("Total Paid", StoreFormatters.formatBdt(order.totalAmountBdt), highlight = true)
                            }
                        }

                        TranslatableTextBlock(
                            englishText = "Loyalty Note: You will earn ${order.totalAmountBdt} points automatically once the owner verifies and completes your order.",
                            banglaText = "লয়্যালটি নোট: ওনার আপনার অর্ডার যাচাই করে Done করলে স্বয়ংক্রিয়ভাবে ${order.totalAmountBdt} পয়েন্ট যোগ হবে।",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarningAmber,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            centerToggle = true,
                            testTagPrefix = "confirmation_loyalty"
                        )
                    }

                    Button(
                        onClick = onTrackOrders,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 50.dp)
                            .testTag("track_orders_button")
                    ) {
                        Text("Track My Orders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onBackToHome,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Filled.Storefront, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Back to Home", color = PureWhite)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(
            text = value,
            style = if (highlight) MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily)
            else MaterialTheme.typography.bodyMedium,
            color = PureWhite,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun checkoutFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = BlackSurfaceElevated,
    unfocusedContainerColor = BlackSurfaceElevated,
    focusedBorderColor = PureWhite,
    unfocusedBorderColor = BlackCardBorder,
    focusedTextColor = PureWhite,
    unfocusedTextColor = PureWhite,
    focusedLabelColor = PureWhite,
    unfocusedLabelColor = TextSecondary
)
