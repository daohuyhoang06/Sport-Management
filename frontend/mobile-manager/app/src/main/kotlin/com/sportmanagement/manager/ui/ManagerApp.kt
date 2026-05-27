package com.sportmanagement.manager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sportmanagement.manager.domain.model.Pitch
import com.sportmanagement.manager.ui.navigation.ManagerTab
import com.sportmanagement.manager.ui.state.BookingsUiState
import com.sportmanagement.manager.ui.screens.auth.LoginScreen
import com.sportmanagement.manager.ui.screens.bookings.AddBookingScreen
import com.sportmanagement.manager.ui.screens.bookings.BookingDetailScreen
import com.sportmanagement.manager.ui.screens.bookings.BookingsScreen
import com.sportmanagement.manager.ui.screens.dashboard.DashboardScreen
import com.sportmanagement.manager.ui.screens.messages.MessageThreadScreen
import com.sportmanagement.manager.ui.screens.messages.MessagesScreen
import com.sportmanagement.manager.ui.screens.pitches.PitchDetailScreen
import com.sportmanagement.manager.ui.screens.pitches.PitchesScreen
import com.sportmanagement.manager.ui.screens.services.ServiceDetailScreen
import com.sportmanagement.manager.ui.screens.services.ServicesScreen
import com.sportmanagement.manager.ui.viewmodel.BookingsViewModel
import com.sportmanagement.manager.ui.viewmodel.DashboardViewModel
import com.sportmanagement.manager.ui.viewmodel.MessagesViewModel
import com.sportmanagement.manager.ui.viewmodel.ServicesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerApp(dashboardViewModel: DashboardViewModel = viewModel()) {
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }

    if (!isLoggedIn) {
        LoginScreen(onLoginSuccess = { isLoggedIn = true })
        return
    }

    val dashboardState by dashboardViewModel.uiState.collectAsState()

    val bookingsViewModel: BookingsViewModel = viewModel()
    val messagesViewModel: MessagesViewModel = viewModel()
    val servicesViewModel: ServicesViewModel = viewModel()

    val bookingsState by bookingsViewModel.uiState.collectAsState()
    val messagesState by messagesViewModel.uiState.collectAsState()
    val servicesState by servicesViewModel.uiState.collectAsState()

    var selectedTab by rememberSaveable { mutableStateOf(ManagerTab.Dashboard) }
    var selectedPitch by remember { mutableStateOf<Pitch?>(null) }

    when {
        selectedPitch != null -> {
            PitchDetailScreen(onBackClick = { selectedPitch = null })
            return
        }
        bookingsState.selectedBooking != null -> {
            BookingDetailScreen(
                booking = bookingsState.selectedBooking!!,
                onBackClick = { bookingsViewModel.onBackFromDetail() },
                onConfirm = { bookingsViewModel.onConfirmBooking(it) },
                onCancel = { bookingsViewModel.onRequestCancel(it) },
                onEdit = { bookingsViewModel.onRequestEdit(it) },
                onPaymentConfirm = { bookingsViewModel.onRequestPayment(it) },
                onMessageCustomer = {
                    bookingsState.selectedBooking?.let { booking ->
                        val conv = messagesState.conversations.firstOrNull {
                            it.customerPhone == booking.customer.phone
                        }
                        if (conv != null) {
                            bookingsViewModel.onBackFromDetail()
                            selectedTab = ManagerTab.Messages
                            messagesViewModel.onConversationClick(conv)
                        }
                    }
                }
            )
            // Show cancel/edit/payment dialogs on top of the detail screen
            if (bookingsState.showCancelDialog) {
                CancelBookingDialogRoot(
                    reason = bookingsState.cancelReasonDraft,
                    onReasonChanged = bookingsViewModel::onCancelReasonChanged,
                    onConfirm = bookingsViewModel::onConfirmCancel,
                    onDismiss = bookingsViewModel::onDismissCancelDialog
                )
            }
            if (bookingsState.showEditDialog) {
                EditBookingDialogRoot(
                    state = bookingsState,
                    onDateChanged = bookingsViewModel::onEditDateChanged,
                    onStartChanged = bookingsViewModel::onEditStartChanged,
                    onEndChanged = bookingsViewModel::onEditEndChanged,
                    onCourtChanged = bookingsViewModel::onEditCourtChanged,
                    onCustomerNameChanged = bookingsViewModel::onEditCustomerNameChanged,
                    onCustomerPhoneChanged = bookingsViewModel::onEditCustomerPhoneChanged,
                    onConfirm = bookingsViewModel::onConfirmEdit,
                    onDismiss = bookingsViewModel::onDismissEditDialog
                )
            }
            if (bookingsState.showPaymentDialog) {
                PaymentConfirmDialogRoot(
                    method = bookingsState.paymentMethodDraft,
                    note = bookingsState.paymentNoteDraft,
                    onMethodChanged = bookingsViewModel::onPaymentMethodChanged,
                    onNoteChanged = bookingsViewModel::onPaymentNoteChanged,
                    onConfirm = bookingsViewModel::onConfirmPayment,
                    onDismiss = bookingsViewModel::onDismissPaymentDialog
                )
            }
            return
        }
        bookingsState.showAddBooking -> {
            AddBookingScreen(
                uiState = bookingsState,
                onBackClick = { bookingsViewModel.onToggleAddBooking() },
                onCourtCodeChanged = bookingsViewModel::onNewBookingCourtChanged,
                onDateChanged = bookingsViewModel::onNewBookingDateChanged,
                onStartChanged = bookingsViewModel::onNewBookingStartChanged,
                onEndChanged = bookingsViewModel::onNewBookingEndChanged,
                onCustomerNameChanged = bookingsViewModel::onNewBookingCustomerNameChanged,
                onCustomerPhoneChanged = bookingsViewModel::onNewBookingCustomerPhoneChanged,
                onDepositChanged = bookingsViewModel::onNewBookingDepositChanged,
                onNotesChanged = bookingsViewModel::onNewBookingNotesChanged,
                onSave = { bookingsViewModel.onSaveNewBooking() }
            )
            return
        }
        messagesState.selectedConversation != null -> {
            val conv = messagesState.selectedConversation!!
            MessageThreadScreen(
                conversation = conv,
                messages = messagesState.chatMessages[conv.id] ?: emptyList(),
                draftMessage = messagesState.draftMessage,
                onBackClick = { messagesViewModel.onBackFromThread() },
                onDraftChanged = messagesViewModel::onDraftMessageChanged,
                onSend = { messagesViewModel.onSendMessage() }
            )
            return
        }
        servicesState.selectedService != null -> {
            ServiceDetailScreen(
                service = servicesState.selectedService!!,
                onBackClick = { servicesViewModel.onBackFromDetail() },
                onToggleActive = { servicesViewModel.onToggleServiceActive(it) },
                onAdjustStock = { id, delta -> servicesViewModel.onAdjustStock(id, delta) }
            )
            return
        }
    }

    Scaffold(
        topBar = {
            ManagerTopAppBar(
                managerName = dashboardState.managerName,
                managerAvatarUrl = dashboardState.managerAvatarUrl
            )
        },
        bottomBar = {
            ManagerBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when (selectedTab) {
            ManagerTab.Dashboard -> DashboardScreen(
                padding = padding,
                viewModel = dashboardViewModel
            )
            ManagerTab.Pitches -> PitchesScreen(
                padding = padding,
                onPitchClick = { selectedPitch = it }
            )
            ManagerTab.Bookings -> BookingsScreen(
                padding = padding,
                onBookingClick = { bookingsViewModel.onBookingClick(it) },
                onAddBooking = { bookingsViewModel.onToggleAddBooking() },
                viewModel = bookingsViewModel
            )
            ManagerTab.Services -> ServicesScreen(
                padding = padding,
                onServiceClick = { servicesViewModel.onServiceClick(it) },
                viewModel = servicesViewModel
            )
            ManagerTab.Messages -> MessagesScreen(
                padding = padding,
                onConversationClick = { messagesViewModel.onConversationClick(it) },
                viewModel = messagesViewModel
            )
        }
    }
}

