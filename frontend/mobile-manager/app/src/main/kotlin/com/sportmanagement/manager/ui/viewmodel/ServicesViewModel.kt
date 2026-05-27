package com.sportmanagement.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.sportmanagement.manager.domain.model.ServiceDetailItem
import com.sportmanagement.manager.domain.model.ServiceItemStatus
import com.sportmanagement.manager.domain.model.StockTransaction
import com.sportmanagement.manager.domain.model.StockTxType
import com.sportmanagement.manager.ui.state.ServicesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ServicesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ServicesUiState())
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

    fun onServiceClick(service: ServiceDetailItem) {
        _uiState.value = _uiState.value.copy(selectedService = service)
    }

    fun onBackFromDetail() {
        _uiState.value = _uiState.value.copy(selectedService = null)
    }

    fun onToggleServiceActive(serviceId: String) {
        val updated = _uiState.value.services.map { svc ->
            if (svc.id == serviceId) {
                val newActive = !svc.isActive
                svc.copy(
                    isActive = newActive,
                    status = if (newActive && svc.stock != 0) ServiceItemStatus.AVAILABLE
                    else if (!newActive) ServiceItemStatus.DISABLED
                    else svc.status
                )
            } else svc
        }
        val selectedUpdated = _uiState.value.selectedService?.let { sel ->
            if (sel.id == serviceId) {
                val newActive = !sel.isActive
                sel.copy(
                    isActive = newActive,
                    status = if (newActive && sel.stock != 0) ServiceItemStatus.AVAILABLE
                    else if (!newActive) ServiceItemStatus.DISABLED
                    else sel.status
                )
            } else sel
        }
        _uiState.value = _uiState.value.copy(services = updated, selectedService = selectedUpdated)
    }

    fun onAdjustStock(serviceId: String, delta: Int) {
        val updated = _uiState.value.services.map { svc ->
            if (svc.id == serviceId) {
                val newStock = maxOf(0, svc.stock + delta)
                val newTx = StockTransaction(
                    id = "tx_${System.currentTimeMillis()}",
                    type = if (delta > 0) StockTxType.IMPORT else StockTxType.SALE,
                    quantity = delta,
                    note = if (delta > 0) "Nhập thêm hàng" else "Điều chỉnh tồn kho",
                    timestamp = "Vừa xong"
                )
                svc.copy(
                    stock = newStock,
                    status = if (newStock == 0) ServiceItemStatus.OUT_OF_STOCK else ServiceItemStatus.AVAILABLE,
                    stockTransactions = listOf(newTx) + svc.stockTransactions
                )
            } else svc
        }
        val selectedUpdated = _uiState.value.selectedService?.let { sel ->
            updated.firstOrNull { it.id == sel.id }
        }
        _uiState.value = _uiState.value.copy(services = updated, selectedService = selectedUpdated)
    }

    fun onImportStock(serviceId: String, quantity: Int, note: String) {
        onAdjustStock(serviceId, quantity)
    }

    fun onQuickUpdatePriceChanged(price: String) {
        _uiState.value = _uiState.value.copy(quickUpdatePrice = price)
    }

    fun onQuickUpdateStockChanged(stock: String) {
        _uiState.value = _uiState.value.copy(quickUpdateStock = stock)
    }

    fun onApplyQuickUpdate(serviceId: String) {
        val priceVal = _uiState.value.quickUpdatePrice.replace(",", "").toLongOrNull() ?: return
        val stockVal = _uiState.value.quickUpdateStock.toIntOrNull() ?: return

        val updated = _uiState.value.services.map { svc ->
            if (svc.id == serviceId) svc.copy(price = priceVal, stock = stockVal) else svc
        }
        _uiState.value = _uiState.value.copy(services = updated)
    }

    fun onToggleAddService() {
        _uiState.value = _uiState.value.copy(showAddService = !_uiState.value.showAddService)
    }
}
