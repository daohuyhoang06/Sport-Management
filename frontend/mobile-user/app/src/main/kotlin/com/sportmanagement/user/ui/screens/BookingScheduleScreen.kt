package com.sportmanagement.user.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportmanagement.user.R
import com.sportmanagement.user.data.remote.api.UserApi
import com.sportmanagement.user.domain.model.BookingConfirmationData
import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.BookingSubCourt
import com.sportmanagement.user.domain.model.BookingTimeGridData
import com.sportmanagement.user.domain.model.BookingTimeRange
import com.sportmanagement.user.domain.model.MatchPostPreview
import com.sportmanagement.user.ui.components.booking.BookingBottomActionBar
import com.sportmanagement.user.ui.components.booking.BookingHeaderSection
import com.sportmanagement.user.ui.components.booking.BookingTimeGrid
import com.sportmanagement.user.ui.AppNavigationBarEffect
import com.sportmanagement.user.ui.components.AppRotatingLoadingIndicator
import com.sportmanagement.user.ui.theme.SportUserTheme
import com.sportmanagement.user.ui.theme.AppCardCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.viewmodel.BookingScheduleViewModel
import kotlinx.coroutines.launch
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScheduleScreen(
    fieldId: Int,
    initialDateText: String,
    sessionKey: Int = 0,
    onBackClick: () -> Unit,
    onNextClick: (BookingConfirmationData, FindOpponentDraft?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookingScheduleViewModel = rememberBookingScheduleViewModel(
        fieldId = fieldId,
        initialDateText = initialDateText,
        sessionKey = sessionKey
    )
) {
    val context = LocalContext.current
    val userApi = remember { UserApi() }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val selectSlotError = stringResource(R.string.booking_select_slot_error)
    val lowerBackgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f)
    val bottomBarColor = MaterialTheme.colorScheme.primary
    var selectedMatchPost by remember { mutableStateOf<MatchPostPreview?>(null) }
    var showMatchDetailDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var requestTeamName by remember { mutableStateOf("") }
    var requestPlayerCount by remember { mutableStateOf("") }
    var requestMessage by remember { mutableStateOf("") }
    var isSubmittingMatchRequest by remember { mutableStateOf(false) }
    var pendingConfirmationData by remember { mutableStateOf<BookingConfirmationData?>(null) }
    var showBookingModeDialog by remember { mutableStateOf(false) }
    var showFindOpponentFormDialog by remember { mutableStateOf(false) }
    var opponentTeamName by remember { mutableStateOf("") }
    var opponentPlayerCount by remember { mutableStateOf("") }
    var opponentDescription by remember { mutableStateOf("") }
    var opponentLevel by remember { mutableStateOf("BEGINNER") }

    AppNavigationBarEffect(
        navigationBarColor = bottomBarColor,
        useDarkIcons = bottomBarColor.luminance() > 0.5f
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = lowerBackgroundColor,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BookingBottomActionBar(
                sliderValue = uiState.sliderValue,
                onSliderChange = viewModel::onSliderChange,
                summary = uiState.summary,
                showSelectedRange = uiState.showSelectedRange,
                onToggleSelectedRange = viewModel::onToggleSelectedRangeVisibility,
                hasSelection = uiState.summary != null,
                onNextClick = {
                    viewModel.buildConfirmationData()?.let {
                        pendingConfirmationData = it
                        showBookingModeDialog = true
                    }
                },
                onRequireSelection = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(selectSlotError)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                BookingHeaderSection(
                    selectedDateText = uiState.selectedDateText,
                    onBackClick = onBackClick,
                    onDateClick = { viewModel.onDatePickerVisibilityChange(true) }
                )
            }
            item {
                if (uiState.isLoading) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        AppRotatingLoadingIndicator(
                            label = "Dang tai lich dat san..."
                        )
                    }
                } else if (uiState.scheduleData != null) {
                    BookingTimeGrid(
                        gridData = uiState.scheduleData!!.grid,
                        cellWidth = uiState.sliderValue.dp,
                        selectedSlots = uiState.selectedSlots,
                        selectedDate = uiState.scheduleData!!.selectedDate,
                        onSlotClick = viewModel::onSlotClick,
                        onMatchPostClick = { post ->
                            selectedMatchPost = post
                            showMatchDetailDialog = false
                            showJoinDialog = false
                        }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = parseDateToMillis(uiState.selectedDateText) ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onDatePickerVisibilityChange(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.onDatePicked(formatDateFromMillis(millis))
                        } ?: viewModel.onDatePickerVisibilityChange(false)
                    }
                ) {
                    Text(stringResource(R.string.booking_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDatePickerVisibilityChange(false) }) {
                    Text(stringResource(R.string.booking_cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }

    if (showBookingModeDialog && pendingConfirmationData != null) {
        BookingModeDialog(
            onDismiss = {
                showBookingModeDialog = false
                pendingConfirmationData = null
            },
            onPrivateBooking = {
                val confirmation = pendingConfirmationData ?: return@BookingModeDialog
                showBookingModeDialog = false
                pendingConfirmationData = null
                onNextClick(confirmation, null)
            },
            onFindOpponent = {
                showBookingModeDialog = false
                showFindOpponentFormDialog = true
                opponentTeamName = ""
                opponentPlayerCount = ""
                opponentDescription = ""
                opponentLevel = "BEGINNER"
            }
        )
    }

    if (showFindOpponentFormDialog && pendingConfirmationData != null) {
        FindOpponentInfoDialog(
            teamName = opponentTeamName,
            playerCount = opponentPlayerCount,
            description = opponentDescription,
            selectedLevel = opponentLevel,
            onTeamNameChange = { opponentTeamName = it },
            onPlayerCountChange = { opponentPlayerCount = it.filter(Char::isDigit) },
            onDescriptionChange = { opponentDescription = it },
            onLevelSelected = { opponentLevel = it },
            onDismiss = {
                showFindOpponentFormDialog = false
                pendingConfirmationData = null
            },
            onBack = {
                showFindOpponentFormDialog = false
                showBookingModeDialog = true
            },
            onConfirm = {
                val confirmation = pendingConfirmationData
                val playerCount = opponentPlayerCount.toIntOrNull()
                if (confirmation == null) {
                    showFindOpponentFormDialog = false
                } else if (opponentTeamName.isBlank() || playerCount == null || playerCount <= 0) {
                    Toast.makeText(
                        context,
                        "Vui lòng nhập đủ thông tin đội để tìm đối thủ.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val draft = FindOpponentDraft(
                        teamName = opponentTeamName.trim(),
                        playerCount = playerCount,
                        level = opponentLevel,
                        description = opponentDescription.trim()
                    )
                    showFindOpponentFormDialog = false
                    pendingConfirmationData = null
                    onNextClick(confirmation, draft)
                }
            }
        )
    }

    val activeMatchPost = selectedMatchPost
    if (activeMatchPost != null && !showMatchDetailDialog && !showJoinDialog) {
        MatchPostSummaryDialog(
            post = activeMatchPost,
            onDismiss = { selectedMatchPost = null },
            onViewDetail = { showMatchDetailDialog = true },
            onJoin = {
                showJoinDialog = true
                requestTeamName = ""
                requestPlayerCount = ""
                requestMessage = ""
            }
        )
    }

    if (activeMatchPost != null && showMatchDetailDialog) {
        MatchPostDetailDialog(
            post = activeMatchPost,
            onDismiss = {
                showMatchDetailDialog = false
                selectedMatchPost = null
            },
            onBack = { showMatchDetailDialog = false },
            onJoin = {
                showMatchDetailDialog = false
                showJoinDialog = true
            }
        )
    }

    if (activeMatchPost != null && showJoinDialog) {
        MatchRequestDialog(
            post = activeMatchPost,
            teamName = requestTeamName,
            playerCount = requestPlayerCount,
            message = requestMessage,
            isSubmitting = isSubmittingMatchRequest,
            onTeamNameChange = { requestTeamName = it },
            onPlayerCountChange = { requestPlayerCount = it.filter(Char::isDigit) },
            onMessageChange = { requestMessage = it },
            onDismiss = {
                showJoinDialog = false
                selectedMatchPost = null
            },
            onBack = {
                showJoinDialog = false
            },
            onSubmit = {
                val token = loadAuthToken(context)
                val playerCount = requestPlayerCount.toIntOrNull()

                if (token.isNullOrBlank()) {
                    Toast.makeText(
                        context,
                        "Bạn cần đăng nhập để gửi yêu cầu ghép trận.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (requestTeamName.isBlank() || playerCount == null || playerCount <= 0) {
                    Toast.makeText(
                        context,
                        "Vui lòng nhập tên đội và số lượng người chơi hợp lệ.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    coroutineScope.launch {
                        isSubmittingMatchRequest = true
                        runCatching {
                            userApi.submitMatchRequest(
                                token = token,
                                matchPostId = activeMatchPost.matchPostId,
                                teamName = requestTeamName.trim(),
                                playerCount = playerCount,
                                message = requestMessage.trim()
                            )
                        }.onSuccess {
                            isSubmittingMatchRequest = false
                            showJoinDialog = false
                            selectedMatchPost = null
                            Toast.makeText(
                                context,
                                "Đã gửi yêu cầu ghép trận.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.onFailure {
                            isSubmittingMatchRequest = false
                            Toast.makeText(
                                context,
                                it.message ?: "Không thể gửi yêu cầu ghép trận lúc này.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun rememberBookingScheduleViewModel(
    fieldId: Int,
    initialDateText: String,
    sessionKey: Int
): BookingScheduleViewModel {
    val key = remember(fieldId, initialDateText, sessionKey) {
        "booking_schedule_${fieldId}_${initialDateText}_$sessionKey"
    }
    return viewModel(
        key = key,
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BookingScheduleViewModel(
                    fieldId = fieldId,
                    initialDateText = initialDateText
                ) as T
            }
        }
    )
}

private fun parseDateToMillis(dateText: String): Long? {
    return runCatching {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
        formatter.parse(dateText)?.time
    }.getOrNull()
}

private fun formatDateFromMillis(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
    return formatter.format(Date(millis))
}

@Composable
private fun BookingModeDialog(
    onDismiss: () -> Unit,
    onPrivateBooking: () -> Unit,
    onFindOpponent: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(AppCardCornerRadius),
            color = MaterialTheme.colorScheme.surface
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Chọn hình thức đặt sân",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                BookingModeCard(
                    title = "🏟️ Đặt sân riêng",
                    description = "Dành cho đội đã có đủ người hoặc đã có đối thủ.",
                    onClick = onPrivateBooking
                )
                BookingModeCard(
                    title = "🤝 Tìm đối thủ",
                    description = "Đăng lịch thi đấu để tìm đội khác giao lưu và ghép trận.",
                    onClick = onFindOpponent
                )
            }
        }
    }
}

@Composable
private fun BookingModeCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(AppCardCornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FindOpponentInfoDialog(
    teamName: String,
    playerCount: String,
    description: String,
    selectedLevel: String,
    onTeamNameChange: (String) -> Unit,
    onPlayerCountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLevelSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val levels = listOf(
        "BEGINNER" to "Mới chơi",
        "INTERMEDIATE" to "Trung bình",
        "ADVANCED" to "Khá",
        "PRO" to "Chuyên nghiệp"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(AppCardCornerRadius),
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Thông tin đội",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                item {
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = onTeamNameChange,
                        label = { Text("Tên đội") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = playerCount,
                        onValueChange = onPlayerCountChange,
                        label = { Text("Số lượng người chơi") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Text(
                        text = "Trình độ",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                item {
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        levels.forEach { (value, label) ->
                            Surface(
                                onClick = { onLevelSelected(value) },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(AppCtaCornerRadius),
                                color = if (selectedLevel == value) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                }
                            ) {
                                Text(
                                    text = label,
                                    color = if (selectedLevel == value) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        label = { Text("Mô tả ngắn") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
                item {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = onBack,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Quay lại")
                        }
                        androidx.compose.material3.Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tiếp tục")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchPostSummaryDialog(
    post: MatchPostPreview,
    onDismiss: () -> Unit,
    onViewDetail: () -> Unit,
    onJoin: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(AppCardCornerRadius),
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Đội đang tìm đối thủ",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                item { MatchInfoLine("Tên đội", post.teamName) }
                item { MatchInfoLine("Số lượng", "${post.playerCount} người") }
                item { MatchInfoLine("Trình độ", post.levelLabel) }
                item {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = onViewDetail,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Xem chi tiết")
                        }
                        androidx.compose.material3.Button(
                            onClick = onJoin,
                            modifier = Modifier.weight(1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(AppCtaCornerRadius)
                        ) {
                            Text("Ghép trận")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchPostDetailDialog(
    post: MatchPostPreview,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    onJoin: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(AppCardCornerRadius),
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Chi tiết tìm đối thủ",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                item { MatchInfoLine("Tên đội", post.teamName) }
                item { MatchInfoLine("Số lượng", "${post.playerCount} người") }
                item { MatchInfoLine("Trình độ", post.levelLabel) }
                item { MatchInfoLine("Khung giờ", "${post.startTime} - ${post.endTime}") }
                item {
                    Text(
                        text = if (post.description.isBlank()) "Chưa có mô tả thêm." else post.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = onBack,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Quay lại")
                        }
                        androidx.compose.material3.Button(
                            onClick = onJoin,
                            modifier = Modifier.weight(1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(AppCtaCornerRadius)
                        ) {
                            Text("Ghép trận")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchRequestDialog(
    post: MatchPostPreview,
    teamName: String,
    playerCount: String,
    message: String,
    isSubmitting: Boolean,
    onTeamNameChange: (String) -> Unit,
    onPlayerCountChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(AppCardCornerRadius),
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Gửi yêu cầu ghép trận",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                item {
                    Text(
                        text = "Đội chủ sân: ${post.teamName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = onTeamNameChange,
                        label = { Text("Tên đội") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isSubmitting
                    )
                }
                item {
                    OutlinedTextField(
                        value = playerCount,
                        onValueChange = onPlayerCountChange,
                        label = { Text("Số lượng người chơi") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isSubmitting
                    )
                }
                item {
                    OutlinedTextField(
                        value = message,
                        onValueChange = onMessageChange,
                        label = { Text("Lời nhắn") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        enabled = !isSubmitting
                    )
                }
                item {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = onBack,
                            modifier = Modifier.weight(1f),
                            enabled = !isSubmitting
                        ) {
                            Text("Quay lại")
                        }
                        androidx.compose.material3.Button(
                            onClick = onSubmit,
                            modifier = Modifier.weight(1f),
                            enabled = !isSubmitting,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(AppCtaCornerRadius)
                        ) {
                            Text(if (isSubmitting) "Đang gửi..." else "Gửi yêu cầu")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchInfoLine(label: String, value: String) {
    androidx.compose.foundation.layout.Column(
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun loadAuthToken(context: android.content.Context): String? {
    return context
        .applicationContext
        .getSharedPreferences("user_repository_cache", android.content.Context.MODE_PRIVATE)
        .getString("auth_token", null)
        ?.takeIf { it.isNotBlank() }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun BookingScheduleScreenPreview() {
    SportUserTheme {
        BookingScheduleScreen(
            fieldId = 1,
            initialDateText = "25/04/2026",
            sessionKey = 1,
            onBackClick = {},
            onNextClick = { _, _ -> }
        )
    }
}
