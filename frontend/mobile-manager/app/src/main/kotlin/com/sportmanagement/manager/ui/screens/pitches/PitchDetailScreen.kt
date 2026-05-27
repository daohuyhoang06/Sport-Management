package com.sportmanagement.manager.ui.screens.pitches

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sportmanagement.manager.domain.model.BlockType
import com.sportmanagement.manager.domain.model.BlockedSlot
import com.sportmanagement.manager.domain.model.Court
import com.sportmanagement.manager.domain.model.CourtStatus
import com.sportmanagement.manager.domain.model.FieldPolicy
import com.sportmanagement.manager.domain.model.FieldScheduleConfig
import com.sportmanagement.manager.domain.model.FieldService
import com.sportmanagement.manager.domain.model.PitchDetail
import com.sportmanagement.manager.domain.model.PitchStatus
import com.sportmanagement.manager.ui.state.PitchDetailTab
import com.sportmanagement.manager.ui.state.PitchDetailUiState
import com.sportmanagement.manager.ui.theme.Amber
import com.sportmanagement.manager.ui.theme.AmberContainer
import com.sportmanagement.manager.ui.theme.SportManagerTheme
import com.sportmanagement.manager.ui.viewmodel.PitchDetailViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PitchDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PitchDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.pitchDetail.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = uiState.pitchDetail.sportType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Đóng")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PitchDetailTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::onTabSelected
            )

            when (uiState.selectedTab) {
                PitchDetailTab.OVERVIEW -> OverviewTab(
                    detail = uiState.pitchDetail,
                    onStatusChange = viewModel::onFieldStatusChange
                )
                PitchDetailTab.COURTS -> CourtsTab(
                    courts = uiState.pitchDetail.courts,
                    showAddDialog = uiState.showAddCourtDialog,
                    newCode = uiState.newCourtCode,
                    newName = uiState.newCourtName,
                    onToggleDialog = viewModel::onToggleAddCourtDialog,
                    onCodeChange = viewModel::onNewCourtCodeChange,
                    onNameChange = viewModel::onNewCourtNameChange,
                    onAdd = viewModel::onAddCourt,
                    onToggleStatus = viewModel::onCourtStatusToggle,
                    onDelete = viewModel::onDeleteCourt
                )
                PitchDetailTab.SCHEDULE -> ScheduleTab(
                    config = uiState.pitchDetail.scheduleConfig,
                    blockedSlots = uiState.pitchDetail.blockedSlots,
                    showEditDialog = uiState.showEditScheduleDialog,
                    showBlockDialog = uiState.showBlockSlotDialog,
                    editOpenTime = uiState.editOpenTime,
                    editCloseTime = uiState.editCloseTime,
                    editSlotMinutes = uiState.editSlotMinutes,
                    editSlotPrice = uiState.editSlotPrice,
                    editPendingHold = uiState.editPendingHold,
                    newBlockDate = uiState.newBlockDate,
                    newBlockStart = uiState.newBlockStart,
                    newBlockEnd = uiState.newBlockEnd,
                    newBlockReason = uiState.newBlockReason,
                    newBlockType = uiState.newBlockType,
                    onOpenEditDialog = viewModel::onOpenEditScheduleDialog,
                    onCloseEditDialog = viewModel::onCloseEditScheduleDialog,
                    onSaveSchedule = viewModel::onSaveSchedule,
                    onOpenTimeChange = viewModel::onEditOpenTimeChange,
                    onCloseTimeChange = viewModel::onEditCloseTimeChange,
                    onSlotMinutesChange = viewModel::onEditSlotMinutesChange,
                    onSlotPriceChange = viewModel::onEditSlotPriceChange,
                    onPendingHoldChange = viewModel::onEditPendingHoldChange,
                    onToggleBlockDialog = viewModel::onToggleBlockSlotDialog,
                    onBlockDateChange = viewModel::onNewBlockDateChange,
                    onBlockStartChange = viewModel::onNewBlockStartChange,
                    onBlockEndChange = viewModel::onNewBlockEndChange,
                    onBlockReasonChange = viewModel::onNewBlockReasonChange,
                    onBlockTypeChange = viewModel::onNewBlockTypeChange,
                    onAddBlock = viewModel::onAddBlockedSlot,
                    onDeleteBlock = viewModel::onDeleteBlockedSlot
                )
                PitchDetailTab.SERVICES -> ServicesTab(
                    services = uiState.pitchDetail.services,
                    showAddDialog = uiState.showAddServiceDialog,
                    newName = uiState.newServiceName,
                    newIsFree = uiState.newServiceIsFree,
                    newPrice = uiState.newServicePrice,
                    onToggleDialog = viewModel::onToggleAddServiceDialog,
                    onNameChange = viewModel::onNewServiceNameChange,
                    onIsFreeChange = viewModel::onNewServiceIsFreeChange,
                    onPriceChange = viewModel::onNewServicePriceChange,
                    onAdd = viewModel::onAddService,
                    onDelete = viewModel::onDeleteService
                )
                PitchDetailTab.POLICIES -> PoliciesTab(
                    policies = uiState.pitchDetail.policies,
                    showAddDialog = uiState.showAddPolicyDialog,
                    newType = uiState.newPolicyType,
                    newTitle = uiState.newPolicyTitle,
                    newContent = uiState.newPolicyContent,
                    onToggleDialog = viewModel::onToggleAddPolicyDialog,
                    onTypeChange = viewModel::onNewPolicyTypeChange,
                    onTitleChange = viewModel::onNewPolicyTitleChange,
                    onContentChange = viewModel::onNewPolicyContentChange,
                    onAdd = viewModel::onAddPolicy,
                    onDelete = viewModel::onDeletePolicy
                )
                PitchDetailTab.BOOKING_HISTORY -> BookingHistoryTab()
            }
        }
    }
}

