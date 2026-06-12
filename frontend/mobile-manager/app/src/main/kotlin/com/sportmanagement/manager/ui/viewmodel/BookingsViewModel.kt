package com.sportmanagement.manager.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportmanagement.manager.data.AppContainer
import com.sportmanagement.manager.data.mapper.toBookingItem
import com.sportmanagement.manager.data.mapper.toHistoryEvent
import com.sportmanagement.manager.data.remote.dto.CreateBookingRequest
import com.sportmanagement.manager.ui.state.buildWeekDayChips
import com.sportmanagement.manager.ui.state.currentWeekStartIso
import com.sportmanagement.manager.domain.model.BookingItem
import com.sportmanagement.manager.domain.model.BookingStatus
import com.sportmanagement.manager.ui.state.BookingsUiState
import com.sportmanagement.manager.ui.state.PitchFilterData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BookingsViewModel : ViewModel() {
    private val apiDateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val _uiState = MutableStateFlow(BookingsUiState())
    val uiState: StateFlow<BookingsUiState> = _uiState.asStateFlow()

    init {
        loadPitchFilters()
        selectDate(todayIso())
    }

    fun loadPitchFilters() {
        viewModelScope.launch {
            AppContainer.fieldRepository.getFields().onSuccess { fields ->
                val filters = mutableListOf(PitchFilterData("Tất cả", isSelected = true))
                fields.forEach { field -> filters.add(PitchFilterData(field.fieldName, isSelected = false)) }
                _uiState.value = _uiState.value.copy(pitchFilters = filters)
            }
        }
    }

    fun loadBookings(status: String? = null, startDate: String? = null, endDate: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            AppContainer.bookingRepository.getBookings(
                status = status,
                startDate = startDate,
                endDate = endDate
            ).fold(
                onSuccess = { dtos ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        bookings = dtos.map { it.toBookingItem() }
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun onDaySelected(index: Int) {
        val selectedDate = _uiState.value.dayChips.getOrNull(index)?.isoDate ?: return
        selectDate(selectedDate)
    }

    fun onPreviousWeek() {
        val selectedDate = shiftDateIso(_uiState.value.selectedDate, -7) ?: return
        selectDate(selectedDate)
    }

    fun onNextWeek() {
        val selectedDate = shiftDateIso(_uiState.value.selectedDate, 7) ?: return
        selectDate(selectedDate)
    }

    fun onCalendarDateSelected(dateMillis: Long) {
        selectDate(formatDateIso(dateMillis))
    }

    fun showTodaySchedule() {
        val t = todayIso()
        Log.d("BookingsViewModel", "showTodaySchedule called, todayIso=$t")
        selectDate(t)
    }

    fun onPitchFilterSelected(index: Int) {
        _uiState.value = _uiState.value.copy(
            pitchFilters = _uiState.value.pitchFilters.mapIndexed { i, chip ->
                chip.copy(isSelected = i == index)
            }
        )
    }

    fun onBookingClick(booking: BookingItem) {
        _uiState.value = _uiState.value.copy(selectedBooking = booking)
        loadBookingHistory(booking.id)
    }

    fun onBackFromDetail() {
        _uiState.value = _uiState.value.copy(selectedBooking = null)
    }

    fun loadBookingHistory(bookingId: String) {
        viewModelScope.launch {
            AppContainer.bookingRepository.getBookingHistory(bookingId.toIntOrNull() ?: return@launch)
                .onSuccess { dtos ->
                    val updated = _uiState.value.bookingHistory.toMutableMap()
                    updated[bookingId] = dtos.map { it.toHistoryEvent() }
                    _uiState.value = _uiState.value.copy(bookingHistory = updated)
                }
        }
    }

    fun onSaveNewBooking() {
        val s = _uiState.value
        val fieldId = s.newBookingSelectedFieldId ?: return
        if (s.newBookingDate.isBlank() || s.newBookingStart.isBlank() || s.newBookingEnd.isBlank()) return

        viewModelScope.launch {
            val isoDate = ddMmYyyyToIso(s.newBookingDate) ?: return@launch
            val startIso = "${isoDate}T${s.newBookingStart}:00.000Z"
            val endIso   = "${isoDate}T${s.newBookingEnd}:00.000Z"

            val request = CreateBookingRequest(
                fieldId = fieldId,
                courtId = s.newBookingCourtId,
                customerName = s.newBookingCustomerName.ifBlank { null },
                customerPhone = s.newBookingCustomerPhone.ifBlank { null },
                startTime = startIso,
                endTime = endIso,
                note = s.newBookingNotes.ifBlank { null }
            )
            AppContainer.bookingRepository.createBooking(request).fold(
                onSuccess = { dto ->
                    val newBooking = dto.toBookingItem()
                    _uiState.value = _uiState.value.copy(
                        showAddBooking = false,
                        bookings = listOf(newBooking) + _uiState.value.bookings,
                        newBookingCourtId = null,
                        newBookingCourtCode = "",
                        newBookingCustomerName = "",
                        newBookingCustomerPhone = "",
                        newBookingDeposit = "",
                        newBookingNotes = ""
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
            )
        }
    }

    fun onConfirmBooking(bookingId: String) {
        updateBookingStatus(bookingId, BookingStatus.CONFIRMED)
        viewModelScope.launch {
            AppContainer.bookingRepository.approveBooking(bookingId.toIntOrNull() ?: return@launch)
                .onFailure { updateBookingStatus(bookingId, BookingStatus.PENDING) }
        }
    }

    fun onRequestCancel(bookingId: String) {
        _uiState.value = _uiState.value.copy(
            showCancelDialog = true,
            cancelTargetId = bookingId,
            cancelReasonDraft = ""
        )
    }

    fun onCancelReasonChanged(reason: String) {
        _uiState.value = _uiState.value.copy(cancelReasonDraft = reason)
    }

    fun onConfirmCancel() {
        val id = _uiState.value.cancelTargetId
        val reason = _uiState.value.cancelReasonDraft
        val updated = _uiState.value.bookings.map { booking ->
            if (booking.id == id) booking.copy(status = BookingStatus.CANCELLED, cancelReason = reason) else booking
        }
        val selectedUpdated = _uiState.value.selectedBooking?.let { sel ->
            if (sel.id == id) sel.copy(status = BookingStatus.CANCELLED, cancelReason = reason) else sel
        }
        _uiState.value = _uiState.value.copy(
            bookings = updated,
            selectedBooking = selectedUpdated,
            showCancelDialog = false,
            cancelTargetId = "",
            cancelReasonDraft = ""
        )
        viewModelScope.launch {
            AppContainer.bookingRepository.cancelBooking(id.toIntOrNull() ?: return@launch, reason.ifBlank { null })
                .onFailure { loadBookings() }
        }
    }

    fun onDismissCancelDialog() {
        _uiState.value = _uiState.value.copy(showCancelDialog = false, cancelTargetId = "", cancelReasonDraft = "")
    }

    fun onCancelBooking(bookingId: String) {
        onRequestCancel(bookingId)
    }

    fun onCompleteBooking(bookingId: String) {
        updateBookingStatus(bookingId, BookingStatus.COMPLETED)
    }

    fun onRequestEdit(bookingId: String) {
        val booking = _uiState.value.bookings.firstOrNull { it.id == bookingId }
            ?: _uiState.value.selectedBooking?.takeIf { it.id == bookingId }
        if (booking != null) {
            _uiState.value = _uiState.value.copy(
                showEditDialog = true,
                editTargetId = bookingId,
                editDate = booking.date,
                editStart = booking.startTime,
                editEnd = booking.endTime,
                editCourtCode = booking.courtCode,
                editCustomerName = booking.customer.name,
                editCustomerPhone = booking.customer.phone
            )
        }
    }

    fun onEditDateChanged(date: String) { _uiState.value = _uiState.value.copy(editDate = date) }
    fun onEditStartChanged(time: String) { _uiState.value = _uiState.value.copy(editStart = time) }
    fun onEditEndChanged(time: String) { _uiState.value = _uiState.value.copy(editEnd = time) }
    fun onEditCourtChanged(court: String) { _uiState.value = _uiState.value.copy(editCourtCode = court) }
    fun onEditCustomerNameChanged(name: String) { _uiState.value = _uiState.value.copy(editCustomerName = name) }
    fun onEditCustomerPhoneChanged(phone: String) { _uiState.value = _uiState.value.copy(editCustomerPhone = phone) }

    fun onConfirmEdit() {
        val id = _uiState.value.editTargetId
        val s = _uiState.value
        val updated = s.bookings.map { b ->
            if (b.id == id) b.copy(
                date = s.editDate,
                startTime = s.editStart,
                endTime = s.editEnd,
                courtCode = s.editCourtCode,
                customer = b.customer.copy(name = s.editCustomerName, phone = s.editCustomerPhone)
            ) else b
        }
        val selectedUpdated = s.selectedBooking?.let { sel ->
            if (sel.id == id) sel.copy(
                date = s.editDate,
                startTime = s.editStart,
                endTime = s.editEnd,
                courtCode = s.editCourtCode,
                customer = sel.customer.copy(name = s.editCustomerName, phone = s.editCustomerPhone)
            ) else sel
        }
        _uiState.value = s.copy(bookings = updated, selectedBooking = selectedUpdated, showEditDialog = false, editTargetId = "")
    }

    fun onDismissEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = false, editTargetId = "")
    }

    fun onRequestPayment(bookingId: String) {
        _uiState.value = _uiState.value.copy(
            showPaymentDialog = true,
            paymentTargetId = bookingId,
            paymentMethodDraft = "Tiền mặt",
            paymentNoteDraft = ""
        )
    }

    fun onPaymentMethodChanged(method: String) { _uiState.value = _uiState.value.copy(paymentMethodDraft = method) }
    fun onPaymentNoteChanged(note: String) { _uiState.value = _uiState.value.copy(paymentNoteDraft = note) }

    fun onConfirmPayment() {
        val id = _uiState.value.paymentTargetId
        val method = _uiState.value.paymentMethodDraft
        val updated = _uiState.value.bookings.map { b ->
            if (b.id == id) b.copy(isPaid = true, paymentMethod = method) else b
        }
        val selectedUpdated = _uiState.value.selectedBooking?.let { sel ->
            if (sel.id == id) sel.copy(isPaid = true, paymentMethod = method) else sel
        }
        _uiState.value = _uiState.value.copy(
            bookings = updated,
            selectedBooking = selectedUpdated,
            showPaymentDialog = false,
            paymentTargetId = ""
        )
    }

    fun onDismissPaymentDialog() {
        _uiState.value = _uiState.value.copy(showPaymentDialog = false, paymentTargetId = "")
    }

    fun onToggleAddBooking() {
        val willShow = !_uiState.value.showAddBooking
        _uiState.value = _uiState.value.copy(showAddBooking = willShow)
        if (willShow) loadFieldsForBooking()
    }

    private fun loadFieldsForBooking() {
        viewModelScope.launch {
            AppContainer.fieldRepository.getFields().onSuccess { fields ->
                val firstField = fields.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    newBookingFields = fields,
                    newBookingSelectedFieldId = firstField?.fieldId,
                    newBookingCourts = emptyList(),
                    newBookingCourtId = null,
                    newBookingCourtCode = ""
                )
                if (firstField != null) loadCourtsForBooking(firstField.fieldId)
            }
        }
    }

    fun onNewBookingFieldSelected(fieldId: Int) {
        _uiState.value = _uiState.value.copy(
            newBookingSelectedFieldId = fieldId,
            newBookingCourts = emptyList(),
            newBookingCourtId = null,
            newBookingCourtCode = ""
        )
        loadCourtsForBooking(fieldId)
    }

    private fun loadCourtsForBooking(fieldId: Int) {
        viewModelScope.launch {
            AppContainer.fieldRepository.getCourts(fieldId).onSuccess { courts ->
                val activeCourts = courts.filter { it.status == "active" }
                _uiState.value = _uiState.value.copy(newBookingCourts = activeCourts)
            }
        }
    }

    fun onNewBookingCourtSelected(courtId: Int, courtCode: String) {
        _uiState.value = _uiState.value.copy(
            newBookingCourtId = courtId,
            newBookingCourtCode = courtCode,
            newBookingStart = "",
            newBookingEnd = ""
        )
        refreshAvailabilityIfReady()
    }

    fun onNewBookingCourtChanged(court: String) {
        _uiState.value = _uiState.value.copy(newBookingCourtCode = court)
    }

    fun onTimeSlotTapped(time: String) {
        val s = _uiState.value
        when {
            s.newBookingStart.isNotBlank() && s.newBookingEnd.isNotBlank() -> {
                _uiState.value = s.copy(newBookingStart = time, newBookingEnd = "")
            }
            s.newBookingStart.isNotBlank() -> {
                val tapMin   = timeStrToMin(time)
                val startMin = timeStrToMin(s.newBookingStart)
                _uiState.value = if (tapMin <= startMin)
                    s.copy(newBookingStart = time, newBookingEnd = "")
                else
                    s.copy(newBookingEnd = time)
            }
            else -> {
                _uiState.value = s.copy(newBookingStart = time, newBookingEnd = "")
            }
        }
    }

    private fun refreshAvailabilityIfReady() {
        val s = _uiState.value
        val fieldId  = s.newBookingSelectedFieldId ?: return
        val courtId  = s.newBookingCourtId ?: return
        val dateIso  = ddMmYyyyToIso(s.newBookingDate) ?: return
        loadSlotAvailability(fieldId, courtId, dateIso)
    }

    private fun loadSlotAvailability(fieldId: Int, courtId: Int, isoDate: String) {
        _uiState.value = _uiState.value.copy(newBookingIsLoadingSlots = true, newBookingBookedRanges = emptyList())
        viewModelScope.launch {
            AppContainer.fieldRepository.getCourtAvailability(fieldId, courtId, isoDate)
                .onSuccess { ranges ->
                    _uiState.value = _uiState.value.copy(
                        newBookingBookedRanges = ranges.map { Pair(timeStrToMin(it.startTime), timeStrToMin(it.endTime)) },
                        newBookingIsLoadingSlots = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(newBookingIsLoadingSlots = false)
                }
        }
    }

    private fun timeStrToMin(t: String): Int {
        val p = t.split(":")
        return (p.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (p.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    private fun ddMmYyyyToIso(date: String): String? {
        val p = date.split("/")
        return if (p.size == 3) "${p[2]}-${p[1]}-${p[0]}" else null
    }

    fun onNewBookingDateChanged(date: String) {
        _uiState.value = _uiState.value.copy(newBookingDate = date, newBookingStart = "", newBookingEnd = "")
        refreshAvailabilityIfReady()
    }

    fun onNewBookingStartChanged(time: String) {
        _uiState.value = _uiState.value.copy(newBookingStart = time)
    }

    fun onNewBookingEndChanged(time: String) {
        _uiState.value = _uiState.value.copy(newBookingEnd = time)
    }

    fun onNewBookingCustomerNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(newBookingCustomerName = name)
    }

    fun onNewBookingCustomerPhoneChanged(phone: String) {
        _uiState.value = _uiState.value.copy(newBookingCustomerPhone = phone)
    }

    fun onNewBookingDepositChanged(deposit: String) {
        _uiState.value = _uiState.value.copy(newBookingDeposit = deposit)
    }

    fun onNewBookingNotesChanged(notes: String) {
        _uiState.value = _uiState.value.copy(newBookingNotes = notes)
    }

    private fun updateBookingStatus(bookingId: String, newStatus: BookingStatus) {
        val updated = _uiState.value.bookings.map { booking ->
            if (booking.id == bookingId) booking.copy(status = newStatus) else booking
        }
        val selectedUpdated = _uiState.value.selectedBooking?.let { sel ->
            if (sel.id == bookingId) sel.copy(status = newStatus) else sel
        }
        _uiState.value = _uiState.value.copy(
            bookings = updated,
            selectedBooking = selectedUpdated
        )
    }

    private fun selectDate(dateIso: String) {
        val weekStartDate = startOfWeekIso(dateIso)
        Log.d("BookingsViewModel", "selectDate called, dateIso=$dateIso, weekStartDate=$weekStartDate")
        _uiState.value = _uiState.value.copy(
            visibleWeekStartDate = weekStartDate,
            selectedDate = dateIso,
            dayChips = buildWeekDayChips(weekStartDate, dateIso)
        )
        Log.d("BookingsViewModel", "loading bookings for $dateIso")
        loadBookings(startDate = dateIso, endDate = dateIso)
    }

    private fun todayIso(): String = apiDateFmt.format(Date())

    private fun formatDateIso(dateMillis: Long): String = apiDateFmt.format(Date(dateMillis))

    private fun shiftDateIso(dateIso: String, days: Int): String? {
        val date = runCatching { apiDateFmt.parse(dateIso) }.getOrNull() ?: return null
        val calendar = Calendar.getInstance().apply { time = date }
        calendar.add(Calendar.DAY_OF_MONTH, days)
        return apiDateFmt.format(calendar.time)
    }

    private fun startOfWeekIso(dateIso: String): String {
        val date = runCatching { apiDateFmt.parse(dateIso) }.getOrNull() ?: Date()
        val calendar = Calendar.getInstance().apply {
            time = date
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
        return apiDateFmt.format(calendar.time)
    }
}
