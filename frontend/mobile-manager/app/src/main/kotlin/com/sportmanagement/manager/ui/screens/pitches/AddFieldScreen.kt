package com.sportmanagement.manager.ui.screens.pitches

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmanagement.manager.ui.viewmodel.PitchesViewModel

// Sport type options (aligned with backend sport_id)
private val SPORT_TYPES = listOf(
    1 to "Bóng đá",
    2 to "Bóng rổ",
    3 to "Cầu lông",
    4 to "Tennis",
    5 to "Bóng chuyền",
    6 to "Pickleball"
)

private val STATUS_OPTIONS = listOf(
    "active" to "Đang hoạt động",
    "maintenance" to "Đang bảo trì",
    "inactive" to "Tạm ngưng"
)

private val SLOT_MINUTES_OPTIONS = listOf(30, 60, 90, 120)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFieldScreen(
    onBackClick: () -> Unit,
    viewModel: PitchesViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var fieldName by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var selectedSportId by rememberSaveable { mutableStateOf(1) }
    var openTime by rememberSaveable { mutableStateOf("06:00") }
    var closeTime by rememberSaveable { mutableStateOf("22:00") }
    var slotPriceText by rememberSaveable { mutableStateOf("") }
    var selectedSlotMinutes by rememberSaveable { mutableStateOf(60) }
    var selectedStatus by rememberSaveable { mutableStateOf("active") }

    // Validation errors
    var fieldNameError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.saveError) {
        uiState.saveError?.let { snackbarHostState.showSnackbar(it) }
    }

    fun validate(): Boolean {
        fieldNameError = if (fieldName.isBlank()) "Vui lòng nhập tên sân" else null
        locationError = if (location.isBlank()) "Vui lòng nhập địa chỉ" else null
        priceError = if (slotPriceText.isNotBlank() && slotPriceText.toDoubleOrNull() == null)
            "Giá không hợp lệ" else null
        return fieldNameError == null && locationError == null && priceError == null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thêm sân mới",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(4.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Thông tin cơ bản
            SectionHeader("Thông tin cơ bản")

            OutlinedTextField(
                value = fieldName,
                onValueChange = { fieldName = it; fieldNameError = null },
                label = { Text("Tên sân *") },
                placeholder = { Text("VD: Sân Thể Thao ABC") },
                isError = fieldNameError != null,
                supportingText = fieldNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it; locationError = null },
                label = { Text("Địa chỉ *") },
                placeholder = { Text("VD: 123 Nguyễn Văn A, Q.1, TP.HCM") },
                isError = locationError != null,
                supportingText = locationError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Số điện thoại") },
                placeholder = { Text("VD: 090 123 4567") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Section: Loại sân
            SectionHeader("Loại sân")
            OptionSelector(
                options = SPORT_TYPES.map { it.second },
                selectedIndex = SPORT_TYPES.indexOfFirst { it.first == selectedSportId }.coerceAtLeast(0),
                onSelect = { selectedSportId = SPORT_TYPES[it].first }
            )

            // Section: Giá & Thời lượng
            SectionHeader("Giá & thời lượng slot")

            OutlinedTextField(
                value = slotPriceText,
                onValueChange = { slotPriceText = it; priceError = null },
                label = { Text("Giá mỗi slot (VND)") },
                placeholder = { Text("VD: 150000") },
                isError = priceError != null,
                supportingText = priceError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                trailingIcon = { Text("₫", color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(end = 12.dp)) },
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Thời lượng mỗi slot",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OptionSelector(
                    options = SLOT_MINUTES_OPTIONS.map { "$it phút" },
                    selectedIndex = SLOT_MINUTES_OPTIONS.indexOf(selectedSlotMinutes).coerceAtLeast(0),
                    onSelect = { selectedSlotMinutes = SLOT_MINUTES_OPTIONS[it] }
                )
            }

            // Section: Giờ hoạt động
            SectionHeader("Khung giờ hoạt động")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeInputField(
                    label = "Giờ mở cửa",
                    value = openTime,
                    onValueChange = { openTime = it },
                    modifier = Modifier.weight(1f)
                )
                TimeInputField(
                    label = "Giờ đóng cửa",
                    value = closeTime,
                    onValueChange = { closeTime = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // Section: Trạng thái
            SectionHeader("Trạng thái ban đầu")
            OptionSelector(
                options = STATUS_OPTIONS.map { it.second },
                selectedIndex = STATUS_OPTIONS.indexOfFirst { it.first == selectedStatus }.coerceAtLeast(0),
                onSelect = { selectedStatus = STATUS_OPTIONS[it].first }
            )

            Spacer(Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    if (validate()) {
                        viewModel.createField(
                            fieldName = fieldName.trim(),
                            location = location.trim(),
                            sportId = selectedSportId,
                            phone = phone.trim().takeIf { it.isNotBlank() },
                            openTime = openTime.trim(),
                            closeTime = closeTime.trim(),
                            slotPrice = slotPriceText.toDoubleOrNull(),
                            slotMinutes = selectedSlotMinutes,
                            status = selectedStatus
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isSaving,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "Tạo sân mới",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 0.5.sp
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptionSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TimeInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("HH:mm") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}