@Composable
private fun ManagerAvatar(managerName: String, avatarUrl: String?) {
    val initial = managerName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "M"

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initial,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManagerTopAppBar(managerName: String, managerAvatarUrl: String?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp),
        color = Color.White
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            ),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ManagerAvatar(managerName = managerName, avatarUrl = managerAvatarUrl)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CHÀO BUỔI SÁNG",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp,
                            color = MaterialTheme.colorScheme.outline,
                            lineHeight = 12.sp
                        )
                        Text(
                            text = managerName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 22.sp
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Thông báo",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )
    }
}

@Composable
private fun ManagerBottomBar(
    selectedTab: ManagerTab,
    onTabSelected: (ManagerTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier.shadow(elevation = 8.dp)
    ) {
        ManagerTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.filledIcon else tab.outlinedIcon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.label.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF94A3B8)
                )
            )
        }
    }
}

@Composable
fun CancelBookingDialogRoot(
    reason: String,
    onReasonChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hủy lịch đặt sân") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Vui lòng ghi nhận lý do hủy để thông báo cho khách hàng.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChanged,
                    label = { Text("Lý do hủy") },
                    placeholder = { Text("Ví dụ: Sân đang bảo trì khẩn cấp...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
            ) { Text("Xác nhận hủy") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Quay lại") } }
    )
}

@Composable
fun EditBookingDialogRoot(
    state: BookingsUiState,
    onDateChanged: (String) -> Unit,
    onStartChanged: (String) -> Unit,
    onEndChanged: (String) -> Unit,
    onCourtChanged: (String) -> Unit,
    onCustomerNameChanged: (String) -> Unit,
    onCustomerPhoneChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sửa thông tin đặt sân") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(state.editCourtCode, onCourtChanged, label = { Text("Sân (mã sân)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.editDate, onDateChanged, label = { Text("Ngày (dd/MM/yyyy)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = "${state.editStart} - ${state.editEnd}",
                    onValueChange = {},
                    label = { Text("Khung giờ (bắt đầu - kết thúc)") },
                    enabled = false,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(state.editStart, onStartChanged, label = { Text("Giờ bắt đầu (HH:mm)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.editEnd, onEndChanged, label = { Text("Giờ kết thúc (HH:mm)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.editCustomerName, onCustomerNameChanged, label = { Text("Tên khách hàng") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.editCustomerPhone, onCustomerPhoneChanged, label = { Text("Số điện thoại") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Lưu thay đổi") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
fun PaymentConfirmDialogRoot(
    method: String,
    note: String,
    onMethodChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xác nhận thanh toán") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = method,
                    onValueChange = onMethodChanged,
                    label = { Text("Phương thức thanh toán") },
                    placeholder = { Text("Tiền mặt / Chuyển khoản / Ví điện tử") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChanged,
                    label = { Text("Ghi chú") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
            ) { Text("Xác nhận đã thanh toán") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
    device = "id:pixel_6"
)
@Composable
private fun ManagerAppPreview() {
    com.sportmanagement.manager.ui.theme.SportManagerTheme {
        ManagerApp(dashboardViewModel = DashboardViewModel())
    }
}
