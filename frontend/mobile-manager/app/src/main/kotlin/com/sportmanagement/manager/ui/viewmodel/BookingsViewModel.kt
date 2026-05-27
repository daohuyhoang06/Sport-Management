package com.sportmanagement.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.sportmanagement.manager.domain.model.BookingItem
import com.sportmanagement.manager.domain.model.BookingStatus
import com.sportmanagement.manager.ui.state.BookingsUiState
import com.sportmanagement.manager.ui.state.DayChipData
import com.sportmanagement.manager.ui.state.PitchFilterData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BookingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BookingsUiState())
    val uiState: StateFlow<BookingsUiState> = _uiState.asStateFlow()

    fun onDaySelected(index: Int) {
        _uiState.value = _uiState.value.copy(
            dayChips = _uiState.value.dayChips.mapIndexed { i, chip ->
                chip.copy(isSelected = i == index)
            }
        )
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
    }

    fun onBackFromDetail() {
        _uiState.value = _uiState.value.copy(selectedBooking = null)
    }

    fun onConfirmBooking(bookingId: String) {
        updateBookingStatus(bookingId, BookingStatus.CONFIRMED)
    }

    fun onRequestCancel(bookingId: String) {
        val booking = _uiState.value.bookings.firstOrNull { it.id == bookingId }
            ?: _uiState.value.selectedBooking?.takeIf { it.id == bookingId }
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
        _uiState.value = _uiState.value.copy(
            showAddBooking = !_uiState.value.showAddBooking
        )
    }

    fun onNewBookingCourtChanged(court: String) {
        _uiState.value = _uiState.value.copy(newBookingCourtCode = court)
    }

    fun onNewBookingDateChanged(date: String) {
        _uiState.value = _uiState.value.copy(newBookingDate = date)
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

    fun onSaveNewBooking() {
        _uiState.value = _uiState.value.copy(
            showAddBooking = false,
            newBookingCustomerName = "",
            newBookingCustomerPhone = "",
            newBookingDeposit = "",
            newBookingNotes = ""
        )
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
}
