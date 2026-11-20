package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.Game
import com.example.data.model.InitialCatalog
import com.example.data.model.Product
import com.example.data.model.StoreConfig
import com.example.data.model.UserProfile
import com.example.data.repository.PaymentInputValidator
import com.example.data.repository.RobloxAccountVerifier
import com.example.data.repository.RobloxLookupResult
import com.example.ui.components.BanglaTranslatePill
import com.example.ui.components.LivePhoneValidationHint
import com.example.ui.components.LiveTrxIdValidationHint
import com.example.ui.components.MsrGlobalHeader
import com.example.ui.components.MsrScrollableColumn
import com.example.ui.components.RobloxUsernameVerifyWidget
import com.example.ui.theme.BlackCardBorder
import com.example.ui.theme.BlackSurface
import com.example.ui.theme.BlackSurfaceElevated
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.StoreCatalogData
import com.example.ui.viewmodel.StoreFormatters
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.launch

private val BkashPink = Color(0xFFE2136E)
private val NagadOrange = Color(0xFFF7941D)

@Composable
fun CategoryPackagesScreen(
    catalogState: UiState<StoreCatalogData>,
    storeConfig: StoreConfig,
    gameId: String,
    categoryId: String,
    userProfile: UserProfile?,
    isVerifiedOwner: Boolean,
    unreadNotifications: Int,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenOwnerDashboard: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onCopyFeedback: (String) -> Unit,
    onSubmitOrder: (
        product: Product,
        game: Game,
        category: Category?,
        targetAccountLabel: String,
        targetAccountInput: String,
        paymentMethod: String,
        paymentRecipientNumber: String,
        paymentSenderNumber: String,
        transactionId: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    val catalog = (catalogState as? UiState.Success)?.data
    val category = catalog?.categories?.firstOrNull { it.id == categoryId }
    val pageTitle = when (categoryId) {
        InitialCatalog.CAT_ROBUX_ID -> "Robux Top-Up"
        InitialCatalog.CAT_ROBLOX_PLUS_ID -> "Roblox Subscription"
        else -> category?.name ?: "Select Package"
    }

    Scaffold(
        modifier = modifier,
        containerColor = PureBlack,
        topBar = {
            MsrGlobalHeader(
                pageTitle = pageTitle,
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
        when (catalogState) {
            is UiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(PureBlack)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PureWhite)
                }
            }
            is UiState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(PureBlack)
                        .padding(innerPadding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = catalogState.message, color = ErrorRed)
                }
            }
            is UiState.Success -> {
                val data = catalogState.data
                val activeConfig = data.storeConfig
                if (activeConfig.maintenanceMode && !isVerifiedOwner) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(PureBlack)
                            .padding(innerPadding)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeConfig.maintenanceMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                    }
                    return@Scaffold
                }

                val currentGame = data.games.firstOrNull { it.id == gameId && it.active }
                    ?: data.games.firstOrNull { it.active }
                    ?: Game(id = gameId, name = "Roblox")
                val currentCategory = data.categories.firstOrNull { it.id == categoryId }
                val packages = remember(data.products, categoryId) {
                    data.products.filter { it.active && it.categoryId == categoryId }
                        .sortedBy { it.displayOrder }
                }

                val bannerDrawable = when {
                    categoryId == InitialCatalog.CAT_ROBUX_ID ||
                        currentCategory?.type == CategoryType.CURRENCY.name -> R.drawable.msr_robux_option
                    categoryId == InitialCatalog.CAT_ROBLOX_PLUS_ID ||
                        currentCategory?.type == CategoryType.SUBSCRIPTION.name -> R.drawable.msr_subscription_option
                    else -> null
                }

                var selectedProductId by remember(categoryId) {
                    mutableStateOf(packages.firstOrNull { it.available }?.id ?: packages.firstOrNull()?.id)
                }
                LaunchedEffect(packages) {
                    if (selectedProductId == null || packages.none { it.id == selectedProductId }) {
                        selectedProductId = packages.firstOrNull { it.available }?.id ?: packages.firstOrNull()?.id
                    }
                }
                val selectedProduct = packages.firstOrNull { it.id == selectedProductId }

                val requiredFields = remember(currentCategory) {
                    currentCategory?.requiredInputFields?.filter { it.isNotBlank() }
                        ?.ifEmpty { listOf("Roblox Username") }
                        ?: listOf("Roblox Username")
                }
                val dynamicInputs = remember(categoryId) { mutableStateMapOf<String, String>() }
                val scope = rememberCoroutineScope()
                var robloxLookupResult by remember(categoryId) { mutableStateOf<RobloxLookupResult>(RobloxLookupResult.Idle) }
                var selectedPaymentMethod by remember { mutableStateOf("bKash") }
                var senderNumber by remember { mutableStateOf("") }
                var transactionId by remember { mutableStateOf("") }
                var paymentConfirmed by remember { mutableStateOf(false) }
                var validationError by remember { mutableStateOf<String?>(null) }

                val isRobloxStore = currentGame.id == InitialCatalog.GAME_ROBLOX_ID ||
                    currentGame.name.contains("roblox", ignoreCase = true)

                val officialRecipient = if (selectedPaymentMethod == "Nagad") {
                    activeConfig.nagadNumber.ifBlank { activeConfig.bkashNumber }
                } else {
                    activeConfig.bkashNumber
                }
                val favIds = userProfile?.favoriteProductIds ?: emptyList()

                MsrScrollableColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .imePadding(),
                    usePureBlackBg = true
                ) {
                    // Compact Category Banner Strip
                    if (bannerDrawable != null) {
                        Surface(
                            color = BlackSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BlackCardBorder, RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = bannerDrawable),
                                    contentDescription = pageTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(width = 76.dp, height = 56.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    var bannerStepsBangla by remember { mutableStateOf(false) }
                                    Text(
                                        text = "${currentGame.name} • $pageTitle",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (bannerStepsBangla) {
                                                "৩টি সহজ ধাপ: প্যাকেজ বাছাই → অ্যাকাউন্ট ও bKash/Nagad → অর্ডার"
                                            } else {
                                                "3 Quick Steps: Package → Account & bKash/Nagad → Order"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        BanglaTranslatePill(
                                            isBangla = bannerStepsBangla,
                                            onToggle = { bannerStepsBangla = !bannerStepsBangla },
                                            compact = true,
                                            testTag = "category_steps_bangla_toggle"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 1ST STEP: SELECT RECHARGE (2-column radio capsule grid matching Sketch 2)
                    StepSectionHeader(stepBadge = "1st", title = "Select Recharge")

                    if (packages.isEmpty()) {
                        Surface(
                            color = BlackSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BlackCardBorder, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Inventory2, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                                Text("No active packages right now.", color = TextPrimary)
                            }
                        }
                    } else {
                        val rows = packages.chunked(2)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            rows.forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    pair.forEach { prod ->
                                        val isSelected = selectedProduct?.id == prod.id
                                        RechargeOptionPill(
                                            product = prod,
                                            selected = isSelected,
                                            onClick = {
                                                if (prod.available) {
                                                    selectedProductId = prod.id
                                                    validationError = null
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    // 2ND STEP: ACCOUNT INFO + BKASH / NAGAD CHOICE (Sketch 2)
                    StepSectionHeader(stepBadge = "2nd", title = "Account Info")

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
                            // 1. Enter your username + Check Roblox Game ID Name
                            requiredFields.forEachIndexed { idx, fieldLabel ->
                                val isRobloxField = isRobloxStore && (
                                    fieldLabel.contains("username", ignoreCase = true) ||
                                        fieldLabel.contains("roblox", ignoreCase = true)
                                    )
                                Text(
                                    text = "${idx + 1}. Enter your $fieldLabel",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PureWhite,
                                    fontWeight = FontWeight.SemiBold
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
                                    placeholder = { Text("Enter $fieldLabel", color = TextMuted) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = blackFormColors(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("purchase_target_input_$idx")
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

                            HorizontalDivider(color = BlackCardBorder)

                            // Choose bKash or Nagad
                            Text(
                                text = "Choose Payment Method (bKash / Nagad)",
                                style = MaterialTheme.typography.titleSmall,
                                color = PureWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                PaymentMethodPill(
                                    name = "bKash",
                                    accentColor = BkashPink,
                                    selected = selectedPaymentMethod == "bKash",
                                    onClick = { selectedPaymentMethod = "bKash" },
                                    testTag = "payment_method_bkash",
                                    modifier = Modifier.weight(1f)
                                )
                                PaymentMethodPill(
                                    name = "Nagad",
                                    accentColor = NagadOrange,
                                    selected = selectedPaymentMethod == "Nagad",
                                    onClick = { selectedPaymentMethod = "Nagad" },
                                    testTag = "payment_method_nagad",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Official Number & Copy
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
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Send Money ($selectedPaymentMethod)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = officialRecipient,
                                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = MonoFontFamily),
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
                                            .testTag("copy_payment_number_button")
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Copy", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // 2. Enter your transaction id & payment phone number
                            Text(
                                text = "${requiredFields.size + 1}. Enter your payment number & transaction id",
                                style = MaterialTheme.typography.titleSmall,
                                color = PureWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                            OutlinedTextField(
                                value = senderNumber,
                                onValueChange = {
                                    senderNumber = it
                                    validationError = null
                                },
                                label = { Text("$selectedPaymentMethod Payment Phone Number (11 Digits) *") },
                                placeholder = { Text("017XXXXXXXX", color = TextMuted) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(14.dp),
                                colors = blackFormColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("purchase_sender_input")
                            )
                            LivePhoneValidationHint(phoneInput = senderNumber)

                            OutlinedTextField(
                                value = transactionId,
                                onValueChange = {
                                    transactionId = it.uppercase()
                                    validationError = null
                                },
                                label = { Text("$selectedPaymentMethod Transaction ID (TrxID) *") },
                                placeholder = {
                                    Text(
                                        if (selectedPaymentMethod == "Nagad") "e.g. 74CLKT6G" else "e.g. BKA9X23M10",
                                        color = TextMuted
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = blackFormColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("purchase_trx_input")
                            )
                            LiveTrxIdValidationHint(
                                method = selectedPaymentMethod,
                                trxInput = transactionId
                            )

                            // Bangla helpful note (Sketch 2 footer)
                            Surface(
                                color = BlackSurfaceElevated,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BlackCardBorder, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "প্রোডাক্ট কিনতে আপনার প্রয়োজন: আপনার সঠিক Username এবং bKash/Nagad থেকে Send Money করা নম্বর ও Transaction ID। কোনো পাসওয়ার্ড লাগবে না।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // 3RD STEP: CHECK & PLACE ORDER
                    StepSectionHeader(stepBadge = "3rd", title = "Check & Place Order")

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
                            if (selectedProduct != null) {
                                val isFav = favIds.contains(selectedProduct.id)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = selectedProduct.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite
                                        )
                                        Text(
                                            text = "$selectedPaymentMethod • Total: ${StoreFormatters.formatBdt(selectedProduct.priceBdt)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = MonoFontFamily),
                                            color = PureWhite
                                        )
                                    }
                                    IconButton(
                                        onClick = { onToggleFavorite(selectedProduct.id) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Favorite package",
                                            tint = if (isFav) ErrorRed else TextMuted
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = BlackCardBorder)

                            var purchaseConfirmBangla by remember { mutableStateOf(false) }
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
                                    modifier = Modifier.testTag("purchase_confirm_checkbox")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (purchaseConfirmBangla) {
                                        if (selectedProduct != null) {
                                            "আমি নিশ্চিত করছি যে $selectedPaymentMethod-এ $officialRecipient নম্বরে ${StoreFormatters.formatBdt(selectedProduct.priceBdt)} পাঠিয়েছি।"
                                        } else {
                                            "আমি নিশ্চিত করছি যে সব অর্ডার এবং পেমেন্ট তথ্য সঠিক।"
                                        }
                                    } else {
                                        if (selectedProduct != null) {
                                            "I confirm I sent ${StoreFormatters.formatBdt(selectedProduct.priceBdt)} via $selectedPaymentMethod to $officialRecipient."
                                        } else {
                                            "I confirm all order and payment details are accurate."
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                BanglaTranslatePill(
                                    isBangla = purchaseConfirmBangla,
                                    onToggle = { purchaseConfirmBangla = !purchaseConfirmBangla },
                                    compact = true,
                                    testTag = "purchase_confirm_bangla_toggle"
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
                                    val prod = selectedProduct
                                    val missingField = requiredFields.firstOrNull { (dynamicInputs[it] ?: "").trim().length < 2 }
                                    val phoneValidation = PaymentInputValidator.validateBdPhoneNumber(senderNumber)
                                    val trxValidation = PaymentInputValidator.validateTransactionId(selectedPaymentMethod, transactionId)
                                    when {
                                        prod == null || !prod.available ->
                                            validationError = "Please select an available package in Step 1."
                                        missingField != null ->
                                            validationError = "Please enter a valid $missingField in Step 2."
                                        robloxLookupResult is RobloxLookupResult.NotFound ->
                                            validationError = "এই Username টি Roblox-এ পাওয়া যায়নি। সঠিক Roblox Username দিয়ে চেক করুন।"
                                        !phoneValidation.isValid ->
                                            validationError = phoneValidation.message
                                        !trxValidation.isValid ->
                                            validationError = trxValidation.message
                                        !paymentConfirmed ->
                                            validationError = "Please check the confirmation box above."
                                        else -> {
                                            validationError = null
                                            val combinedLabel = requiredFields.joinToString(" / ")
                                            val combinedValue = requiredFields.joinToString(" | ") { field ->
                                                if (requiredFields.size == 1) (dynamicInputs[field] ?: "").trim()
                                                else "$field: ${(dynamicInputs[field] ?: "").trim()}"
                                            }
                                            onSubmitOrder(
                                                prod,
                                                currentGame,
                                                currentCategory,
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
                                enabled = !isSubmitting && selectedProduct != null,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PureWhite,
                                    contentColor = PureBlack,
                                    disabledContainerColor = BlackSurfaceElevated,
                                    disabledContentColor = TextSecondary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 54.dp)
                                    .testTag("place_order_button")
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = PureBlack,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text("Placing Order...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Filled.ReceiptLong, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    val totalText = selectedProduct?.let { StoreFormatters.formatBdt(it.priceBdt) } ?: ""
                                    Text(
                                        text = if (totalText.isNotBlank()) "Place Order ($totalText)" else "Place Order",
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
    }
}

@Composable
private fun StepSectionHeader(stepBadge: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            color = PureWhite,
            shape = CircleShape,
            modifier = Modifier.defaultMinSize(minWidth = 40.dp, minHeight = 32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stepBadge,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = PureBlack
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PureWhite
        )
    }
}

@Composable
private fun RechargeOptionPill(
    product: Product,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (selected) BlackSurfaceElevated else BlackSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) PureWhite else BlackCardBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = product.available, onClick = onClick)
            .testTag("package_option_${product.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = if (selected) PureWhite else TextSecondary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(PureWhite)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = product.quantityLabel.ifBlank { product.name },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (product.available) PureWhite else TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (product.available) "${product.priceBdt} BDT" else "Out",
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily),
                fontWeight = FontWeight.Bold,
                color = if (product.available) PureWhite else ErrorRed,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PaymentMethodPill(
    name: String,
    accentColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (selected) BlackSurfaceElevated else BlackSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) accentColor else BlackCardBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = PureWhite
            )
        }
    }
}

@Composable
private fun blackFormColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = BlackSurfaceElevated,
    unfocusedContainerColor = BlackSurfaceElevated,
    focusedBorderColor = PureWhite,
    unfocusedBorderColor = BlackCardBorder,
    focusedTextColor = PureWhite,
    unfocusedTextColor = PureWhite,
    focusedLabelColor = PureWhite,
    unfocusedLabelColor = TextSecondary
)
