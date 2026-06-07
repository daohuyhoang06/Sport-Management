package com.sportmanagement.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportmanagement.manager.data.AppContainer
import com.sportmanagement.manager.data.mapper.toServiceDetailItem
import com.sportmanagement.manager.domain.model.ServiceDetailItem
import com.sportmanagement.manager.domain.model.ServiceItemStatus
import com.sportmanagement.manager.domain.model.StockTransaction
import com.sportmanagement.manager.domain.model.StockTxType
import com.sportmanagement.manager.ui.state.ServicesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServicesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ServicesUiState())
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

    init {
        loadServices()
    }

    // Load services từ tất cả fields của manager
    fun loadServices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            AppContainer.fieldRepository.getFields().fold(
                onSuccess = { fields ->
                    val allServices = mutableListOf<ServiceDetailItem>()
                    fields.forEach { field ->
                        AppContainer.fieldRepository.getServices(field.fieldId).onSuccess { dtos ->
                            allServices.addAll(dtos.map { it.toServiceDetailItem() })
                        }
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, services = allServices)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

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
                svc.copy(isActive = newActive, status = if (newActive) ServiceItemStatus.AVAILABLE else ServiceItemStatus.DISABLED)
            } else svc
        }
        val selectedUpdated = _uiState.value.selectedService?.let { sel ->
            updated.firstOrNull { it.id == sel.id }
        }
        _uiState.value = _uiState.value.copy(services = updated, selectedService = selectedUpdated)
    }

    fun onAdjustStock(serviceId: String, delta: Int) {
        val updated = _uiState.value.services.map { svc ->
            if (svc.id == serviceId) {
                val newStock = maxOf(0, svc.stock + delta)
                val tx = StockTransaction(
                    id = "tx_${System.currentTimeMillis()}",
                    type = if (delta > 0) StockTxType.IMPORT else StockTxType.SALE,
                    quantity = delta,
                    note = if (delta > 0) "Nhập thêm hàng" else "Điều chỉnh tồn kho",
                    timestamp = "Vừa xong"
                )
                svc.copy(
                    stock = newStock,
                    status = if (newStock == 0) ServiceItemStatus.OUT_OF_STOCK else ServiceItemStatus.AVAILABLE,
                    stockTransactions = listOf(tx) + svc.stockTransactions
                )
            } else svc
        }
        _uiState.value = _uiState.value.copy(
            services = updated,
            selectedService = _uiState.value.selectedService?.let { sel -> updated.firstOrNull { it.id == sel.id } }
        )
    }

    fun onImportStock(serviceId: String, quantity: Int, note: String) = onAdjustStock(serviceId, quantity)

    fun onQuickUpdatePriceChanged(price: String) { _uiState.value = _uiState.value.copy(quickUpdatePrice = price) }
    fun onQuickUpdateStockChanged(stock: String) { _uiState.value = _uiState.value.copy(quickUpdateStock = stock) }

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
