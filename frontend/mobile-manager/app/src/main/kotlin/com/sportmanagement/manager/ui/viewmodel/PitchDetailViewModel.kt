package com.sportmanagement.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class PitchDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PitchDetailUiState())
    val uiState: StateFlow<PitchDetailUiState> = _uiState

    fun onTabSelected(tab: PitchDetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    // ── Court management ──────────────────────────────────────────────────────

    fun onToggleAddCourtDialog() {
        _uiState.update { it.copy(showAddCourtDialog = !it.showAddCourtDialog, newCourtCode = "", newCourtName = "") }
    }

    fun onNewCourtCodeChange(code: String) { _uiState.update { it.copy(newCourtCode = code) } }
    fun onNewCourtNameChange(name: String) { _uiState.update { it.copy(newCourtName = name) } }

    fun onAddCourt() {
        val state = _uiState.value
        if (state.newCourtCode.isBlank() || state.newCourtName.isBlank()) return
        val newCourt = Court(
            id = UUID.randomUUID().toString(),
            fieldId = state.pitchDetail.id,
            courtCode = state.newCourtCode.trim(),
            courtName = state.newCourtName.trim(),
            status = CourtStatus.ACTIVE,
            sortOrder = state.pitchDetail.courts.size + 1
        )
        _uiState.update { it.copy(
            pitchDetail = it.pitchDetail.copy(courts = it.pitchDetail.courts + newCourt),
            showAddCourtDialog = false,
            newCourtCode = "",
            newCourtName = ""
        )}
    }

    fun onCourtStatusToggle(courtId: String) {
        _uiState.update { state ->
            val updated = state.pitchDetail.courts.map { court ->
                if (court.id == courtId)
                    court.copy(status = if (court.status == CourtStatus.ACTIVE) CourtStatus.INACTIVE else CourtStatus.ACTIVE)
                else court
            }
            state.copy(pitchDetail = state.pitchDetail.copy(courts = updated))
        }
    }

    fun onDeleteCourt(courtId: String) {
        _uiState.update { state ->
            state.copy(pitchDetail = state.pitchDetail.copy(
                courts = state.pitchDetail.courts.filter { it.id != courtId }
            ))
        }
    }

    // ── Schedule config ───────────────────────────────────────────────────────

    fun onOpenEditScheduleDialog() {
        val config = _uiState.value.pitchDetail.scheduleConfig
        _uiState.update { it.copy(
            showEditScheduleDialog = true,
            editOpenTime = config.openTime,
            editCloseTime = config.closeTime,
            editSlotMinutes = config.slotMinutes,
            editSlotPrice = config.slotPrice.toString(),
            editPendingHold = config.pendingHoldMinutes
        )}
    }

    fun onCloseEditScheduleDialog() { _uiState.update { it.copy(showEditScheduleDialog = false) } }

    fun onEditOpenTimeChange(v: String) { _uiState.update { it.copy(editOpenTime = v) } }
    fun onEditCloseTimeChange(v: String) { _uiState.update { it.copy(editCloseTime = v) } }
    fun onEditSlotMinutesChange(v: Int) { _uiState.update { it.copy(editSlotMinutes = v) } }
    fun onEditSlotPriceChange(v: String) { _uiState.update { it.copy(editSlotPrice = v) } }
    fun onEditPendingHoldChange(v: Int) { _uiState.update { it.copy(editPendingHold = v) } }

    fun onSaveSchedule() {
        val state = _uiState.value
        val price = state.editSlotPrice.toLongOrNull() ?: return
        val newConfig = FieldScheduleConfig(
            openTime = state.editOpenTime,
            closeTime = state.editCloseTime,
            slotMinutes = state.editSlotMinutes,
            slotPrice = price,
            pendingHoldMinutes = state.editPendingHold
        )
        _uiState.update { it.copy(
            pitchDetail = it.pitchDetail.copy(scheduleConfig = newConfig),
            showEditScheduleDialog = false
        )}
    }

    // ── Blocked slots ─────────────────────────────────────────────────────────

    fun onToggleBlockSlotDialog() {
        _uiState.update { it.copy(
            showBlockSlotDialog = !it.showBlockSlotDialog,
            newBlockDate = "", newBlockStart = "", newBlockEnd = "",
            newBlockReason = "", newBlockType = BlockType.MAINTENANCE
        )}
    }

    fun onNewBlockDateChange(v: String) { _uiState.update { it.copy(newBlockDate = v) } }
    fun onNewBlockStartChange(v: String) { _uiState.update { it.copy(newBlockStart = v) } }
    fun onNewBlockEndChange(v: String) { _uiState.update { it.copy(newBlockEnd = v) } }
    fun onNewBlockReasonChange(v: String) { _uiState.update { it.copy(newBlockReason = v) } }
    fun onNewBlockTypeChange(v: BlockType) { _uiState.update { it.copy(newBlockType = v) } }

    fun onAddBlockedSlot() {
        val state = _uiState.value
        if (state.newBlockDate.isBlank() || state.newBlockStart.isBlank() || state.newBlockEnd.isBlank()) return
        val newSlot = BlockedSlot(
            id = UUID.randomUUID().toString(),
            fieldId = state.pitchDetail.id,
            courtId = null,
            blockDate = state.newBlockDate,
            startTime = state.newBlockStart,
            endTime = state.newBlockEnd,
            reason = state.newBlockReason,
            blockType = state.newBlockType
        )
        _uiState.update { it.copy(
            pitchDetail = it.pitchDetail.copy(blockedSlots = it.pitchDetail.blockedSlots + newSlot),
            showBlockSlotDialog = false
        )}
    }

    fun onDeleteBlockedSlot(blockId: String) {
        _uiState.update { state ->
            state.copy(pitchDetail = state.pitchDetail.copy(
                blockedSlots = state.pitchDetail.blockedSlots.filter { it.id != blockId }
            ))
        }
    }

    // ── Services ──────────────────────────────────────────────────────────────

    fun onToggleAddServiceDialog() {
        _uiState.update { it.copy(
            showAddServiceDialog = !it.showAddServiceDialog,
            newServiceName = "", newServiceIsFree = true, newServicePrice = ""
        )}
    }

    fun onNewServiceNameChange(v: String) { _uiState.update { it.copy(newServiceName = v) } }
    fun onNewServiceIsFreeChange(v: Boolean) { _uiState.update { it.copy(newServiceIsFree = v) } }
    fun onNewServicePriceChange(v: String) { _uiState.update { it.copy(newServicePrice = v) } }

    fun onAddService() {
        val state = _uiState.value
        if (state.newServiceName.isBlank()) return
        val price = if (state.newServiceIsFree) 0L else (state.newServicePrice.toLongOrNull() ?: 0L)
        val newService = FieldService(
            id = UUID.randomUUID().toString(),
            fieldId = state.pitchDetail.id,
            serviceName = state.newServiceName.trim(),
            isFree = state.newServiceIsFree,
            price = price
        )
        _uiState.update { it.copy(
            pitchDetail = it.pitchDetail.copy(services = it.pitchDetail.services + newService),
            showAddServiceDialog = false
        )}
    }

    fun onDeleteService(serviceId: String) {
        _uiState.update { state ->
            state.copy(pitchDetail = state.pitchDetail.copy(
                services = state.pitchDetail.services.filter { it.id != serviceId }
            ))
        }
    }

    // ── Policies ──────────────────────────────────────────────────────────────

    fun onToggleAddPolicyDialog() {
        _uiState.update { it.copy(
            showAddPolicyDialog = !it.showAddPolicyDialog,
            newPolicyType = "payment", newPolicyTitle = "", newPolicyContent = ""
        )}
    }

    fun onNewPolicyTypeChange(v: String) { _uiState.update { it.copy(newPolicyType = v) } }
    fun onNewPolicyTitleChange(v: String) { _uiState.update { it.copy(newPolicyTitle = v) } }
    fun onNewPolicyContentChange(v: String) { _uiState.update { it.copy(newPolicyContent = v) } }

    fun onAddPolicy() {
        val state = _uiState.value
        if (state.newPolicyTitle.isBlank() || state.newPolicyContent.isBlank()) return
        val newPolicy = FieldPolicy(
            id = UUID.randomUUID().toString(),
            fieldId = state.pitchDetail.id,
            policyType = state.newPolicyType,
            title = state.newPolicyTitle.trim(),
            content = state.newPolicyContent.trim()
        )
        _uiState.update { it.copy(
            pitchDetail = it.pitchDetail.copy(policies = it.pitchDetail.policies + newPolicy),
            showAddPolicyDialog = false
        )}
    }

    fun onDeletePolicy(policyId: String) {
        _uiState.update { state ->
            state.copy(pitchDetail = state.pitchDetail.copy(
                policies = state.pitchDetail.policies.filter { it.id != policyId }
            ))
        }
    }

    fun onFieldStatusChange(status: PitchStatus) {
        _uiState.update { it.copy(pitchDetail = it.pitchDetail.copy(status = status)) }
    }
}