// ── Tab row ────────────────────────────────────────────────────────────────────

@Composable
private fun PitchDetailTabRow(
    selectedTab: PitchDetailTab,
    onTabSelected: (PitchDetailTab) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .shadow(elevation = 2.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(PitchDetailTab.entries) { tab ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = tab.label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Tab 1: Tổng quan ───────────────────────────────────────────────────────────

@Composable
private fun OverviewTab(
    detail: PitchDetail,
    onStatusChange: (PitchStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = detail.avatarImageUrl,
                    contentDescription = detail.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            when (detail.status) {
                                PitchStatus.ACTIVE -> Color(0xFF4CAF50).copy(alpha = 0.9f)
                                PitchStatus.BOOKED -> Color(0xFF1976D2).copy(alpha = 0.9f)
                                PitchStatus.MAINTENANCE -> Color(0xFFF59E0B).copy(alpha = 0.9f)
                                PitchStatus.LOCKED -> Color(0xFFE11D48).copy(alpha = 0.9f)
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Text(
                        text = detail.status.isBadge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        if (detail.galleryUrls.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle("Thư viện ảnh")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(detail.galleryUrls) { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }

        item {
            InfoCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle("Thông tin cơ bản")
                    InfoRow(Icons.Filled.LocationOn, "Địa chỉ", detail.location)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    InfoRow(Icons.Filled.Phone, "Hotline", detail.phone)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    InfoRow(Icons.Filled.Star, "Loại sân", detail.sportType)
                }
            }
        }

        item {
            InfoCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle("Thống kê")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            value = detail.rating.toString(),
                            label = "Đánh giá",
                            color = Color(0xFFF59E0B)
                        )
                        StatItem(
                            value = detail.bookingCount.toString(),
                            label = "Lượt đặt",
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatItem(
                            value = detail.courts.count { it.status == CourtStatus.ACTIVE }.toString(),
                            label = "Sân active",
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }

        item {
            InfoCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle("Trạng thái hoạt động")
                    PitchStatus.entries.forEach { status ->
                        val isSelected = detail.status == status
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(0.2f)
                                    else Color.Transparent
                                )
                                .clickable { onStatusChange(status) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                            )
                            Column {
                                Text(
                                    text = status.label,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

// ── Tab 2: Sân con ────────────────────────────────────────────────────────────

@Composable
private fun CourtsTab(
    courts: List<Court>,
    showAddDialog: Boolean,
    newCode: String,
    newName: String,
    onToggleDialog: () -> Unit,
    onCodeChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onAdd: () -> Unit,
    onToggleStatus: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        SectionTitle("Danh sách sân con")
                        Text(
                            text = "${courts.size} sân · ${courts.count { it.status == CourtStatus.ACTIVE }} đang hoạt động",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            if (courts.isEmpty()) {
                item {
                    EmptyState(
                        message = "Chưa có sân con nào",
                        subMessage = "Thêm sân con để bắt đầu nhận booking"
                    )
                }
            } else {
                items(courts) { court ->
                    CourtCard(
                        court = court,
                        onToggleStatus = { onToggleStatus(court.id) },
                        onDelete = { onDelete(court.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        FloatingActionButton(
            onClick = onToggleDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Thêm sân con", tint = Color.White)
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = onToggleDialog,
            title = { Text("Thêm sân con") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newCode,
                        onValueChange = onCodeChange,
                        label = { Text("Mã sân (vd: S1, A, B)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = onNameChange,
                        label = { Text("Tên hiển thị (vd: Sân 1)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = onAdd, enabled = newCode.isNotBlank() && newName.isNotBlank()) {
                    Text("Thêm")
                }
            },
            dismissButton = {
                TextButton(onClick = onToggleDialog) { Text("Hủy") }
            }
        )
    }
}

@Composable
private fun CourtCard(
    court: Court,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = court.courtCode,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = court.courtName,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Thứ tự: ${court.sortOrder}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (court.status == CourtStatus.ACTIVE)
                        Icons.Outlined.CheckCircle else Icons.Outlined.PauseCircle,
                    contentDescription = null,
                    tint = if (court.status == CourtStatus.ACTIVE)
                        Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Switch(
                    checked = court.status == CourtStatus.ACTIVE,
                    onCheckedChange = { onToggleStatus() },
                    modifier = Modifier.height(24.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50)
                    )
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Xóa",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── Tab 3: Lịch & Giá ─────────────────────────────────────────────────────────

@Composable
private fun ScheduleTab(
    config: FieldScheduleConfig,
    blockedSlots: List<BlockedSlot>,
    showEditDialog: Boolean,
    showBlockDialog: Boolean,
    editOpenTime: String,
    editCloseTime: String,
    editSlotMinutes: Int,
    editSlotPrice: String,
    editPendingHold: Int,
    newBlockDate: String,
    newBlockStart: String,
    newBlockEnd: String,
    newBlockReason: String,
    newBlockType: BlockType,
    onOpenEditDialog: () -> Unit,
    onCloseEditDialog: () -> Unit,
    onSaveSchedule: () -> Unit,
    onOpenTimeChange: (String) -> Unit,
    onCloseTimeChange: (String) -> Unit,
    onSlotMinutesChange: (Int) -> Unit,
    onSlotPriceChange: (String) -> Unit,
    onPendingHoldChange: (Int) -> Unit,
    onToggleBlockDialog: () -> Unit,
    onBlockDateChange: (String) -> Unit,
    onBlockStartChange: (String) -> Unit,
    onBlockEndChange: (String) -> Unit,
    onBlockReasonChange: (String) -> Unit,
    onBlockTypeChange: (BlockType) -> Unit,
    onAddBlock: () -> Unit,
    onDeleteBlock: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            InfoCard {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle("Cấu hình thời gian & giá")
                        IconButton(onClick = onOpenEditDialog) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Chỉnh sửa",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    ScheduleConfigRow("Giờ mở cửa", config.openTime)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
                    ScheduleConfigRow("Giờ đóng cửa", config.closeTime)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
                    ScheduleConfigRow("Bước lưới", "${config.slotMinutes} phút/slot")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
                    ScheduleConfigRow("Giá theo slot", formatVnd(config.slotPrice) + "đ")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
                    ScheduleConfigRow("Giữ chỗ pending", "${config.pendingHoldMinutes} phút")
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    SectionTitle("Slot bị khóa")
                    Text(
                        text = "${blockedSlots.size} slot đang khóa",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Button(
                    onClick = onToggleBlockDialog,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Khóa slot", fontSize = 13.sp)
                }
            }
        }

        if (blockedSlots.isEmpty()) {
            item {
                EmptyState(
                    message = "Không có slot nào bị khóa",
                    subMessage = "Khóa slot khi bảo trì hoặc có sự kiện đặc biệt"
                )
            }
        } else {
            items(blockedSlots) { slot ->
                BlockedSlotCard(slot = slot, onDelete = { onDeleteBlock(slot.id) })
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }

    if (showEditDialog) {
        EditScheduleDialog(
            openTime = editOpenTime,
            closeTime = editCloseTime,
            slotMinutes = editSlotMinutes,
            slotPrice = editSlotPrice,
            pendingHold = editPendingHold,
            onOpenTimeChange = onOpenTimeChange,
            onCloseTimeChange = onCloseTimeChange,
            onSlotMinutesChange = onSlotMinutesChange,
            onSlotPriceChange = onSlotPriceChange,
            onPendingHoldChange = onPendingHoldChange,
            onSave = onSaveSchedule,
            onDismiss = onCloseEditDialog
        )
    }

    if (showBlockDialog) {
        AddBlockSlotDialog(
            date = newBlockDate,
            start = newBlockStart,
            end = newBlockEnd,
            reason = newBlockReason,
            blockType = newBlockType,
            onDateChange = onBlockDateChange,
            onStartChange = onBlockStartChange,
            onEndChange = onBlockEndChange,
            onReasonChange = onBlockReasonChange,
            onTypeChange = onBlockTypeChange,
            onAdd = onAddBlock,
            onDismiss = onToggleBlockDialog
        )
    }
}

@Composable
private fun ScheduleConfigRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun BlockedSlotCard(slot: BlockedSlot, onDelete: () -> Unit) {
    val blockColor = when (slot.blockType) {
        BlockType.MAINTENANCE -> Color(0xFFF59E0B)
        BlockType.EVENT -> MaterialTheme.colorScheme.tertiary
        BlockType.OTHER -> MaterialTheme.colorScheme.outline
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(blockColor)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = slot.blockDate,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${slot.startTime} - ${slot.endTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Text(
                    text = slot.reason.ifBlank { slot.blockType.label },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(blockColor.copy(0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = slot.blockType.label,
                        fontSize = 11.sp,
                        color = blockColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Xóa",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditScheduleDialog(
    openTime: String,
    closeTime: String,
    slotMinutes: Int,
    slotPrice: String,
    pendingHold: Int,
    onOpenTimeChange: (String) -> Unit,
    onCloseTimeChange: (String) -> Unit,
    onSlotMinutesChange: (Int) -> Unit,
    onSlotPriceChange: (String) -> Unit,
    onPendingHoldChange: (Int) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val slotOptions = listOf(30, 60, 90, 120)
    val holdOptions = listOf(10, 15, 20, 30)
    var slotExpanded by remember { mutableStateOf(false) }
    var holdExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cấu hình lịch & giá") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = openTime,
                    onValueChange = onOpenTimeChange,
                    label = { Text("Giờ mở cửa (HH:mm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = closeTime,
                    onValueChange = onCloseTimeChange,
                    label = { Text("Giờ đóng cửa (HH:mm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = slotExpanded,
                    onExpandedChange = { slotExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "$slotMinutes phút",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bước lưới (phút/slot)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(slotExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = slotExpanded, onDismissRequest = { slotExpanded = false }) {
                        slotOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text("$opt phút") },
                                onClick = { onSlotMinutesChange(opt); slotExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = slotPrice,
                    onValueChange = onSlotPriceChange,
                    label = { Text("Giá mỗi slot (đồng)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = holdExpanded,
                    onExpandedChange = { holdExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "$pendingHold phút",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Giữ chỗ pending (phút)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(holdExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = holdExpanded, onDismissRequest = { holdExpanded = false }) {
                        holdOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text("$opt phút") },
                                onClick = { onPendingHoldChange(opt); holdExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBlockSlotDialog(
    date: String,
    start: String,
    end: String,
    reason: String,
    blockType: BlockType,
    onDateChange: (String) -> Unit,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onReasonChange: (String) -> Unit,
    onTypeChange: (BlockType) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    var typeExpanded by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Khóa slot") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = onDateChange,
                    label = { Text("Ngày (yyyy-MM-dd)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = start,
                        onValueChange = onStartChange,
                        label = { Text("Từ (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = end,
                        onValueChange = onEndChange,
                        label = { Text("Đến (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = blockType.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loại khóa") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        BlockType.entries.forEach { bt ->
                            DropdownMenuItem(
                                text = { Text(bt.label) },
                                onClick = { onTypeChange(bt); typeExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    label = { Text("Lý do (tùy chọn)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onAdd, enabled = date.isNotBlank() && start.isNotBlank() && end.isNotBlank()) {
                Text("Khóa")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

// ── Tab 4: Dịch vụ ────────────────────────────────────────────────────────────

@Composable
private fun ServicesTab(
    services: List<FieldService>,
    showAddDialog: Boolean,
    newName: String,
    newIsFree: Boolean,
    newPrice: String,
    onToggleDialog: () -> Unit,
    onNameChange: (String) -> Unit,
    onIsFreeChange: (Boolean) -> Unit,
    onPriceChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SectionTitle("Dịch vụ tại sân") }

            if (services.isEmpty()) {
                item { EmptyState("Chưa có dịch vụ", "Thêm dịch vụ miễn phí hoặc tính phí") }
            } else {
                items(services) { service ->
                    ServiceCard(service = service, onDelete = { onDelete(service.id) })
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }

        FloatingActionButton(
            onClick = onToggleDialog,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Thêm dịch vụ", tint = Color.White)
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = onToggleDialog,
            title = { Text("Thêm dịch vụ") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = onNameChange,
                        label = { Text("Tên dịch vụ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Miễn phí", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = newIsFree,
                            onCheckedChange = onIsFreeChange,
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF4CAF50))
                        )
                    }
                    if (!newIsFree) {
                        OutlinedTextField(
                            value = newPrice,
                            onValueChange = onPriceChange,
                            label = { Text("Giá (đồng)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = onAdd, enabled = newName.isNotBlank()) { Text("Thêm") }
            },
            dismissButton = { TextButton(onClick = onToggleDialog) { Text("Hủy") } }
        )
    }
}

@Composable
private fun ServiceCard(service: FieldService, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (service.isFree) Color(0xFF4CAF50)
                        else MaterialTheme.colorScheme.primary
                    )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.serviceName,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (service.isFree) "Miễn phí" else "${formatVnd(service.price)}đ",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (service.isFree) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ── Tab 5: Chính sách ─────────────────────────────────────────────────────────

@Composable
private fun PoliciesTab(
    policies: List<FieldPolicy>,
    showAddDialog: Boolean,
    newType: String,
    newTitle: String,
    newContent: String,
    onToggleDialog: () -> Unit,
    onTypeChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SectionTitle("Chính sách & Nội quy") }

            if (policies.isEmpty()) {
                item { EmptyState("Chưa có chính sách", "Thêm chính sách thanh toán, hủy đặt, nội quy sân") }
            } else {
                items(policies) { policy ->
                    PolicyCard(policy = policy, onDelete = { onDelete(policy.id) })
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }

        FloatingActionButton(
            onClick = onToggleDialog,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Thêm chính sách", tint = Color.White)
        }
    }

    if (showAddDialog) {
        val policyTypes = mapOf("payment" to "Thanh toán", "cancellation" to "Hủy đặt", "rules" to "Nội quy", "other" to "Khác")
        var typeExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onToggleDialog,
            title = { Text("Thêm chính sách") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    @OptIn(ExperimentalMaterial3Api::class)
                    ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                        OutlinedTextField(
                            value = policyTypes[newType] ?: newType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Loại chính sách") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            policyTypes.forEach { (k, v) ->
                                DropdownMenuItem(text = { Text(v) }, onClick = { onTypeChange(k); typeExpanded = false })
                            }
                        }
                    }
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = onTitleChange,
                        label = { Text("Tiêu đề") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newContent,
                        onValueChange = onContentChange,
                        label = { Text("Nội dung") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = onAdd, enabled = newTitle.isNotBlank() && newContent.isNotBlank()) { Text("Thêm") }
            },
            dismissButton = { TextButton(onClick = onToggleDialog) { Text("Hủy") } }
        )
    }
}

@Composable
private fun PolicyCard(policy: FieldPolicy, onDelete: () -> Unit) {
    val typeLabels = mapOf("payment" to "Thanh toán", "cancellation" to "Hủy đặt", "rules" to "Nội quy", "other" to "Khác")
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(0.3f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = typeLabels[policy.policyType] ?: policy.policyType,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Text(
                        text = policy.title,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = policy.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

// ── Shared UI helpers ─────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
        )
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun EmptyState(message: String, subMessage: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Block,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(40.dp)
        )
        Text(text = message, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.outline)
        Text(
            text = subMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outlineVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ── Tab 6: Lịch sử đặt sân ────────────────────────────────────────────────────

private data class PitchBookingRecord(
    val id: String,
    val customerName: String,
    val courtCode: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val totalPrice: Long,
    val isPaid: Boolean,
    val status: String,
    val statusColor: Color
)

private val demoPitchBookingHistory = listOf(
    PitchBookingRecord("b001", "Nguyễn Văn An", "S1", "26/05/2026", "17:00", "18:30", 450_000L, true, "Hoàn thành", Color(0xFF15803D)),
    PitchBookingRecord("b002", "Trần Thị Bích", "S2", "26/05/2026", "18:30", "20:00", 450_000L, false, "Chờ xác nhận", Color(0xFFF59E0B)),
    PitchBookingRecord("b003", "Lê Hoàng Nam", "S1", "25/05/2026", "15:00", "16:30", 450_000L, true, "Hoàn thành", Color(0xFF15803D)),
    PitchBookingRecord("b004", "Phạm Minh Tuấn", "S3", "25/05/2026", "19:00", "20:30", 450_000L, true, "Đã xác nhận", Color(0xFF1976D2)),
    PitchBookingRecord("b005", "Hoàng Thị Mai", "S2", "24/05/2026", "08:00", "09:30", 450_000L, false, "Đã hủy", Color(0xFFE11D48))
)

@Composable
private fun BookingHistoryTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Lịch sử đặt sân")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${demoPitchBookingHistory.size} lượt",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Tất cả", "Hoàn thành", "Đang chờ", "Đã hủy").forEach { label ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (label == "Tất cả") MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            color = if (label == "Tất cả") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        items(demoPitchBookingHistory) { record ->
            PitchBookingHistoryCard(record = record)
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun PitchBookingHistoryCard(record: PitchBookingRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(100.dp)
                    .background(record.statusColor)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.customerName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(record.statusColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = record.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = record.statusColor
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.CalendarMonth, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                        Text(text = record.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.AccessTime, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                        Text(text = "${record.startTime} - ${record.endTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = record.courtCode, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Payments, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = formatVnd(record.totalPrice) + "đ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            if (record.isPaid) Icons.Filled.CheckCircle else Icons.Filled.AccessTime,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = if (record.isPaid) Color(0xFF15803D) else Color(0xFFF59E0B)
                        )
                        Text(
                            text = if (record.isPaid) "Đã thanh toán" else "Chưa thanh toán",
                            fontSize = 11.sp,
                            color = if (record.isPaid) Color(0xFF15803D) else Color(0xFFF59E0B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun formatVnd(amount: Long): String =
    NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount)

// ── Preview ────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844, device = "id:pixel_6")
@Composable
private fun PitchDetailScreenPreview() {
    SportManagerTheme {
        PitchDetailScreen(onBackClick = {})
    }
}
