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
import androidx.compose.ui.graphics.luminance
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
import com.sportmanagement.user.ui.AppNavigationBarEffect
import com.sportmanagement.user.ui.components.AppRotatingLoadingIndicator
import com.sportmanagement.user.ui.theme.SportUserTheme
import com.sportmanagement.user.ui.viewmodel.BookingScheduleViewModel
import kotlinx.coroutines.launch
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
    onNextClick: (BookingConfirmationData) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookingScheduleViewModel = rememberBookingScheduleViewModel(
        fieldId = fieldId,
        initialDateText = initialDateText,
        sessionKey = sessionKey
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val selectSlotError = stringResource(R.string.booking_select_slot_error)
    val lowerBackgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f)
    val bottomBarColor = MaterialTheme.colorScheme.primary

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
                    viewModel.buildConfirmationData()?.let { onNextClick(it) } 
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
                        onSlotClick = viewModel::onSlotClick
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

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun BookingScheduleScreenPreview() {
    SportUserTheme {
        BookingScheduleScreen(
            fieldId = 1,
            initialDateText = "25/04/2026",
            sessionKey = 1,
            onBackClick = {},
            onNextClick = {}
        )
    }
}
