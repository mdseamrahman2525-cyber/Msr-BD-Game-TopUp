package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class CategoryType(val label: String) {
    CURRENCY("Top-Up Packages"),
    SUBSCRIPTION("Subscriptions"),
    GRAB("GRAB"),
    OTHER("Other Products");

    companion object {
        fun fromRaw(raw: String?): CategoryType =
            entries.firstOrNull { it.name == raw } ?: CURRENCY
    }
}

enum class MembershipTier(val label: String) {
    STANDARD("Member"),
    SILVER("SILVER"),
    GOLD("GOLD"),
    PLATINUM("PLATINUM");

    companion object {
        fun fromRaw(raw: String?): MembershipTier =
            entries.firstOrNull { it.name == raw } ?: STANDARD

        fun computeTier(points: Long, silver: Long, gold: Long, platinum: Long): MembershipTier = when {
            points >= platinum -> PLATINUM
            points >= gold -> GOLD
            points >= silver -> SILVER
            else -> STANDARD
        }
    }
}

enum class OrderStatus(val label: String) {
    PENDING("Pending"),
    PAYMENT_VERIFICATION("Payment Verification"),
    PROCESSING("Processing"),
    COMPLETED("Done"),
    REJECTED("Rejected");

    companion object {
        fun fromRaw(raw: String?): OrderStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

enum class NotificationType {
    ORDER,
    OFFER,
    ANNOUNCEMENT;

    companion object {
        fun fromRaw(raw: String?): NotificationType =
            entries.firstOrNull { it.name == raw } ?: ANNOUNCEMENT
    }
}

@Keep
@IgnoreExtraProperties
data class StoreConfig(
    @DocumentId val id: String = "store_config",
    val bkashNumber: String = "01354246861",
    val nagadNumber: String = "01354246861",
    val paymentInstructions: String = "Open bKash or Nagad App -> Send Money -> Enter official number 01354246861 -> Enter exact Total Amount -> Copy Transaction ID and submit below.",
    val supportPhone: String = "01354246861",
    val whatsappCommunityUrl: String = "https://chat.whatsapp.com/HrVQr70upFb5fNqsTM5w32",
    val supportInfo: String = "Official MSR BD Game Top-Up Support Desk. Active daily 9:00 AM - 12:00 AM BST for payment verification & order fulfillment.",
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "We are performing a quick system upgrade to serve you faster. Please check back shortly.",
    val announcementBanner: String = "Welcome to MSR BD Game Top-Up! Official Roblox Robux & Plus Subscriptions with instant bKash verification.",
    val silverThreshold: Long = 1000L,
    val goldThreshold: Long = 5000L,
    val platinumThreshold: Long = 12300L,
    val rewardDescription: String = "Earn 1 Loyalty Point for every ৳1 of verified & completed purchases. Unlock Silver (1,000), Gold (5,000), and Platinum (12,300) VIP tiers.",
    val privacyPolicyText: String = "MSR BD Game Top-Up collects only your Google account display name, email, and game fulfillment identifier (such as Roblox Username) plus bKash payment reference needed to process orders. We NEVER ask for or store your Roblox, Google, or personal passwords.",
    val termsText: String = "All digital top-ups and subscriptions are processed after manual owner verification of your bKash Transaction ID. Only verified and completed orders earn loyalty points. Double-check your Roblox Username before submitting an order.",
    val appDownloadUrl: String = "https://ais-pre-oc3kvhkv733pmjlr6xalk4-836236406937.asia-northeast1.run.app",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Game(
    @DocumentId val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val themeColorHex: String = "#2997FF",
    val active: Boolean = true,
    val featured: Boolean = false,
    val displayOrder: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Category(
    @DocumentId val id: String = "",
    val gameId: String = "",
    val name: String = "",
    val description: String = "",
    val type: String = CategoryType.CURRENCY.name,
    val displayOrder: Long = 1L,
    val active: Boolean = true,
    val requiredInputFields: List<String> = listOf("Roblox Username"),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val gameId: String = "",
    val categoryId: String = "",
    val categoryType: String = CategoryType.CURRENCY.name,
    val name: String = "",
    val subtitle: String = "",
    val description: String = "",
    val quantityLabel: String = "",
    val amountValue: Long = 0L,
    val priceBdt: Long = 0L,
    val imageUrl: String = "",
    val available: Boolean = true,
    val active: Boolean = true,
    val featured: Boolean = false,
    val displayOrder: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BonusOffer(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val productId: String = "",
    val productName: String = "",
    val imageUrl: String = "",
    val originalPriceBdt: Long = 0L,
    val discountedPriceBdt: Long = 0L,
    val dropDayOfMonth: Long = 10L,
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FaqItem(
    @DocumentId val id: String = "",
    val question: String = "",
    val answer: String = "",
    val displayOrder: Long = 1L,
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Announcement(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val points: Long = 0L,
    val totalVerifiedSpending: Long = 0L,
    val membershipTier: String = MembershipTier.STANDARD.name,
    val role: String = "customer",
    val favoriteProductIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PointsEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val orderId: String = "",
    val pointsDelta: Long = 0L,
    val amountBdt: Long = 0L,
    val reason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = NotificationType.ORDER.name,
    val relatedOrderId: String = "",
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Order(
    @DocumentId val id: String = "",
    val orderCode: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val gameId: String = "",
    val gameName: String = "",
    val categoryName: String = "",
    val productId: String = "",
    val productName: String = "",
    val quantityLabel: String = "",
    val unitPriceBdt: Long = 0L,
    val totalAmountBdt: Long = 0L,
    val targetAccountLabel: String = "Roblox Username",
    val targetAccountInput: String = "",
    val paymentMethod: String = "bKash",
    val paymentRecipientNumber: String = "01354246861",
    val paymentSenderNumber: String = "",
    val transactionId: String = "",
    val paymentConfirmed: Boolean = false,
    val status: String = OrderStatus.PENDING.name,
    val adminNote: String = "",
    val pointsAwarded: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class OwnerOrderMessage(
    @DocumentId val id: String = "",
    val orderId: String = "",
    val orderCode: String = "",
    val customerUid: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val gameName: String = "",
    val productName: String = "",
    val totalAmountBdt: Long = 0L,
    val targetAccountLabel: String = "Roblox Username",
    val targetAccountInput: String = "",
    val paymentMethod: String = "bKash",
    val paymentSenderNumber: String = "",
    val transactionId: String = "",
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)
