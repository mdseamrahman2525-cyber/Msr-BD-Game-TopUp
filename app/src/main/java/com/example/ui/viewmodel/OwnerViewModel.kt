package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BonusOffer
import com.example.data.model.Category
import com.example.data.model.FaqItem
import com.example.data.model.Game
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.OwnerOrderMessage
import com.example.data.model.Product
import com.example.data.model.StoreConfig
import com.example.data.model.UserProfile
import com.example.data.repository.StoreRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SalesAnalyticsSummary(
    val totalRevenueBdt: Long = 0L,
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val completedOrders: Int = 0,
    val rejectedOrders: Int = 0,
    val topProducts: List<Pair<String, Int>> = emptyList()
)

class OwnerViewModel(
    private val repository: StoreRepository
) : ViewModel() {

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _adminFeedback = MutableStateFlow<String?>(null)
    val adminFeedback: StateFlow<String?> = _adminFeedback.asStateFlow()

    private val _orderSearch = MutableStateFlow("")
    val orderSearch: StateFlow<String> = _orderSearch.asStateFlow()

    private val _orderStatusFilter = MutableStateFlow<OrderStatus?>(null)
    val orderStatusFilter: StateFlow<OrderStatus?> = _orderStatusFilter.asStateFlow()

    val allOrdersState: StateFlow<UiState<List<Order>>> = flow {
        repository.observeAllOrders().collect { emit(UiState.Success(it) as UiState<List<Order>>) }
    }.catch { error ->
        emit(unwrapError(error, "Failed to load all orders"))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val allCustomersState: StateFlow<UiState<List<UserProfile>>> = flow {
        repository.observeAllCustomers().collect { emit(UiState.Success(it) as UiState<List<UserProfile>>) }
    }.catch { error ->
        emit(unwrapError(error, "Failed to load customers"))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val ownerOrderMessagesState: StateFlow<UiState<List<OwnerOrderMessage>>> = flow {
        repository.observeOwnerOrderMessages().collect {
            emit(UiState.Success(it) as UiState<List<OwnerOrderMessage>>)
        }
    }.catch { error ->
        emit(unwrapError(error, "Failed to load customer order messages"))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    fun setOrderSearch(query: String) {
        _orderSearch.value = query
    }

    fun setOrderStatusFilter(status: OrderStatus?) {
        _orderStatusFilter.value = status
    }

    fun computeSalesAnalytics(orders: List<Order>): SalesAnalyticsSummary {
        val completed = orders.filter { it.status == OrderStatus.COMPLETED.name }
        val pending = orders.count {
            it.status == OrderStatus.PENDING.name ||
                it.status == OrderStatus.PAYMENT_VERIFICATION.name ||
                it.status == OrderStatus.PROCESSING.name
        }
        val rejected = orders.count { it.status == OrderStatus.REJECTED.name }
        val topProducts = completed.groupingBy { it.productName }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }

        return SalesAnalyticsSummary(
            totalRevenueBdt = completed.sumOf { it.totalAmountBdt },
            totalOrders = orders.size,
            pendingOrders = pending,
            completedOrders = completed.size,
            rejectedOrders = rejected,
            topProducts = topProducts
        )
    }

    fun updateOrderStatus(order: Order, status: OrderStatus, adminNote: String, storeConfig: StoreConfig) {
        runAction("Order ${order.orderCode} updated to ${status.label}") {
            repository.updateOrderStatus(order, status, adminNote, storeConfig)
        }
    }

    fun markOrderMessageRead(messageId: String) {
        viewModelScope.launch {
            repository.markOwnerOrderMessageRead(messageId)
        }
    }

    fun saveGame(game: Game) = runAction("Saved game: ${game.name}") { repository.saveGame(game) }

    fun deleteGame(gameId: String) = runAction("Deleted game") { repository.deleteGame(gameId) }

    fun saveCategory(category: Category) = runAction("Saved category: ${category.name}") { repository.saveCategory(category) }

    fun deleteCategory(categoryId: String) = runAction("Deleted category") { repository.deleteCategory(categoryId) }

    fun saveProduct(product: Product) = runAction("Saved product: ${product.name}") { repository.saveProduct(product) }

    fun deleteProduct(productId: String) = runAction("Deleted product") { repository.deleteProduct(productId) }

    fun saveBonusOffer(offer: BonusOffer) = runAction("Saved bonus offer: ${offer.title}") { repository.saveBonusOffer(offer) }

    fun deleteBonusOffer(offerId: String) = runAction("Deleted bonus offer") { repository.deleteBonusOffer(offerId) }

    fun saveFaq(faq: FaqItem) = runAction("Saved FAQ") { repository.saveFaq(faq) }

    fun deleteFaq(faqId: String) = runAction("Deleted FAQ") { repository.deleteFaq(faqId) }

    fun saveStoreConfig(config: StoreConfig) = runAction("Store settings updated") { repository.saveStoreConfig(config) }

    fun publishAnnouncement(title: String, message: String, customerUids: List<String>) =
        runAction("Announcement published to ${customerUids.size} customers") {
            repository.publishAnnouncement(title, message, customerUids)
        }

    fun adjustCustomerPoints(customer: UserProfile, pointsDelta: Long, reason: String, storeConfig: StoreConfig) =
        runAction("Updated points for ${customer.displayName}") {
            repository.adjustCustomerPoints(customer, pointsDelta, reason, storeConfig)
        }

    fun reseedDefaultCatalog() = runAction("Official Roblox catalog initialized in Firestore") {
        repository.forceSeedInitialCatalog()
    }

    fun clearFeedback() {
        _adminFeedback.value = null
    }

    private fun runAction(successMessage: String, block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = block()
            _isBusy.value = false
            result.onSuccess {
                _adminFeedback.value = successMessage
            }.onFailure { err ->
                _adminFeedback.value = err.localizedMessage ?: "Admin action failed."
            }
        }
    }

    private fun <T> unwrapError(error: Throwable, fallback: String): UiState<T> {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val root = firestoreError ?: error
        Log.w("OwnerVM", fallback, root)
        return UiState.Error(root.localizedMessage ?: fallback)
    }
}
