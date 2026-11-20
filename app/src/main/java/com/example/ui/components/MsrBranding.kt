package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.ceil

/**
 * Reusable Global Header Component displaying Image 2 (msr_global_header) consistently
 * across EVERY application page without distortion, stretching, or cropping.
 */
@Composable
fun MsrGlobalHeader(
    modifier: Modifier = Modifier,
    pageTitle: String? = null,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    unreadNotificationCount: Int = 0,
    onNotificationsClick: (() -> Unit)? = null,
    isOwner: Boolean = false,
    onOwnerDashboardClick: (() -> Unit)? = null,
    barColor: Color = DarkSurface
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PitchBlack)
            .statusBarsPadding()
    ) {
        // Official Image 2 horizontal banner — exact 1200:201 aspect ratio, zero crop/stretch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PitchBlack)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.msr_global_header),
                contentDescription = stringResource(id = R.string.cd_global_header),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1200f / 201f)
            )
        }

        // Compact contextual action bar (Back / Page Label / Notifications / Owner shortcut)
        if (showBackButton || pageTitle != null || onNotificationsClick != null || (isOwner && onOwnerDashboardClick != null)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(barColor)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButton && onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("header_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (pageTitle != null) {
                    Text(
                        text = pageTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (isOwner && onOwnerDashboardClick != null) {
                    Surface(
                        color = GoldAccent.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(onClick = onOwnerDashboardClick)
                            .testTag("header_owner_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = "Owner Dashboard",
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Owner",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (onNotificationsClick != null) {
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("header_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationCount > 0) {
                                    Badge(containerColor = PureWhite, contentColor = PitchBlack) {
                                        Text(
                                            text = if (unreadNotificationCount > 9) "9+" else "$unreadNotificationCount",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (unreadNotificationCount > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = if (unreadNotificationCount > 0) PureWhite else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = DarkCardBorder.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun MsrLogoImage(
    size: Dp = 96.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.msr_logo),
        contentDescription = stringResource(id = R.string.cd_official_logo),
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(
                width = 1.5.dp,
                color = DarkCardBorder,
                shape = CircleShape
            )
    )
}

/**
 * Scrollable container with a calm #141320/#201F31 dark slate backdrop and subtle
 * scrolling artwork texture (toned down so the interface stays restful and clean).
 */
@Composable
fun MsrScrollableColumn(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    usePureBlackBg: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val bgPainter = painterResource(id = R.drawable.msr_scroll_bg)
    val baseBg = if (usePureBlackBg) PitchBlack else DeepNavyBg

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBg)
            .verticalScroll(scrollState)
    ) {
        if (!usePureBlackBg) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .matchParentSize()
                    .clipToBounds()
            ) {
                val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
                val tileHeightPx = widthPx * (1280f / 720f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(DeepNavyBg)
                    if (tileHeightPx > 0f && size.height > 0f) {
                        val tileCount = ceil(size.height / tileHeightPx).toInt().coerceAtLeast(1)
                        for (i in 0 until tileCount) {
                            translate(top = i * tileHeightPx) {
                                with(bgPainter) {
                                    draw(size = Size(size.width, tileHeightPx), alpha = 0.06f)
                                }
                            }
                        }
                    }
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                DeepNavyBg.copy(alpha = 0.88f),
                                DarkSurface.copy(alpha = 0.82f),
                                DeepNavyBg.copy(alpha = 0.90f)
                            )
                        )
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = contentPadding, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

/**
 * LazyList backdrop that translates cleanly in calm #201F31 slate tones.
 */
@Composable
fun MsrScrollingBackdropForLazyList(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val bgPainter = painterResource(id = R.drawable.msr_scroll_bg)
    val scrollOffsetPx by remember {
        derivedStateOf {
            (listState.firstVisibleItemIndex * 240f) + listState.firstVisibleItemScrollOffset
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        drawRect(DeepNavyBg)
        val tileHeightPx = size.width * (1280f / 720f)
        if (tileHeightPx > 0f) {
            val wrappedOffset = scrollOffsetPx % tileHeightPx
            val tilesNeeded = ceil((size.height + wrappedOffset) / tileHeightPx).toInt() + 1
            for (i in 0 until tilesNeeded) {
                val topY = (i * tileHeightPx) - wrappedOffset
                translate(top = topY) {
                    with(bgPainter) {
                        draw(size = Size(size.width, tileHeightPx), alpha = 0.06f)
                    }
                }
            }
        }
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    DeepNavyBg.copy(alpha = 0.88f),
                    DarkSurface.copy(alpha = 0.82f),
                    DeepNavyBg.copy(alpha = 0.90f)
                )
            )
        )
    }
}
