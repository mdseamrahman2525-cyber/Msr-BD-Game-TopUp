package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CategoryType
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtlePurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.StoreFormatters
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProductVisualBadge(
    imageUrl: String,
    categoryType: String,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val type = CategoryType.fromRaw(categoryType)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(
                when (type) {
                    CategoryType.CURRENCY -> DarkSurfaceElevated
                    CategoryType.SUBSCRIPTION -> Color(0xFF28243E)
                    CategoryType.GRAB -> DarkSurfaceElevated
                    CategoryType.OTHER -> DarkSurfaceElevated
                }
            )
            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Product artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Canvas(modifier = Modifier.size(size * 0.62f)) {
                val cx = this.size.width / 2f
                val cy = this.size.height / 2f
                val r = this.size.minDimension * 0.44f
                when (type) {
                    CategoryType.CURRENCY -> {
                        val hexPath = Path().apply {
                            for (i in 0 until 6) {
                                val angle = Math.toRadians((60 * i - 30).toDouble())
                                val x = cx + (r * cos(angle)).toFloat()
                                val y = cy + (r * sin(angle)).toFloat()
                                if (i == 0) moveTo(x, y) else lineTo(x, y)
                            }
                            close()
                        }
                        drawPath(
                            path = hexPath,
                            brush = Brush.linearGradient(listOf(GoldAccent, Color(0xFFF3D27A)))
                        )
                        drawPath(
                            path = hexPath,
                            color = Color(0xFFFFE7A0),
                            style = Stroke(width = 2f)
                        )
                        val sq = r * 0.45f
                        drawRoundRect(
                            color = PitchBlack,
                            topLeft = Offset(cx - sq / 2f, cy - sq / 2f),
                            size = Size(sq, sq),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                    CategoryType.SUBSCRIPTION -> {
                        val crown = Path().apply {
                            moveTo(cx - r, cy + r * 0.5f)
                            lineTo(cx - r, cy - r * 0.35f)
                            lineTo(cx - r * 0.38f, cy + r * 0.1f)
                            lineTo(cx, cy - r * 0.7f)
                            lineTo(cx + r * 0.38f, cy + r * 0.1f)
                            lineTo(cx + r, cy - r * 0.35f)
                            lineTo(cx + r, cy + r * 0.5f)
                            close()
                        }
                        drawPath(
                            path = crown,
                            brush = Brush.linearGradient(listOf(PureWhite, Color(0xFFC8C7E2)))
                        )
                    }
                    else -> {
                        val gem = Path().apply {
                            moveTo(cx, cy - r)
                            lineTo(cx + r * 0.85f, cy)
                            lineTo(cx, cy + r)
                            lineTo(cx - r * 0.85f, cy)
                            close()
                        }
                        drawPath(
                            path = gem,
                            brush = Brush.linearGradient(listOf(PureWhite, ElectricBlue))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RobuxPackageCard(
    product: Product,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = DarkCardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("product_card_${product.id}"),
        color = DarkSurface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ProductVisualBadge(
                    imageUrl = product.imageUrl,
                    categoryType = product.categoryType,
                    size = 48.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = product.subtitle.ifBlank { "Official Digital Top-Up" },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("fav_btn_${product.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) ErrorRed else TextMuted
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = StoreFormatters.formatBdt(product.priceBdt),
                        style = MaterialTheme.typography.headlineSmall.copy(fontFamily = MonoFontFamily),
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                    AvailabilityChip(available = product.available)
                }

                Button(
                    onClick = onBuyClick,
                    enabled = product.available,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = PureBlack,
                        disabledContainerColor = DarkSurfaceElevated,
                        disabledContentColor = TextMuted
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("buy_btn_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCartCheckout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (product.available) "Buy Now" else "Out of Stock",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SubscriptionPackageCard(
    product: Product,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = DarkCardBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("subscription_card_${product.id}"),
        color = DarkSurface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ProductVisualBadge(
                    imageUrl = product.imageUrl,
                    categoryType = CategoryType.SUBSCRIPTION.name,
                    size = 54.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = DarkSurfaceElevated,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = product.quantityLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite subscription",
                        tint = if (isFavorite) ErrorRed else TextMuted
                    )
                }
            }

            if (product.description.isNotBlank()) {
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = StoreFormatters.formatBdt(product.priceBdt),
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = MonoFontFamily),
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                    AvailabilityChip(available = product.available)
                }
                Button(
                    onClick = onBuyClick,
                    enabled = product.available,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = PureBlack,
                        disabledContainerColor = DarkSurfaceElevated,
                        disabledContentColor = TextMuted
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("buy_sub_${product.id}")
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (product.available) "Buy Now" else "Unavailable",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AvailabilityChip(available: Boolean) {
    val color = if (available) SuccessGreen else ErrorRed
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (available) "Available • Instant Queue" else "Currently Unavailable",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun OrderStatusChip(statusRaw: String, modifier: Modifier = Modifier) {
    val status = OrderStatus.fromRaw(statusRaw)
    val (bg, fg) = when (status) {
        OrderStatus.PENDING -> WarningAmber.copy(alpha = 0.16f) to WarningAmber
        OrderStatus.PAYMENT_VERIFICATION -> ElectricBlue.copy(alpha = 0.18f) to ElectricCyan
        OrderStatus.PROCESSING -> SubtlePurple.copy(alpha = 0.20f) to Color(0xFFD2CBFF)
        OrderStatus.COMPLETED -> SuccessGreen.copy(alpha = 0.18f) to SuccessGreen
        OrderStatus.REJECTED -> ErrorRed.copy(alpha = 0.18f) to ErrorRed
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.border(1.dp, fg.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
