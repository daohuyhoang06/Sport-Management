package com.sportmanagement.user.ui.components.chatbot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.ChatbotDeliveryState
import com.sportmanagement.user.domain.model.ChatbotMessage
import com.sportmanagement.user.domain.model.ChatbotSender
import com.sportmanagement.user.ui.state.ChatbotUiState
import com.sportmanagement.user.ui.theme.AppCardCornerRadius
import com.sportmanagement.user.ui.theme.AppCompactCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaWideHeight
import com.sportmanagement.user.ui.theme.AppFieldHorizontalPadding
import com.sportmanagement.user.ui.theme.AppFieldVerticalPadding
import com.sportmanagement.user.ui.theme.AppHeaderGradientEnd
import com.sportmanagement.user.ui.theme.AppHeaderGradientStart
import com.sportmanagement.user.ui.theme.AppPanelCornerRadius
import com.sportmanagement.user.ui.theme.AppSearchCornerRadius
import com.sportmanagement.user.ui.theme.AppScreenHorizontalPadding
import kotlin.math.roundToInt

@Composable
fun ChatbotOverlay(
    uiState: ChatbotUiState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onToggleWindow: () -> Unit,
    onCloseWindow: () -> Unit,
    onDraftChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onRetryMessage: (String) -> Unit,
    onDismissError: () -> Unit,
    onButtonAnchorChanged: (Float, Float) -> Unit
) {
    if (!uiState.isWidgetEnabled) return

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val isLandscape = maxWidth > maxHeight
        val sidePadding = AppScreenHorizontalPadding
        val topPadding = maxOf(contentPadding.calculateTopPadding(), AppScreenHorizontalPadding)
        val bottomPadding = maxOf(contentPadding.calculateBottomPadding(), AppScreenHorizontalPadding)
        val floatingButtonSize = AppCtaWideHeight + AppCompactCornerRadius
        val windowWidthFraction = if (isLandscape) 0.58f else 0.94f
        val minWindowHeight = if (isLandscape) maxHeight * 0.42f else maxHeight * 0.28f
        val maxWindowHeight = if (isLandscape) maxHeight * 0.78f else maxHeight * 0.54f
        val density = LocalDensity.current

        var localAnchorX by remember { mutableFloatStateOf(uiState.buttonAnchorX) }
        var localAnchorY by remember { mutableFloatStateOf(uiState.buttonAnchorY) }
        var isDragging by remember { mutableStateOf(false) }

        LaunchedEffect(uiState.buttonAnchorX, uiState.buttonAnchorY) {
            if (!isDragging) {
                localAnchorX = uiState.buttonAnchorX
                localAnchorY = uiState.buttonAnchorY
            }
        }

        val minXPx = with(density) { sidePadding.toPx() }
        val maxXPx = with(density) { (maxWidth - sidePadding - floatingButtonSize).toPx() }
        val minYPx = with(density) { topPadding.toPx() }
        val maxYPx = with(density) { (maxHeight - bottomPadding - floatingButtonSize).toPx() }
        val travelXPx = (maxXPx - minXPx).coerceAtLeast(0f)
        val travelYPx = (maxYPx - minYPx).coerceAtLeast(0f)

        val buttonOffsetX = minXPx + (travelXPx * localAnchorX)
        val buttonOffsetY = minYPx + (travelYPx * localAnchorY)

        AnimatedVisibility(
            visible = uiState.isWindowOpen,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 8 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 8 }),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    start = sidePadding,
                    end = sidePadding,
                    top = topPadding,
                    bottom = bottomPadding
                )
        ) {
            ChatbotWindow(
                state = uiState,
                modifier = Modifier
                    .fillMaxWidth(windowWidthFraction)
                    .widthIn(min = maxWidth * if (isLandscape) 0.34f else 0.72f)
                    .heightIn(min = minWindowHeight, max = maxWindowHeight),
                onCloseWindow = onCloseWindow,
                onDraftChanged = onDraftChanged,
                onSendMessage = onSendMessage,
                onRetryMessage = onRetryMessage,
                onDismissError = onDismissError
            )
        }

        AnimatedVisibility(
            visible = !uiState.isWindowOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ChatbotFloatingButton(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = buttonOffsetX.roundToInt(),
                            y = buttonOffsetY.roundToInt()
                        )
                    }
                    .pointerInput(travelXPx, travelYPx) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                onButtonAnchorChanged(localAnchorX, localAnchorY)
                            },
                            onDragCancel = {
                                isDragging = false
                                onButtonAnchorChanged(localAnchorX, localAnchorY)
                            }
                        ) { change, dragAmount ->
                            change.consume()

                            val nextAnchorX = if (travelXPx == 0f) {
                                0f
                            } else {
                                (localAnchorX + (dragAmount.x / travelXPx)).coerceIn(0f, 1f)
                            }
                            val nextAnchorY = if (travelYPx == 0f) {
                                0f
                            } else {
                                (localAnchorY + (dragAmount.y / travelYPx)).coerceIn(0f, 1f)
                            }

                            localAnchorX = nextAnchorX
                            localAnchorY = nextAnchorY
                        }
                    },
                buttonSize = floatingButtonSize,
                onClick = onToggleWindow,
                onLongClick = {}
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ChatbotFloatingButton(
    modifier: Modifier = Modifier,
    buttonSize: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "chatbot_fab_scale"
    )

    Surface(
        modifier = modifier
            .size(buttonSize)
            .clip(CircleShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        shadowElevation = if (isPressed) buttonSize * 0.14f else buttonSize * 0.3f,
        tonalElevation = buttonSize * 0.08f
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f))
                .padding(AppCompactCornerRadius)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AppHeaderGradientStart.copy(alpha = 0.72f),
                            AppHeaderGradientEnd.copy(alpha = 0.72f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.icons8_ai_chatting_50),
                contentDescription = stringResource(R.string.chatbot_toggle_content_description),
                modifier = Modifier
                    .size(buttonSize * 0.58f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.08f))
                    .padding(AppCompactCornerRadius)
                    .size(buttonSize * scale),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun ChatbotWindow(
    state: ChatbotUiState,
    modifier: Modifier = Modifier,
    onCloseWindow: () -> Unit,
    onDraftChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onRetryMessage: (String) -> Unit,
    onDismissError: () -> Unit
) {
    val listState = rememberLazyListState()
    val inputMinHeight = AppCtaWideHeight - AppFieldHorizontalPadding
    val sendButtonSize = inputMinHeight * 0.78f

    LaunchedEffect(state.messages.size, state.isLoading, state.isWindowOpen) {
        if (!state.isWindowOpen) return@LaunchedEffect
        val lastIndex = state.messages.lastIndex
        when {
            state.isLoading && lastIndex >= 0 -> listState.animateScrollToItem(lastIndex + 1)
            lastIndex >= 0 -> listState.animateScrollToItem(lastIndex)
        }
    }

    Surface(
        modifier = modifier.navigationBarsPadding(),
        shape = RoundedCornerShape(AppPanelCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 14.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(AppHeaderGradientStart, AppHeaderGradientEnd)
                        )
                    )
                    .padding(
                        horizontal = AppFieldHorizontalPadding,
                        vertical = AppFieldVerticalPadding
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
                        tonalElevation = 0.dp
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.icons8_ai_chatting_50),
                            contentDescription = null,
                            modifier = Modifier
                                .size(AppCtaWideHeight)
                                .padding(AppCompactCornerRadius),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.size(AppFieldHorizontalPadding))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppFieldVerticalPadding / 4)
                    ) {
                        Text(
                            text = stringResource(R.string.chatbot_window_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.chatbot_window_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
                        )
                    }
                    IconButton(onClick = onCloseWindow) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.chatbot_close_content_description),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    state.messages.isEmpty() && !state.isLoading -> {
                        ChatbotEmptyState(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = AppScreenHorizontalPadding)
                        )
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = AppFieldHorizontalPadding,
                                end = AppFieldHorizontalPadding,
                                top = AppFieldVerticalPadding,
                                bottom = (AppCtaWideHeight * 2)
                            ),
                            verticalArrangement = Arrangement.spacedBy(AppFieldVerticalPadding / 1.5f)
                        ) {
                            items(
                                items = state.messages,
                                key = { it.id }
                            ) { chatMessage ->
                                ChatMessageRow(
                                    message = chatMessage,
                                    onRetryMessage = { onRetryMessage(chatMessage.id) }
                                )
                            }

                            if (state.isLoading) {
                                item(key = "chatbot_loading") {
                                    ChatbotLoadingRow()
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(
                            horizontal = AppFieldHorizontalPadding,
                            vertical = AppFieldVerticalPadding
                        ),
                    verticalArrangement = Arrangement.spacedBy(AppFieldVerticalPadding / 2)
                ) {
                    AnimatedVisibility(visible = state.errorMessage != null) {
                        ChatbotErrorBanner(
                            message = state.errorMessage.orEmpty(),
                            onDismissError = onDismissError
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(AppSearchCornerRadius),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 10.dp,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(
                                    minHeight = inputMinHeight
                                )
                                .padding(
                                    start = AppFieldHorizontalPadding,
                                    end = AppCompactCornerRadius * 2,
                                    top = AppCompactCornerRadius,
                                    bottom = AppCompactCornerRadius
                                ),
                            horizontalArrangement = Arrangement.spacedBy(AppCompactCornerRadius),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (state.draftMessage.isBlank()) {
                                    Text(
                                        text = stringResource(R.string.chatbot_input_placeholder),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                BasicTextField(
                                    value = state.draftMessage,
                                    onValueChange = onDraftChanged,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { onSendMessage() }),
                                    enabled = !state.isLoading && !state.isTyping,
                                    singleLine = true
                                )
                            }

                            FilledIconButton(
                                onClick = onSendMessage,
                                modifier = Modifier
                                    .size(sendButtonSize)
                                    .align(Alignment.CenterVertically),
                                enabled = state.draftMessage.isNotBlank() && !state.isLoading && !state.isTyping
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = stringResource(R.string.chatbot_send_content_description),
                                    modifier = Modifier.size(sendButtonSize * 0.52f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatbotEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppFieldVerticalPadding)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 0.dp
        ) {
            Box(
                modifier = Modifier.padding(AppFieldHorizontalPadding),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.icons8_ai_chatting_50),
                    contentDescription = null,
                    modifier = Modifier
                        .size(AppCtaWideHeight - AppFieldHorizontalPadding)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Text(
            text = stringResource(R.string.chatbot_empty_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.chatbot_empty_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChatMessageRow(
    message: ChatbotMessage,
    onRetryMessage: () -> Unit
) {
    val isUser = message.sender == ChatbotSender.USER
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val label = if (isUser) {
        null
    } else {
        stringResource(R.string.chatbot_sender_bot)
    }
    val rowAlignment = if (isUser) Alignment.End else Alignment.Start
    val rowArrangement = if (isUser) Arrangement.End else Arrangement.Start
    val bubbleShape = if (isUser) {
        RoundedCornerShape(
            topStart = AppCardCornerRadius,
            topEnd = AppCardCornerRadius,
            bottomStart = AppCardCornerRadius,
            bottomEnd = AppCompactCornerRadius
        )
    } else {
        RoundedCornerShape(
            topStart = AppCardCornerRadius,
            topEnd = AppCardCornerRadius,
            bottomStart = AppCompactCornerRadius,
            bottomEnd = AppCardCornerRadius
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = rowAlignment
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = rowArrangement
        ) {
            Surface(
                shape = bubbleShape,
                color = bubbleColor,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = if (message.deliveryState == ChatbotDeliveryState.FAILED) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.72f))
                } else {
                    null
                }
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = AppFieldHorizontalPadding,
                        vertical = AppFieldVerticalPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppFieldVerticalPadding / 4)
                ) {
                    if (label != null) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.72f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }
            }
        }

        if (message.deliveryState == ChatbotDeliveryState.FAILED) {
            Row(
                modifier = Modifier.padding(top = AppFieldVerticalPadding / 4),
                horizontalArrangement = Arrangement.spacedBy(AppFieldHorizontalPadding / 2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.chatbot_failed_state),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(onClick = onRetryMessage, contentPadding = PaddingValues(0.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(AppFieldHorizontalPadding)
                    )
                    Spacer(modifier = Modifier.size(AppFieldHorizontalPadding / 2))
                    Text(text = stringResource(R.string.chatbot_retry))
                }
            }
        }
    }
}

@Composable
private fun ChatbotLoadingRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(AppCardCornerRadius),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = AppFieldHorizontalPadding,
                    vertical = AppFieldVerticalPadding
                ),
                horizontalArrangement = Arrangement.spacedBy(AppFieldHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(AppFieldHorizontalPadding + AppCompactCornerRadius),
                    strokeWidth = 2.dp
                )
                Text(
                    text = stringResource(R.string.chatbot_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChatbotErrorBanner(
    message: String,
    onDismissError: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(
                horizontal = AppFieldHorizontalPadding,
                vertical = AppFieldVerticalPadding
            ),
        horizontalArrangement = Arrangement.spacedBy(AppFieldHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
        TextButton(onClick = onDismissError, contentPadding = PaddingValues(0.dp)) {
            Text(
                text = stringResource(R.string.chatbot_error_dismiss),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
