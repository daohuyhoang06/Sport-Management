package com.sportmanagement.user.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.BookingConfirmationData
import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.BookingSubCourt
import com.sportmanagement.user.domain.model.BookingTimeGridData
import com.sportmanagement.user.domain.model.BookingTimeRange
import com.sportmanagement.user.ui.components.booking.BookingBottomActionBar
import com.sportmanagement.user.ui.components.booking.BookingHeaderSection
import com.sportmanagement.user.ui.components.booking.BookingTimeGrid
import com.sportmanagement.user.ui.theme.SportUserTheme
import com.sportmanagement.user.ui.viewmodel.BookingScheduleViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScheduleScreen(
    scheduleData: BookingScheduleData,
    onBackClick: () -> Unit,
    onNextClick: (BookingConfirmationData) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookingScheduleViewModel = rememberBookingScheduleViewModel(scheduleData)
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val selectSlotError = stringResource(R.string.booking_select_slot_error)
    val lowerBackgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f)

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
                onNextClick = { onNextClick(viewModel.buildConfirmationData()) },
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
                BookingTimeGrid(
                    gridData = scheduleData.grid,
                    cellWidth = uiState.sliderValue.dp,
                    selectedSlots = uiState.selectedSlots,
                    onSlotClick = viewModel::onSlotClick
                )
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
}

@Composable
private fun rememberBookingScheduleViewModel(
    scheduleData: BookingScheduleData
): BookingScheduleViewModel {
    val key = remember(scheduleData) {
        "booking_schedule_${scheduleData.selectedDate}_${scheduleData.grid.openTime}_${scheduleData.grid.closeTime}_${scheduleData.grid.courts.size}"
    }
    return viewModel(
        key = key,
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BookingScheduleViewModel(scheduleData) as T
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

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun BookingScheduleScreenPreview() {
    SportUserTheme {
        BookingScheduleScreen(
            scheduleData = BookingScheduleData(
                selectedDate = "25/04/2026",
                grid = BookingTimeGridData(
                    openTime = "06:00",
                    closeTime = "22:00",
                    gridStepMinutes = 30,
                    minBookingMinutes = 60,
                    courts = listOf(
                        BookingSubCourt("court-1", "Sân 1"),
                        BookingSubCourt("court-2", "Sân 2"),
                        BookingSubCourt("court-3", "Sân 3")
                    ),
                    bookedSlots = listOf(
                        BookingTimeRange("court-1", "17:00", "18:00"),
                        BookingTimeRange("court-2", "17:00", "18:00"),
                        BookingTimeRange("court-3", "18:00", "18:30")
                    ),
                    blockedSlots = listOf(
                        BookingTimeRange("court-1", "19:00", "20:00"),
                        BookingTimeRange("court-2", "11:00", "14:00"),
                        BookingTimeRange("court-3", "06:00", "14:00")
                    )
                ),
                pricePerHour = 150_000,
                estimatedPrice = "150.000đ"
            ),
            onBackClick = {},
            onNextClick = {}
        )
    }
}
