package com.sportmanagement.manager.ui.state

import com.sportmanagement.manager.domain.model.ServiceCategory
import com.sportmanagement.manager.domain.model.ServiceDetailItem
import com.sportmanagement.manager.domain.model.ServiceItemStatus

data class ServicesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val services: List<ServiceDetailItem> = emptyList(),
    val selectedService: ServiceDetailItem? = null,
    val showAddService: Boolean = false,
    val newServiceName: String = "",
    val newServiceCategory: ServiceCategory = ServiceCategory.OTHER,
    val newServicePrice: String = "",
    val newServiceStock: String = "",
    val newServiceDescription: String = "",
    val quickUpdateServiceId: String? = null,
    val quickUpdatePrice: String = "",
    val quickUpdateStock: String = ""
) {
    val totalServices: Int get() = services.size
    val totalActive: Int get() = services.count { it.isActive }
    val totalOutOfStock: Int get() = services.count { it.status == ServiceItemStatus.OUT_OF_STOCK }
    val dailyRevenue: Long get() = 0L
}
