package com.sportmanagement.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportmanagement.manager.data.AppContainer
import com.sportmanagement.manager.data.mapper.toBlockedSlot
import com.sportmanagement.manager.data.mapper.toCourt
import com.sportmanagement.manager.data.mapper.toFieldPolicy
import com.sportmanagement.manager.data.mapper.toFieldService
import com.sportmanagement.manager.data.mapper.toPitchDetail
import com.sportmanagement.manager.data.remote.dto.CreateBlockedSlotRequest
import com.sportmanagement.manager.domain.model.BlockType
import com.sportmanagement.manager.domain.model.BlockedSlot
import com.sportmanagement.manager.domain.model.Court
import com.sportmanagement.manager.domain.model.CourtStatus
import com.sportmanagement.manager.domain.model.FieldPolicy
import com.sportmanagement.manager.domain.model.FieldScheduleConfig
import com.sportmanagement.manager.domain.model.FieldService
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
            val fieldsResult    = AppContainer.fieldRepository.getFields()

            val fieldDto = fieldsResult.getOrNull()?.firstOrNull { it.fieldId == fieldId }
            if (fieldDto == null) {
                _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy sân") }
                return@launch
            }

            val blockedSlotsDeferred = async { AppContainer.fieldRepository.getBlockedSlots(fieldId) }

            val pitchDetail = fieldDto.toPitchDetail(
                courts   = courtsDeferred.await().getOrNull()?.map { it.toCourt() }         ?: emptyList(),
                services = servicesDeferred.await().getOrNull()?.map { it.toFieldService() } ?: emptyList(),
                policies = policiesDeferred.await().getOrNull()?.map { it.toFieldPolicy() }  ?: emptyList()
            ).copy(
                blockedSlots = blockedSlotsDeferred.await().getOrNull()?.map { it.toBlockedSlot() } ?: emptyList()
            )
            _uiState.update { it.copy(isLoading = false, pitchDetail = pitchDetail) }
        }
    }

    fun onTabSelected(tab: PitchDetailTab) { _uiState.update { it.copy(selectedTab = tab) } }

    // ── Courts ────────────────────────────────────────────────────────────────

    fun onToggleAddCourtDialog() {
        _uiState.update { it.copy(showAddCourtDialog = !it.showAddCourtDialog, newCourtCode = "", newCourtName = "") }
    }
    fun onNewCourtCodeChange(code: String) { _uiState.update { it.copy(newCourtCode = code) } }
    fun onNewCourtNameChange(name: String) { _uiState.update { it.copy(newCourtName = name) } }

    fun onAddCourt() {
        val s = _uiState.value
        if (s.newCourtCode.isBlank() || s.newCourtName.isBlank()) return
        val court = Court(UUID.randomUUID().toString(), s.pitchDetail.id, s.newCourtCode.trim(), s.newCourtName.trim(), CourtStatus.ACTIVE, s.pitchDetail.courts.size + 1)
        _uiState.update { it.copy(pitchDetail = it.pitchDetail.copy(courts = it.pitchDetail.courts + court), showAddCourtDialog = false, newCourtCode = "", newCourtName = "") }
    }

    fun onCourtStatusToggle(courtId: String) {
        _uiState.update { s ->
            s.copy(pitchDetail = s.pitchDetail.copy(courts = s.pitchDetail.courts.map { c ->
                if (c.id == courtId) c.copy(status = if (c.status == CourtStatus.ACTIVE) CourtStatus.INACTIVE else CourtStatus.ACTIVE) else c
            }))
        }
    }

    fun onDeleteCourt(courtId: String) {
        _uiState.update { s -> s.copy(pitchDetail = s.pitchDetail.copy(courts = s.pitchDetail.courts.filter { it.id != courtId })) }
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
        val svc = FieldService(UUID.randomUUID().toString(), s.pitchDetail.id, s.newServiceName.trim(), s.newServiceIsFree, if (s.newServiceIsFree) 0L else s.newServicePrice.toLongOrNull() ?: 0L)
        _uiState.update { it.copy(pitchDetail = it.pitchDetail.copy(services = it.pitchDetail.services + svc), showAddServiceDialog = false) }
    }

    fun onDeleteService(id: String) {
        _uiState.update { s -> s.copy(pitchDetail = s.pitchDetail.copy(services = s.pitchDetail.services.filter { it.id != id })) }
    }

    // ── Policies ──────────────────────────────────────────────────────────────

    fun onToggleAddPolicyDialog() { _uiState.update { it.copy(showAddPolicyDialog = !it.showAddPolicyDialog, newPolicyType = "payment", newPolicyTitle = "", newPolicyContent = "") } }
    fun onNewPolicyTypeChange(v: String) { _uiState.update { it.copy(newPolicyType = v) } }
    fun onNewPolicyTitleChange(v: String) { _uiState.update { it.copy(newPolicyTitle = v) } }
    fun onNewPolicyContentChange(v: String) { _uiState.update { it.copy(newPolicyContent = v) } }

    fun onAddPolicy() {
        val s = _uiState.value
        if (s.newPolicyTitle.isBlank() || s.newPolicyContent.isBlank()) return
        val policy = FieldPolicy(UUID.randomUUID().toString(), s.pitchDetail.id, s.newPolicyType, s.newPolicyTitle.trim(), s.newPolicyContent.trim())
        _uiState.update { it.copy(pitchDetail = it.pitchDetail.copy(policies = it.pitchDetail.policies + policy), showAddPolicyDialog = false) }
    }

    fun onDeletePolicy(id: String) {
        _uiState.update { s -> s.copy(pitchDetail = s.pitchDetail.copy(policies = s.pitchDetail.policies.filter { it.id != id })) }
    }

    fun onFieldStatusChange(status: PitchStatus) {
        _uiState.update { it.copy(pitchDetail = it.pitchDetail.copy(status = status)) }
    }
}
