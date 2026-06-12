package com.sportmanagement.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportmanagement.manager.data.AppContainer
import com.sportmanagement.manager.data.mapper.toBlockedSlot
import com.sportmanagement.manager.data.mapper.toBookingItem
import com.sportmanagement.manager.data.mapper.toCourt
import com.sportmanagement.manager.data.mapper.toFieldPolicy
import com.sportmanagement.manager.data.mapper.toFieldService
import com.sportmanagement.manager.data.mapper.toPitchDetail
import com.sportmanagement.manager.data.remote.dto.CreateBlockedSlotRequest
import com.sportmanagement.manager.data.remote.dto.CreateCourtRequest
import com.sportmanagement.manager.data.remote.dto.UpdateBasicInfoRequest
import com.sportmanagement.manager.data.remote.dto.CreatePolicyRequest
import com.sportmanagement.manager.data.remote.dto.CreateServiceRequest
import com.sportmanagement.manager.data.remote.dto.UpdateCourtRequest
import com.sportmanagement.manager.domain.model.BlockType
import com.sportmanagement.manager.domain.model.BlockedSlot
import com.sportmanagement.manager.domain.model.Court
import com.sportmanagement.manager.domain.model.CourtStatus
import com.sportmanagement.manager.domain.model.FieldScheduleConfig
import com.sportmanagement.manager.domain.model.PitchStatus
import com.sportmanagement.manager.ui.state.PitchDetailTab
import com.sportmanagement.manager.ui.state.PitchDetailUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class PitchDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PitchDetailUiState())
    val uiState: StateFlow<PitchDetailUiState> = _uiState

    fun loadField(fieldId: Int) {
        if (fieldId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val courtsDeferred  = async { AppContainer.fieldRepository.getCourts(fieldId) }
            val servicesDeferred = async { AppContainer.fieldRepository.getServices(fieldId) }
            val policiesDeferred = async { AppContainer.fieldRepository.getPolicies(fieldId) }
            val blockedSlotsDeferred = async { AppContainer.fieldRepository.getBlockedSlots(fieldId) }
            val fieldDeferred = async { AppContainer.fieldRepository.getField(fieldId) }
            val statsDeferred = async { AppContainer.fieldRepository.getFieldStats(fieldId) }
            val reviewStatsDeferred = async { AppContainer.fieldRepository.getFieldReviewStats(fieldId) }
            val reviewsDeferred = async { AppContainer.fieldRepository.getFieldReviews(fieldId) }
            val fieldsResult = AppContainer.fieldRepository.getFields()

            val fieldDto = fieldDeferred.await().getOrNull()
                ?: fieldsResult.getOrNull()?.firstOrNull { it.fieldId == fieldId }
            if (fieldDto == null) {
                _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy sân") }
                return@launch
            }

            val pitchDetail = fieldDto.toPitchDetail(
                courts   = courtsDeferred.await().getOrNull()?.map { it.toCourt() }         ?: emptyList(),
                services = servicesDeferred.await().getOrNull()?.map { it.toFieldService() } ?: emptyList(),
                policies = policiesDeferred.await().getOrNull()?.map { it.toFieldPolicy() }  ?: emptyList()
            ).copy(
                rating = reviewStatsDeferred.await().getOrNull()?.averageRating ?: 0f,
                bookingCount = statsDeferred.await().getOrNull()?.totalBookings ?: 0,
                blockedSlots = blockedSlotsDeferred.await().getOrNull()?.map { it.toBlockedSlot() } ?: emptyList()
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    pitchDetail = pitchDetail,
                    reviews = reviewsDeferred.await().getOrNull().orEmpty()
                )
            }
        }
    }

    fun onTabSelected(tab: PitchDetailTab) { _uiState.update { it.copy(selectedTab = tab) } }

    // ── Basic info edit ───────────────────────────────────────────────────────

    fun onOpenEditBasicInfoDialog() {
        val d = _uiState.value.pitchDetail
        _uiState.update { it.copy(
            showEditBasicInfoDialog = true,
            editFieldName = d.name,
            editLocation = d.location,
            editPhone = d.phone
        ) }
    }
    fun onCloseEditBasicInfoDialog() { _uiState.update { it.copy(showEditBasicInfoDialog = false) } }
    fun onEditFieldNameChange(v: String) { _uiState.update { it.copy(editFieldName = v) } }
    fun onEditLocationChange(v: String) { _uiState.update { it.copy(editLocation = v) } }
    fun onEditPhoneChange(v: String) { _uiState.update { it.copy(editPhone = v) } }

    fun onSaveBasicInfo() {
        val s = _uiState.value
        if (s.editFieldName.isBlank() || s.editLocation.isBlank()) return
        val fieldId = s.pitchDetail.id.toIntOrNull() ?: return
        _uiState.update { it.copy(showEditBasicInfoDialog = false) }
        viewModelScope.launch {
            val request = UpdateBasicInfoRequest(
                fieldName = s.editFieldName.trim(),
                location = s.editLocation.trim(),
                phone = s.editPhone.trim().ifBlank { null }
            )
            AppContainer.fieldRepository.updateBasicInfo(fieldId, request).fold(
                onSuccess = { loadField(fieldId) },
                onFailure = { e -> _uiState.update { it.copy(error = e.message) } }
            )
        }
    }

    // ── Courts ────────────────────────────────────────────────────────────────

    fun onToggleAddCourtDialog() {
        _uiState.update { it.copy(showAddCourtDialog = !it.showAddCourtDialog, newCourtCode = "", newCourtName = "") }
    }
    fun onNewCourtCodeChange(code: String) { _uiState.update { it.copy(newCourtCode = code) } }
    fun onNewCourtNameChange(name: String) { _uiState.update { it.copy(newCourtName = name) } }

    fun onAddCourt() {
        val s = _uiState.value
        if (s.newCourtCode.isBlank() || s.newCourtName.isBlank()) return
        val fieldId = s.pitchDetail.id.toIntOrNull() ?: return
        _uiState.update { it.copy(showAddCourtDialog = false, newCourtCode = "", newCourtName = "") }
        viewModelScope.launch {
            val request = CreateCourtRequest(
                courtCode = s.newCourtCode.trim(),
                courtName = s.newCourtName.trim()
            )
            AppContainer.fieldRepository.createCourt(fieldId, request).fold(
                onSuccess = { loadField(fieldId) },
                onFailure = { e -> _uiState.update { it.copy(error = e.message) } }
            )
        }
    }

    fun onCourtStatusToggle(courtId: String) {
        val s = _uiState.value
        val fieldId = s.pitchDetail.id.toIntOrNull() ?: return
        val courtIntId = courtId.toIntOrNull() ?: return
        val court = s.pitchDetail.courts.firstOrNull { it.id == courtId } ?: return
        val newStatus = if (court.status == CourtStatus.ACTIVE) "inactive" else "active"
        // Optimistic update
        _uiState.update { st ->
            st.copy(pitchDetail = st.pitchDetail.copy(courts = st.pitchDetail.courts.map { c ->
                if (c.id == courtId) c.copy(status = if (newStatus == "active") CourtStatus.ACTIVE else CourtStatus.INACTIVE) else c
            }))
        }
        viewModelScope.launch {
            AppContainer.fieldRepository.updateCourt(fieldId, courtIntId, UpdateCourtRequest(newStatus)).onFailure {
                loadField(fieldId) // revert nếu lỗi
            }
        }
    }

    fun onDeleteCourt(courtId: String) {
        val s = _uiState.value
        val fieldId = s.pitchDetail.id.toIntOrNull() ?: return
        val courtIntId = courtId.toIntOrNull() ?: return
        // Optimistic remove
        _uiState.update { st -> st.copy(pitchDetail = st.pitchDetail.copy(courts = st.pitchDetail.courts.filter { it.id != courtId })) }
        viewModelScope.launch {
            AppContainer.fieldRepository.deleteCourt(fieldId, courtIntId).onFailure {
                loadField(fieldId) // revert nếu lỗi
            }
        }
    }

    // ── Schedule ──────────────────────────────────────────────────────────────

    fun onOpenEditScheduleDialog() {
        val c = _uiState.value.pitchDetail.scheduleConfig
        _uiState.update { it.copy(showEditScheduleDialog = true, editOpenTime = c.openTime, editCloseTime = c.closeTime, editSlotMinutes = c.slotMinutes, editSlotPrice = c.slotPrice.toString(), editPendingHold = c.pendingHoldMinutes) }
    }
    fun onCloseEditScheduleDialog() { _uiState.update { it.copy(showEditScheduleDialog = false) } }
    fun onEditOpenTimeChange(v: String) { _uiState.update { it.copy(editOpenTime = v) } }
    fun onEditCloseTimeChange(v: String) { _uiState.update { it.copy(editCloseTime = v) } }
    fun onEditSlotMinutesChange(v: Int) { _uiState.update { it.copy(editSlotMinutes = v) } }
    fun onEditSlotPriceChange(v: String) { _uiState.update { it.copy(editSlotPrice = v) } }
    fun onEditPendingHoldChange(v: Int) { _uiState.update { it.copy(editPendingHold = v) } }

    fun onSaveSchedule() {
        val s = _uiState.value
        val price = s.editSlotPrice.toLongOrNull() ?: return
        _uiState.update { it.copy(pitchDetail = it.pitchDetail.copy(scheduleConfig = FieldScheduleConfig(s.editOpenTime, s.editCloseTime, s.editSlotMinutes, price, s.editPendingHold)), showEditScheduleDialog = false) }
    }

    // ── Blocked slots ─────────────────────────────────────────────────────────

    fun onToggleBlockSlotDialog() { _uiState.update { it.copy(showBlockSlotDialog = !it.showBlockSlotDialog, newBlockDate = "", newBlockStart = "", newBlockEnd = "", newBlockReason = "", newBlockType = BlockType.MAINTENANCE) } }
    fun onNewBlockDateChange(v: String) { _uiState.update { it.copy(newBlockDate = v) } }
    fun onNewBlockStartChange(v: String) { _uiState.update { it.copy(newBlockStart = v) } }
    fun onNewBlockEndChange(v: String) { _uiState.update { it.copy(newBlockEnd = v) } }
    fun onNewBlockReasonChange(v: String) { _uiState.update { it.copy(newBlockReason = v) } }
    fun onNewBlockTypeChange(v: BlockType) { _uiState.update { it.copy(newBlockType = v) } }

    fun onAddBlockedSlot() {
        val s = _uiState.value
        if (s.newBlockDate.isBlank() || s.newBlockStart.isBlank() || s.newBlockEnd.isBlank()) return
        val fieldId = s.pitchDetail.id.toIntOrNull() ?: return
        viewModelScope.launch {
            val request = CreateBlockedSlotRequest(
                blockDate = s.newBlockDate,
                startTime = "${s.newBlockStart}:00",
                endTime   = "${s.newBlockEnd}:00",
                reason    = s.newBlockReason.ifBlank { null },
                blockType = s.newBlockType.name.lowercase()
            )
            _uiState.update { it.copy(showBlockSlotDialog = false) }
            AppContainer.fieldRepository.createBlockedSlot(fieldId, request).fold(
                onSuccess = { loadField(fieldId) },  // reload để lấy slot_id thật
                onFailure = { e -> _uiState.update { it.copy(error = e.message) } }
            )
        }
    }

    fun onDeleteBlockedSlot(id: String) {
        val fieldId = _uiState.value.pitchDetail.id.toIntOrNull() ?: return
        val slotId = id.toIntOrNull()
        // Optimistic remove
        _uiState.update { s -> s.copy(pitchDetail = s.pitchDetail.copy(blockedSlots = s.pitchDetail.blockedSlots.filter { it.id != id })) }
        if (slotId != null) {
            viewModelScope.launch {
                AppContainer.fieldRepository.deleteBlockedSlot(fieldId, slotId).onFailure {
                    loadField(fieldId) // revert nếu lỗi
                }
            }
        }
    }

    // ── Services ──────────────────────────────────────────────────────────────

    fun onToggleAddServiceDialog() { _uiState.update { it.copy(showAddServiceDialog = !it.showAddServiceDialog, newServiceName = "", newServiceIsFree = true, newServicePrice = "") } }
    fun onNewServiceNameChange(v: String) { _uiState.update { it.copy(newServiceName = v) } }
    fun onNewServiceIsFreeChange(v: Boolean) { _uiState.update { it.copy(newServiceIsFree = v) } }
    fun onNewServicePriceChange(v: String) { _uiState.update { it.copy(newServicePrice = v) } }

    fun onAddService() {
        val s = _uiState.value
        if (s.newServiceName.isBlank()) return
        val price = if (s.newServiceIsFree) 0L else s.newServicePrice.toLongOrNull() ?: 0L
        if (!s.newServiceIsFree && price <= 0) return
        val fieldId = s.pitchDetail.id.toIntOrNull() ?: return
        _uiState.update { it.copy(showAddServiceDialog = false, newServiceName = "", newServiceIsFree = true, newServicePrice = "") }
        viewModelScope.launch {
            val request = CreateServiceRequest(
                serviceName = s.newServiceName.trim(),
                isFree = s.newServiceIsFree,
                price = price
            )
            AppContainer.fieldRepository.createService(fieldId, request).fold(
                onSuccess = { loadField(fieldId) },
                onFailure = { e -> _uiState.update { it.copy(error = e.message) } }
            )
        }
    }

    fun onDeleteService(id: String) {
        val fieldId = _uiState.value.pitchDetail.id.toIntOrNull() ?: return
        val serviceId = id.toIntOrNull() ?: return
        _uiState.update { s -> s.copy(pitchDetail = s.pitchDetail.copy(services = s.pitchDetail.services.filter { it.id != id })) }
        viewModelScope.launch {
            AppContainer.fieldRepository.deleteService(fieldId, serviceId).onFailure {
                loadField(fieldId)
            }
        }
    }

    // ── Policies ──────────────────────────────────────────────────────────────

    fun onToggleAddPolicyDialog() { _uiState.update { it.copy(showAddPolicyDialog = !it.showAddPolicyDialog, newPolicyType = "payment", newPolicyTitle = "", newPolicyContent = "") } }
    fun onNewPolicyTypeChange(v: String) { _uiState.update { it.copy(newPolicyType = v) } }
    fun onNewPolicyTitleChange(v: String) { _uiState.update { it.copy(newPolicyTitle = v) } }
    fun onNewPolicyContentChange(v: String) { _uiState.update { it.copy(newPolicyContent = v) } }

    fun onAddPolicy() {
        val s = _uiState.value
        if (s.newPolicyTitle.isBlank() || s.newPolicyContent.isBlank()) return
        val fieldId = s.pitchDetail.id.toIntOrNull() ?: return
        _uiState.update { it.copy(showAddPolicyDialog = false, newPolicyTitle = "", newPolicyContent = "") }
        viewModelScope.launch {
            val request = CreatePolicyRequest(
                title = s.newPolicyTitle.trim(),
                content = s.newPolicyContent.trim(),
                policyType = s.newPolicyType
            )
            AppContainer.fieldRepository.createPolicy(fieldId, request).fold(
                onSuccess = { loadField(fieldId) },
                onFailure = { e -> _uiState.update { it.copy(error = e.message) } }
            )
        }
    }

    fun onDeletePolicy(id: String) {
        val fieldId = _uiState.value.pitchDetail.id.toIntOrNull() ?: return
        val policyId = id.toIntOrNull() ?: return
        _uiState.update { s -> s.copy(pitchDetail = s.pitchDetail.copy(policies = s.pitchDetail.policies.filter { it.id != id })) }
        viewModelScope.launch {
            AppContainer.fieldRepository.deletePolicy(fieldId, policyId).onFailure {
                loadField(fieldId)
            }
        }
    }

    fun onFieldStatusChange(status: PitchStatus) {
        _uiState.update { it.copy(pitchDetail = it.pitchDetail.copy(status = status)) }
    }

    // ── Booking history ────────────────────────────────────────────────────────

    fun loadBookingHistory(fieldId: Int, showLoading: Boolean = true) {
        if (fieldId <= 0) return
        viewModelScope.launch {
            if (showLoading) _uiState.update { it.copy(isLoadingHistory = true, historyError = null) }
            val filter = _uiState.value.historyStatusFilter.takeIf { it != "all" }
            AppContainer.bookingRepository.getBookings(fieldId = fieldId, status = filter)
                .onSuccess { list ->
                    _uiState.update { it.copy(
                        isLoadingHistory = false,
                        bookingHistory = list.map { dto -> dto.toBookingItem() }
                    ) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoadingHistory = false, historyError = e.message) }
                }
        }
    }

    fun onHistoryFilterChange(filter: String) {
        val fieldId = _uiState.value.pitchDetail.id.toIntOrNull() ?: return
        if (fieldId <= 0) return
        _uiState.update { it.copy(historyStatusFilter = filter) }
        loadBookingHistory(fieldId, showLoading = true)
    }

    fun refreshBookingHistory() {
        val fieldId = _uiState.value.pitchDetail.id.toIntOrNull() ?: return
        if (fieldId <= 0) return
        loadBookingHistory(fieldId, showLoading = false)
    }
}
