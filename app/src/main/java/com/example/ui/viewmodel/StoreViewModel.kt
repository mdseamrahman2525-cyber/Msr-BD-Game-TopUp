package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Announcement
import com.example.data.model.AppNotification
import com.example.data.model.BonusOffer
import com.example.data.model.Category
import com.example.data.model.FaqItem
import com.example.data.model.Game
import com.example.data.model.InitialCatalog
import com.example.data.model.Order
import com.example.data.model.PointsEntry
import com.example.data.model.Product
import com.example.data.model.StoreConfig
import com.example.data.model.UserProfile
import com.example.data.repository.StoreRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class StoreCatalogData(
    val storeConfig: StoreConfig = InitialCatalog.defaultStoreConfig,
    val games: List<Game> = InitialCatalog.defaultGames,
    val categories: List<Category> = InitialCatalog.defaultCategories,
    val products: List<Product> = InitialCatalog.defaultProducts,
    val bonusOffers: List<BonusOffer> = emptyList(),
    val faqs: List<FaqItem> = InitialCatalog.defaultFaqs,
    val announcements: List<Announcement> = emptyList()
)

data class CheckoutSelection(
    val product: Product,
    val game: Game,
    val category: Category?,
    val discountedPriceBdt: Long? = null,
    val bonusOfferTitle: String? = null
)

