package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Announcement
import com.example.data.model.AppNotification
import com.example.data.model.BonusOffer
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.FaqItem
import com.example.data.model.Game
import com.example.data.model.InitialCatalog
import com.example.data.model.MembershipTier
import com.example.data.model.NotificationType
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.OwnerOrderMessage
import com.example.data.model.PointsEntry
import com.example.data.model.Product
import com.example.data.model.StoreConfig
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class StoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in with Google.")

    fun observeStoreConfig(): Flow<StoreConfig> =
        db.collection("app_settings").document("store_config")
            .snapshots()
            .map { snap ->
                snap.toObject(StoreConfig::class.java, ESTIMATE) ?: InitialCatalog.defaultStoreConfig
            }

    fun observeGames(isOwner: Boolean): Flow<List<Game>> {
        val query = if (isOwner) db.collection("games")
        else db.collection("games").whereEqualTo("active", true)
        return query.snapshots().map { snap ->
            snap.toObjects(Game::class.java, ESTIMATE).sortedBy { it.displayOrder }
        }
    }

    fun observeCategories(isOwner: Boolean): Flow<List<Category>> {
        val query = if (isOwner) db.collection("categories")
        else db.collection("categories").whereEqualTo("active", true)
        return query.snapshots().map { snap ->
            snap.toObjects(Category::class.java, ESTIMATE).sortedBy { it.displayOrder }
        }
    }

    fun observeProducts(isOwner: Boolean): Flow<List<Product>> {
        val query = if (isOwner) db.collection("products")
        else db.collection("products").whereEqualTo("active", true)
        return query.snapshots().map { snap ->
            snap.toObjects(Product::class.java, ESTIMATE).sortedBy { it.displayOrder }
        }
    }

    fun observeBonusOffers(isOwner: Boolean): Flow<List<BonusOffer>> {
        val query = if (isOwner) db.collection("bonus_offers")
        else db.collection("bonus_offers").whereEqualTo("active", true)
        return query.snapshots().map { snap ->
            snap.toObjects(BonusOffer::class.java, ESTIMATE)
                .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
    }

    fun observeFaqs(isOwner: Boolean): Flow<List<FaqItem>> {
        val query = if (isOwner) db.collection("faqs")
        else db.collection("faqs").whereEqualTo("active", true)
        return query.snapshots().map { snap ->
            snap.toObjects(FaqItem::class.java, ESTIMATE).sortedBy { it.displayOrder }
        }
    }

    fun observeAnnouncements(isOwner: Boolean): Flow<List<Announcement>> {
        val query = if (isOwner) db.collection("announcements")
        else db.collection("announcements").whereEqualTo("active", true)
        return query.snapshots().map { snap ->
            snap.toObjects(Announcement::class.java, ESTIMATE)
                .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> =
        db.collection("users").document(userId)
            .snapshots()
            .map { snap -> snap.toObject(UserProfile::class.java, ESTIMATE) }

    fun observeUserOrders(userId: String): Flow<List<Order>> =
        db.collection("orders")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(Order::class.java, ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }

    fun observeAllOrders(): Flow<List<Order>> =
        db.collection("orders")
            .snapshots()
            .map { snap ->
                snap.toObjects(Order::class.java, ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }

    fun observeAllCustomers(): Flow<List<UserProfile>> =
        db.collection("users")
            .snapshots()
            .map { snap ->
                snap.toObjects(UserProfile::class.java, ESTIMATE)
                    .sortedByDescending { it.totalVerifiedSpending }
            }

    fun observePointsHistory(userId: String): Flow<List<PointsEntry>> =
        db.collection("users").document(userId).collection("points_history")
            .snapshots()
            .map { snap ->
                snap.toObjects(PointsEntry::class.java, ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }

    fun observeNotifications(userId: String): Flow<List<AppNotification>> =
        db.collection("users").document(userId).collection("notifications")
            .snapshots()
            .map { snap ->
                snap.toObjects(AppNotification::class.java, ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }

    fun observeOwnerOrderMessages(): Flow<List<OwnerOrderMessage>> =
        db.collection("owner_order_messages")
            .snapshots()
            .map { snap ->
                snap.toObjects(OwnerOrderMessage::class.java, ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }

    suspend fun verifyOwnerAdminRecord(uid: String): Boolean {
        return try {
            val user = auth.currentUser ?: return false
            if (GoogleAuthManager.isVerifiedOwnerUser(user)) return true
            val doc = db.collection("admins").document(uid).get().await()
            doc.exists()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    suspend fun ensureUserProfileAndSeedIfOwner(user: FirebaseUser, isOwner: Boolean): Result<Unit> {
        val docPath = "users/${user.uid}"
        return try {
            val userRef = db.collection("users").document(user.uid)
            val existing = userRef.get().await()
            val safeName = (user.displayName ?: "MSR Gamer").take(100)
            val safeEmail = (user.email ?: "").take(200)
            val safePhoto = (user.photoUrl?.toString() ?: "").take(1000)

            if (!existing.exists()) {
                val payload = mapOf(
                    "userId" to user.uid,
                    "displayName" to safeName,
                    "email" to safeEmail,
                    "photoUrl" to safePhoto,
                    "points" to 0L,
                    "totalVerifiedSpending" to 0L,
                    "membershipTier" to MembershipTier.STANDARD.name,
                    "role" to "customer",
                    "favoriteProductIds" to emptyList<String>(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userRef.set(payload).await()
            } else {
                userRef.update(
                    mapOf(
                        "displayName" to safeName,
                        "photoUrl" to safePhoto,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            if (isOwner) {
                seedInitialCatalogIfEmpty().getOrThrow()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun seedInitialCatalogIfEmpty(): Result<Unit> {
        return try {
            val gamesSnap = db.collection("games").limit(1).get().await()
            if (!gamesSnap.isEmpty) return Result.success(Unit)
            forceSeedInitialCatalog()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "games")
            Result.failure(e)
        }
    }

    suspend fun forceSeedInitialCatalog(): Result<Unit> {
        return try {
            val batch = db.batch()
            val cfg = InitialCatalog.defaultStoreConfig
            batch.set(
                db.collection("app_settings").document("store_config"),
                mapOf(
                    "bkashNumber" to cfg.bkashNumber,
                    "nagadNumber" to cfg.nagadNumber,
                    "paymentInstructions" to cfg.paymentInstructions,
                    "supportPhone" to cfg.supportPhone,
                    "whatsappCommunityUrl" to cfg.whatsappCommunityUrl,
                    "supportInfo" to cfg.supportInfo,
                    "maintenanceMode" to cfg.maintenanceMode,
                    "maintenanceMessage" to cfg.maintenanceMessage,
                    "announcementBanner" to cfg.announcementBanner,
                    "silverThreshold" to cfg.silverThreshold,
                    "goldThreshold" to cfg.goldThreshold,
                    "platinumThreshold" to cfg.platinumThreshold,
                    "rewardDescription" to cfg.rewardDescription,
                    "privacyPolicyText" to cfg.privacyPolicyText,
                    "termsText" to cfg.termsText,
                    "appDownloadUrl" to cfg.appDownloadUrl,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            InitialCatalog.defaultGames.forEach { g ->
                batch.set(db.collection("games").document(g.id), gamePayload(g, isCreate = true))
            }
            InitialCatalog.defaultCategories.forEach { c ->
                batch.set(db.collection("categories").document(c.id), categoryPayload(c, isCreate = true))
            }
            InitialCatalog.defaultProducts.forEach { p ->
                batch.set(db.collection("products").document(p.id), productPayload(p, isCreate = true))
            }
            InitialCatalog.defaultFaqs.forEach { f ->
                batch.set(db.collection("faqs").document(f.id), faqPayload(f, isCreate = true))
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "seed_catalog")
            Result.failure(e)
        }
    }

    suspend fun toggleFavoriteProduct(userId: String, productId: String, currentFavorites: List<String>): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Unauthorized favorite mutation" }
            val updated = if (currentFavorites.contains(productId)) {
                currentFavorites.filterNot { it == productId }
            } else {
                (currentFavorites + productId).take(50)
            }
            db.collection("users").document(uid).update(
                mapOf(
                    "favoriteProductIds" to updated,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun submitOrder(
        game: Game,
        categoryName: String,
        product: Product,
        overridePriceBdt: Long?,
        targetAccountLabel: String,
        targetAccountInput: String,
        paymentMethod: String,
        paymentRecipientNumber: String,
        paymentSenderNumber: String,
        transactionId: String
    ): Result<Order> {
        val docRef = db.collection("orders").document()
        return try {
            val user = auth.currentUser ?: throw IllegalStateException("Please sign in with Google first.")
            val uid = user.uid
            val datePart = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val randPart = Random.nextInt(1000, 9999)
            val orderCode = "MSR-$datePart-$randPart"
            val finalPrice = (overridePriceBdt ?: product.priceBdt).coerceAtLeast(1L)
            val cleanMethod = if (paymentMethod.equals("Nagad", ignoreCase = true)) "Nagad" else "bKash"

            val payload = mapOf(
                "orderCode" to orderCode,
                "userId" to uid,
                "customerName" to (user.displayName ?: "MSR Customer").take(100),
                "customerEmail" to (user.email ?: "").take(200),
                "gameId" to game.id.ifBlank { InitialCatalog.GAME_ROBLOX_ID },
                "gameName" to game.name.ifBlank { "Roblox" }.take(80),
                "categoryName" to categoryName.ifBlank { "Roblox Top-Up" }.take(80),
                "productId" to product.id,
                "productName" to product.name.take(100),
                "quantityLabel" to product.quantityLabel.ifBlank { product.name }.take(60),
                "unitPriceBdt" to finalPrice,
                "totalAmountBdt" to finalPrice,
                "targetAccountLabel" to targetAccountLabel.ifBlank { "Roblox Username" }.take(60),
                "targetAccountInput" to targetAccountInput.trim().take(120),
                "paymentMethod" to cleanMethod,
                "paymentRecipientNumber" to paymentRecipientNumber.trim().take(30),
                "paymentSenderNumber" to paymentSenderNumber.trim().take(30),
                "transactionId" to transactionId.trim().take(60),
                "paymentConfirmed" to true,
                "status" to OrderStatus.PENDING.name,
                "adminNote" to "",
                "pointsAwarded" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val batch = db.batch()
            batch.set(docRef, payload)

            val notifRef = db.collection("users").document(uid).collection("notifications").document()
            batch.set(
                notifRef,
                mapOf(
                    "userId" to uid,
                    "title" to "Order Submitted ($orderCode)",
                    "message" to "Your order for ${product.name} (৳$finalPrice) is Pending $cleanMethod verification.",
                    "type" to NotificationType.ORDER.name,
                    "relatedOrderId" to docRef.id,
                    "read" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            val ownerMsgRef = db.collection("owner_order_messages").document(docRef.id)
            batch.set(
                ownerMsgRef,
                mapOf(
                    "orderId" to docRef.id,
                    "orderCode" to orderCode,
                    "customerUid" to uid,
                    "customerName" to (user.displayName ?: "MSR Customer").take(100),
                    "customerEmail" to (user.email ?: "").take(200),
                    "gameName" to game.name.ifBlank { "Roblox" }.take(80),
                    "productName" to product.name.take(100),
                    "totalAmountBdt" to finalPrice,
                    "targetAccountLabel" to targetAccountLabel.ifBlank { "Roblox Username" }.take(60),
                    "targetAccountInput" to targetAccountInput.trim().take(120),
                    "paymentMethod" to cleanMethod,
                    "paymentSenderNumber" to paymentSenderNumber.trim().take(30),
                    "transactionId" to transactionId.trim().take(60),
                    "read" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()

            Result.success(
                Order(
                    id = docRef.id,
                    orderCode = orderCode,
                    userId = uid,
                    customerName = user.displayName ?: "MSR Customer",
                    customerEmail = user.email ?: "",
                    gameId = game.id,
                    gameName = game.name,
                    categoryName = categoryName,
                    productId = product.id,
                    productName = product.name,
                    quantityLabel = product.quantityLabel,
                    unitPriceBdt = finalPrice,
                    totalAmountBdt = finalPrice,
                    targetAccountLabel = targetAccountLabel,
                    targetAccountInput = targetAccountInput.trim(),
                    paymentMethod = cleanMethod,
                    paymentRecipientNumber = paymentRecipientNumber.trim(),
                    paymentSenderNumber = paymentSenderNumber.trim(),
                    transactionId = transactionId.trim(),
                    paymentConfirmed = true,
                    status = OrderStatus.PENDING.name,
                    adminNote = "",
                    pointsAwarded = false
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(userId: String, notificationId: String): Result<Unit> {
        val path = "users/$userId/notifications/$notificationId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            db.document(path).update("read", true).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun markOwnerOrderMessageRead(messageId: String): Result<Unit> {
        val path = "owner_order_messages/$messageId"
        return try {
            requireUserId()
            db.document(path).update("read", true).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteAccountProfile(userId: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            db.document(path).delete().await()
            auth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- OWNER ADMIN OPERATIONS ---

    suspend fun updateOrderStatus(
        order: Order,
        newStatus: OrderStatus,
        adminNote: String,
        storeConfig: StoreConfig
    ): Result<Unit> {
        val orderRef = db.collection("orders").document(order.id)
        val userRef = db.collection("users").document(order.userId)
        val notifRef = userRef.collection("notifications").document()
        val pointsRef = userRef.collection("points_history").document()

        return try {
            requireUserId()
            val cleanNote = adminNote.trim().take(500)
            db.runTransaction { tx ->
                val orderSnap = tx.get(orderRef)
                val alreadyAwarded = orderSnap.getBoolean("pointsAwarded") ?: order.pointsAwarded
                val shouldAwardNow = newStatus == OrderStatus.COMPLETED && !alreadyAwarded

                var currentPoints = 0L
                var currentSpend = 0L
                if (shouldAwardNow) {
                    val userSnap = tx.get(userRef)
                    currentPoints = userSnap.getLong("points") ?: 0L
                    currentSpend = userSnap.getLong("totalVerifiedSpending") ?: 0L
                }

                tx.update(
                    orderRef,
                    mapOf(
                        "status" to newStatus.name,
                        "adminNote" to cleanNote,
                        "pointsAwarded" to (alreadyAwarded || shouldAwardNow),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                if (shouldAwardNow) {
                    val earned = order.totalAmountBdt.coerceAtLeast(0L)
                    val newPoints = currentPoints + earned
                    val newSpend = currentSpend + earned
                    val newTier = MembershipTier.computeTier(
                        newPoints,
                        storeConfig.silverThreshold,
                        storeConfig.goldThreshold,
                        storeConfig.platinumThreshold
                    ).name

                    tx.update(
                        userRef,
                        mapOf(
                            "points" to newPoints,
                            "totalVerifiedSpending" to newSpend,
                            "membershipTier" to newTier,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    tx.set(
                        pointsRef,
                        mapOf(
                            "userId" to order.userId,
                            "orderId" to order.id,
                            "pointsDelta" to earned,
                            "amountBdt" to order.totalAmountBdt,
                            "reason" to "Verified order ${order.orderCode} (${order.productName})",
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                val notifTitle = when (newStatus) {
                    OrderStatus.PAYMENT_VERIFICATION -> "Payment Under Verification"
                    OrderStatus.PROCESSING -> "Order Processing"
                    OrderStatus.COMPLETED -> "Order Completed!"
                    OrderStatus.REJECTED -> "Order Rejected"
                    OrderStatus.PENDING -> "Order Status Updated"
                }
                val notifMsg = when (newStatus) {
                    OrderStatus.COMPLETED -> "Your order ${order.orderCode} for ${order.productName} is complete! You earned ${order.totalAmountBdt} loyalty points."
                    OrderStatus.REJECTED -> "Your order ${order.orderCode} was rejected.${if (cleanNote.isNotBlank()) " Reason: $cleanNote" else ""}"
                    else -> "Order ${order.orderCode} status updated to ${newStatus.label}."
                }
                tx.set(
                    notifRef,
                    mapOf(
                        "userId" to order.userId,
                        "title" to notifTitle,
                        "message" to notifMsg.take(500),
                        "type" to NotificationType.ORDER.name,
                        "relatedOrderId" to order.id,
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            runCatching {
                db.collection("owner_order_messages").document(order.id).update("read", true).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, orderRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveGame(game: Game): Result<Unit> {
        val docRef = if (game.id.isBlank()) db.collection("games").document() else db.collection("games").document(game.id)
        return try {
            requireUserId()
            val isNew = game.id.isBlank() || game.createdAt == null
            if (isNew) {
                docRef.set(gamePayload(game, isCreate = true)).await()
            } else {
                docRef.update(gamePayload(game, isCreate = false)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteGame(gameId: String): Result<Unit> = deleteDoc("games", gameId)

    suspend fun saveCategory(category: Category): Result<Unit> {
        val docRef = if (category.id.isBlank()) db.collection("categories").document() else db.collection("categories").document(category.id)
        return try {
            requireUserId()
            val isNew = category.id.isBlank() || category.createdAt == null
            if (isNew) {
                docRef.set(categoryPayload(category, isCreate = true)).await()
            } else {
                docRef.update(categoryPayload(category, isCreate = false)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> = deleteDoc("categories", categoryId)

    suspend fun saveProduct(product: Product): Result<Unit> {
        val docRef = if (product.id.isBlank()) db.collection("products").document() else db.collection("products").document(product.id)
        return try {
            requireUserId()
            val isNew = product.id.isBlank() || product.createdAt == null
            if (isNew) {
                docRef.set(productPayload(product, isCreate = true)).await()
            } else {
                docRef.update(productPayload(product, isCreate = false)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = deleteDoc("products", productId)

    suspend fun saveBonusOffer(offer: BonusOffer): Result<Unit> {
        val docRef = if (offer.id.isBlank()) db.collection("bonus_offers").document() else db.collection("bonus_offers").document(offer.id)
        return try {
            requireUserId()
            val isNew = offer.id.isBlank() || offer.createdAt == null
            val base = mutableMapOf<String, Any>(
                "title" to offer.title.trim().take(100),
                "description" to offer.description.trim().take(600),
                "productId" to offer.productId.take(128),
                "productName" to offer.productName.trim().take(100),
                "imageUrl" to offer.imageUrl.trim().take(1000),
                "originalPriceBdt" to offer.originalPriceBdt.coerceAtLeast(1L),
                "discountedPriceBdt" to offer.discountedPriceBdt.coerceAtLeast(1L),
                "dropDayOfMonth" to if (offer.dropDayOfMonth in listOf(10L, 20L, 30L)) offer.dropDayOfMonth else 10L,
                "startDate" to (offer.startDate ?: Timestamp.now()),
                "endDate" to (offer.endDate ?: Timestamp(Date(System.currentTimeMillis() + 86400000L))),
                "active" to offer.active,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (isNew) {
                base["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(base).await()
            } else {
                docRef.update(base).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteBonusOffer(offerId: String): Result<Unit> = deleteDoc("bonus_offers", offerId)

    suspend fun saveFaq(faq: FaqItem): Result<Unit> {
        val docRef = if (faq.id.isBlank()) db.collection("faqs").document() else db.collection("faqs").document(faq.id)
        return try {
            requireUserId()
            val isNew = faq.id.isBlank() || faq.createdAt == null
            if (isNew) {
                docRef.set(faqPayload(faq, isCreate = true)).await()
            } else {
                docRef.update(faqPayload(faq, isCreate = false)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteFaq(faqId: String): Result<Unit> = deleteDoc("faqs", faqId)

    suspend fun publishAnnouncement(title: String, message: String, customerUids: List<String>): Result<Unit> {
        val docRef = db.collection("announcements").document()
        return try {
            requireUserId()
            val cleanTitle = title.trim().take(120)
            val cleanMsg = message.trim().take(1000)
            val batch = db.batch()
            batch.set(
                docRef,
                mapOf(
                    "title" to cleanTitle,
                    "message" to cleanMsg,
                    "active" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            customerUids.take(200).forEach { targetUid ->
                val notifRef = db.collection("users").document(targetUid).collection("notifications").document()
                batch.set(
                    notifRef,
                    mapOf(
                        "userId" to targetUid,
                        "title" to cleanTitle,
                        "message" to cleanMsg.take(500),
                        "type" to NotificationType.ANNOUNCEMENT.name,
                        "relatedOrderId" to "",
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveStoreConfig(config: StoreConfig): Result<Unit> {
        val docRef = db.collection("app_settings").document("store_config")
        return try {
            requireUserId()
            docRef.set(
                mapOf(
                    "bkashNumber" to config.bkashNumber.trim().take(30),
                    "nagadNumber" to config.nagadNumber.trim().take(30),
                    "paymentInstructions" to config.paymentInstructions.trim().take(1000),
                    "supportPhone" to config.supportPhone.trim().take(30),
                    "whatsappCommunityUrl" to config.whatsappCommunityUrl.trim().take(500),
                    "supportInfo" to config.supportInfo.trim().take(1000),
                    "maintenanceMode" to config.maintenanceMode,
                    "maintenanceMessage" to config.maintenanceMessage.trim().take(500),
                    "announcementBanner" to config.announcementBanner.trim().take(500),
                    "silverThreshold" to config.silverThreshold.coerceAtLeast(0L),
                    "goldThreshold" to config.goldThreshold.coerceAtLeast(0L),
                    "platinumThreshold" to config.platinumThreshold.coerceAtLeast(0L),
                    "rewardDescription" to config.rewardDescription.trim().take(1000),
                    "privacyPolicyText" to config.privacyPolicyText.trim().take(4000),
                    "termsText" to config.termsText.trim().take(4000),
                    "appDownloadUrl" to config.appDownloadUrl.trim().take(500),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun adjustCustomerPoints(
        customer: UserProfile,
        pointsDelta: Long,
        reason: String,
        storeConfig: StoreConfig
    ): Result<Unit> {
        val userRef = db.collection("users").document(customer.userId)
        val entryRef = userRef.collection("points_history").document()
        return try {
            requireUserId()
            val newPoints = (customer.points + pointsDelta).coerceAtLeast(0L)
            val newTier = MembershipTier.computeTier(
                newPoints,
                storeConfig.silverThreshold,
                storeConfig.goldThreshold,
                storeConfig.platinumThreshold
            ).name
            val batch = db.batch()
            batch.update(
                userRef,
                mapOf(
                    "points" to newPoints,
                    "membershipTier" to newTier,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                entryRef,
                mapOf(
                    "userId" to customer.userId,
                    "orderId" to "",
                    "pointsDelta" to pointsDelta,
                    "amountBdt" to 0L,
                    "reason" to reason.trim().ifBlank { "Owner Reward Adjustment" }.take(200),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            Result.failure(e)
        }
    }

    private suspend fun deleteDoc(collection: String, id: String): Result<Unit> {
        val path = "$collection/$id"
        return try {
            requireUserId()
            require(id.isNotBlank())
            db.collection(collection).document(id).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    private fun gamePayload(g: Game, isCreate: Boolean): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "name" to g.name.trim().take(80),
            "slug" to g.slug.ifBlank { g.name.lowercase(Locale.US).replace(" ", "-") }.take(80),
            "description" to g.description.trim().take(500),
            "imageUrl" to g.imageUrl.trim().take(1000),
            "themeColorHex" to g.themeColorHex.ifBlank { "#2997FF" }.take(16),
            "active" to g.active,
            "featured" to g.featured,
            "displayOrder" to g.displayOrder.coerceAtLeast(0L),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isCreate) map["createdAt"] = FieldValue.serverTimestamp()
        return map
    }

    private fun categoryPayload(c: Category, isCreate: Boolean): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "gameId" to c.gameId.take(128),
            "name" to c.name.trim().take(80),
            "description" to c.description.trim().take(400),
            "type" to CategoryType.fromRaw(c.type).name,
            "displayOrder" to c.displayOrder.coerceAtLeast(0L),
            "active" to c.active,
            "requiredInputFields" to c.requiredInputFields.filter { it.isNotBlank() }.take(6).ifEmpty { listOf("Roblox Username") },
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isCreate) map["createdAt"] = FieldValue.serverTimestamp()
        return map
    }

    private fun productPayload(p: Product, isCreate: Boolean): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "gameId" to p.gameId.take(128),
            "categoryId" to p.categoryId.take(128),
            "categoryType" to CategoryType.fromRaw(p.categoryType).name,
            "name" to p.name.trim().take(100),
            "subtitle" to p.subtitle.trim().take(150),
            "description" to p.description.trim().take(600),
            "quantityLabel" to p.quantityLabel.ifBlank { p.name }.trim().take(60),
            "amountValue" to p.amountValue.coerceAtLeast(0L),
            "priceBdt" to p.priceBdt.coerceAtLeast(1L),
            "imageUrl" to p.imageUrl.trim().take(1000),
            "available" to p.available,
            "active" to p.active,
            "featured" to p.featured,
            "displayOrder" to p.displayOrder.coerceAtLeast(0L),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isCreate) map["createdAt"] = FieldValue.serverTimestamp()
        return map
    }

    private fun faqPayload(f: FaqItem, isCreate: Boolean): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "question" to f.question.trim().take(300),
            "answer" to f.answer.trim().take(1500),
            "displayOrder" to f.displayOrder.coerceAtLeast(0L),
            "active" to f.active,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isCreate) map["createdAt"] = FieldValue.serverTimestamp()
        return map
    }
}