class StoreViewModel(
    private val repository: StoreRepository,
    private val currentUser: FirebaseUser,
    val isVerifiedOwner: Boolean
) : ViewModel() {

    private val currentUserId: String = currentUser.uid

    private val _selectedGameId = MutableStateFlow<String?>(null)
    val selectedGameId: StateFlow<String?> = _selectedGameId.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _checkoutSelection = MutableStateFlow<CheckoutSelection?>(null)
    val checkoutSelection: StateFlow<CheckoutSelection?> = _checkoutSelection.asStateFlow()

    private val _lastConfirmedOrder = MutableStateFlow<Order?>(null)
    val lastConfirmedOrder: StateFlow<Order?> = _lastConfirmedOrder.asStateFlow()

    private val _isSubmittingOrder = MutableStateFlow(false)
    val isSubmittingOrder: StateFlow<Boolean> = _isSubmittingOrder.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _serverOwnerVerified = MutableStateFlow(isVerifiedOwner)
    val serverOwnerVerified: StateFlow<Boolean> = _serverOwnerVerified.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureUserProfileAndSeedIfOwner(currentUser, isVerifiedOwner)
            if (isVerifiedOwner) {
                _serverOwnerVerified.value = repository.verifyOwnerAdminRecord(currentUserId)
            }
        }
    }

    val catalogState: StateFlow<UiState<StoreCatalogData>> = combine(
        repository.observeStoreConfig(),
        repository.observeGames(isOwner = isVerifiedOwner),
        repository.observeCategories(isOwner = isVerifiedOwner),
        repository.observeProducts(isOwner = isVerifiedOwner),
        repository.observeBonusOffers(isOwner = isVerifiedOwner),
        repository.observeFaqs(isOwner = isVerifiedOwner),
        repository.observeAnnouncements(isOwner = isVerifiedOwner)
    ) { values ->
        val config = values[0] as StoreConfig
        @Suppress("UNCHECKED_CAST")
        val games = values[1] as List<Game>
        @Suppress("UNCHECKED_CAST")
        val categories = values[2] as List<Category>
        @Suppress("UNCHECKED_CAST")
        val products = values[3] as List<Product>
        @Suppress("UNCHECKED_CAST")
        val offers = values[4] as List<BonusOffer>
        @Suppress("UNCHECKED_CAST")
        val faqs = values[5] as List<FaqItem>
        @Suppress("UNCHECKED_CAST")
        val announcements = values[6] as List<Announcement>

        // If Firestore hasn't been seeded yet by the owner, show the official initial Roblox catalog cleanly
        val effectiveGames = if (games.isEmpty() && !isVerifiedOwner) InitialCatalog.defaultGames else games
        val effectiveCategories = if (categories.isEmpty() && !isVerifiedOwner) InitialCatalog.defaultCategories else categories
        val effectiveProducts = if (products.isEmpty() && !isVerifiedOwner) InitialCatalog.defaultProducts else products
        val effectiveFaqs = if (faqs.isEmpty() && !isVerifiedOwner) InitialCatalog.defaultFaqs else faqs

        UiState.Success(
            StoreCatalogData(
                storeConfig = config,
                games = effectiveGames,
                categories = effectiveCategories,
                products = effectiveProducts,
                bonusOffers = offers,
                faqs = effectiveFaqs,
                announcements = announcements
            )
        ) as UiState<StoreCatalogData>
    }
        .catch { error -> emit(unwrapFirestoreError(error, "Failed to load store catalog")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Success(StoreCatalogData()))

    val userProfileState: StateFlow<UiState<UserProfile?>> = repository.observeUserProfile(currentUserId)
        .asUiState("Failed to load profile")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val ordersState: StateFlow<UiState<List<Order>>> = repository.observeUserOrders(currentUserId)
        .asUiState("Failed to load orders")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val pointsHistoryState: StateFlow<UiState<List<PointsEntry>>> = repository.observePointsHistory(currentUserId)
        .asUiState("Failed to load points history")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val notificationsState: StateFlow<UiState<List<AppNotification>>> = repository.observeNotifications(currentUserId)
        .asUiState("Failed to load notifications")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    fun selectGame(gameId: String) {
        _selectedGameId.value = gameId
        _selectedCategoryId.value = null
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun startCheckout(
        product: Product,
        game: Game,
        category: Category?,
        discountedPriceBdt: Long? = null,
        bonusOfferTitle: String? = null
    ) {
        _checkoutSelection.value = CheckoutSelection(
            product = product,
            game = game,
            category = category,
            discountedPriceBdt = discountedPriceBdt,
            bonusOfferTitle = bonusOfferTitle
        )
    }

    fun toggleFavorite(productId: String, currentFavorites: List<String>) {
        viewModelScope.launch {
            repository.toggleFavoriteProduct(currentUserId, productId, currentFavorites)
                .onFailure { _snackbarMessage.value = "Could not update favorite." }
        }
    }

    fun submitOrder(
        targetAccountLabel: String,
        targetAccountInput: String,
        paymentMethod: String,
        paymentRecipientNumber: String,
        paymentSenderNumber: String,
        transactionId: String,
        onSuccess: (Order) -> Unit
    ) {
        val selection = _checkoutSelection.value ?: return
        submitDirectOrder(
            game = selection.game,
            categoryName = selection.category?.name ?: "Roblox Top-Up",
            product = selection.product,
            overridePriceBdt = selection.discountedPriceBdt,
            targetAccountLabel = targetAccountLabel,
            targetAccountInput = targetAccountInput,
            paymentMethod = paymentMethod,
            paymentRecipientNumber = paymentRecipientNumber,
            paymentSenderNumber = paymentSenderNumber,
            transactionId = transactionId,
            onSuccess = onSuccess
        )
    }

    fun submitDirectOrder(
        game: Game,
        categoryName: String,
        product: Product,
        overridePriceBdt: Long? = null,
        targetAccountLabel: String,
        targetAccountInput: String,
        paymentMethod: String,
        paymentRecipientNumber: String,
        paymentSenderNumber: String,
        transactionId: String,
        onSuccess: (Order) -> Unit
    ) {
        viewModelScope.launch {
            _isSubmittingOrder.value = true
            val result = repository.submitOrder(
                game = game,
                categoryName = categoryName,
                product = product,
                overridePriceBdt = overridePriceBdt,
                targetAccountLabel = targetAccountLabel,
                targetAccountInput = targetAccountInput,
                paymentMethod = paymentMethod,
                paymentRecipientNumber = paymentRecipientNumber,
                paymentSenderNumber = paymentSenderNumber,
                transactionId = transactionId
            )
            _isSubmittingOrder.value = false
            result.onSuccess { order ->
                _lastConfirmedOrder.value = order
                onSuccess(order)
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Failed to submit order. Please retry."
            }
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(currentUserId, notificationId)
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAccountProfile(currentUserId)
                .onSuccess { onDeleted() }
                .onFailure { _snackbarMessage.value = it.localizedMessage ?: "Re-authentication may be required to delete account." }
        }
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearMessage() {
        _snackbarMessage.value = null
    }

    private fun <T> Flow<T>.asUiState(fallbackMsg: String): Flow<UiState<T>> =
        kotlinx.coroutines.flow.flow<UiState<T>> {
            collect { value -> emit(UiState.Success(value)) }
        }.catch { error ->
            emit(unwrapFirestoreError<T>(error, fallbackMsg))
        }

    private fun <T> unwrapFirestoreError(error: Throwable, fallbackMsg: String): UiState<T> {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val root = firestoreError ?: error
        Log.w("StoreVM", fallbackMsg, root)
        return UiState.Error(root.localizedMessage ?: fallbackMsg)
    }
}

object StoreFormatters {
    private val bdtFormatter = NumberFormat.getNumberInstance(Locale.US)

    fun formatBdt(amount: Long): String = "৳${bdtFormatter.format(amount)}"

    fun formatNumber(value: Long): String = bdtFormatter.format(value)

    data class BonusDropCountdown(
        val nextDateLabel: String,
        val days: Long,
        val hours: Long,
        val minutes: Long,
        val seconds: Long,
        val isDropDayToday: Boolean
    )

    fun computeNextBonusDrop(nowMillis: Long = System.currentTimeMillis()): BonusDropCountdown {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val isDropToday = day == 10 || day == 20 || day == 30

        val target = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 0)
            when {
                day <= 10 -> set(Calendar.DAY_OF_MONTH, 10)
                day <= 20 -> set(Calendar.DAY_OF_MONTH, 20)
                day <= 30 -> set(Calendar.DAY_OF_MONTH, 30)
                else -> {
                    add(Calendar.MONTH, 1)
                    set(Calendar.DAY_OF_MONTH, 10)
                }
            }
        }

        val diff = (target.timeInMillis - nowMillis).coerceAtLeast(0L)
        val totalSec = diff / 1000L
        val days = totalSec / 86400L
        val hours = (totalSec % 86400L) / 3600L
        val mins = (totalSec % 3600L) / 60L
        val secs = totalSec % 60L

        val monthName = target.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) ?: ""
        val targetDay = target.get(Calendar.DAY_OF_MONTH)
        return BonusDropCountdown(
            nextDateLabel = "$targetDay $monthName",
            days = days,
            hours = hours,
            minutes = mins,
            seconds = secs,
            isDropDayToday = isDropToday
        )
    }
}
