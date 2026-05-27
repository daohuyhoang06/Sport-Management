package com.sportmanagement.manager.ui.state

import com.sportmanagement.manager.domain.model.ServiceCategory
import com.sportmanagement.manager.domain.model.ServiceDetailItem
import com.sportmanagement.manager.domain.model.ServiceItemStatus
import com.sportmanagement.manager.domain.model.StockTransaction
import com.sportmanagement.manager.domain.model.StockTxType

data class ServicesUiState(
    val services: List<ServiceDetailItem> = demoServiceItems(),
    val selectedService: ServiceDetailItem? = null,
    val showAddService: Boolean = false,
    val newServiceName: String = "",
    val newServiceCategory: ServiceCategory = ServiceCategory.OTHER,
    val newServicePrice: String = "",
    val newServiceStock: String = "",
    val newServiceDescription: String = "",
    val quickUpdateServiceId: String? = null,
    val quickUpdatePrice: String = "15,000",
    val quickUpdateStock: String = "124"
) {
    val totalServices: Int get() = services.size
    val totalActive: Int get() = services.count { it.isActive }
    val totalOutOfStock: Int get() = services.count { it.status == ServiceItemStatus.OUT_OF_STOCK }
    val dailyRevenue: Long get() = services.sumOf { it.revenue / 30 }
}

fun demoServiceItems() = listOf(
    ServiceDetailItem(
        id = "s1",
        name = "Nước uống & Giải khát",
        category = ServiceCategory.BEVERAGE,
        price = 15_000L,
        stock = 124,
        maxStock = 200,
        status = ServiceItemStatus.AVAILABLE,
        isActive = true,
        description = "Nước lọc, nước ngọt, nước tăng lực các loại. Phục vụ tại quầy và giao tận sân.",
        soldCount = 523,
        revenue = 7_845_000L,
        stockTransactions = listOf(
            StockTransaction("tx1", StockTxType.IMPORT, 50, "Nhập hàng từ nhà cung cấp ABC", "Hôm nay 08:00"),
            StockTransaction("tx2", StockTxType.SALE, -3, "Bán cho khách sân A1", "Hôm nay 10:30"),
            StockTransaction("tx3", StockTxType.SALE, -5, "Bán cho khách sân B1", "Hôm qua 18:00"),
            StockTransaction("tx4", StockTxType.IMPORT, 30, "Nhập hàng bổ sung", "Hôm qua 08:00")
        )
    ),
    ServiceDetailItem(
        id = "s2",
        name = "Thuê áo bib & Đồng phục",
        category = ServiceCategory.EQUIPMENT,
        price = 50_000L,
        stock = 45,
        maxStock = 60,
        status = ServiceItemStatus.AVAILABLE,
        isActive = true,
        description = "Áo bib và đồng phục cho đội bóng. Có đủ các size từ S đến XL.",
        soldCount = 218,
        revenue = 10_900_000L,
        stockTransactions = listOf(
            StockTransaction("tx5", StockTxType.SALE, -2, "Thuê cho khách sân A1", "Hôm nay 17:00"),
            StockTransaction("tx6", StockTxType.RETURN, 2, "Khách trả áo sân A1", "Hôm nay 18:30")
        )
    ),
    ServiceDetailItem(
        id = "s3",
        name = "Thuê bóng đá",
        category = ServiceCategory.EQUIPMENT,
        price = 30_000L,
        stock = 0,
        maxStock = 20,
        status = ServiceItemStatus.OUT_OF_STOCK,
        isActive = false,
        description = "Bóng đá size 4 và size 5 chuẩn FIFA.",
        soldCount = 312,
        revenue = 9_360_000L,
        stockTransactions = listOf(
            StockTransaction("tx7", StockTxType.SALE, -3, "Bán cho khách sân B1", "Hôm qua 20:00"),
            StockTransaction("tx8", StockTxType.ADJUST, -1, "Điều chỉnh kiểm kho", "02/10/2023")
        )
    ),
    ServiceDetailItem(
        id = "s4",
        name = "Dịch vụ trọng tài",
        category = ServiceCategory.PERSONNEL,
        price = 200_000L,
        stock = -1,
        maxStock = -1,
        status = ServiceItemStatus.AVAILABLE,
        isActive = true,
        description = "Trọng tài chuyên nghiệp cho các trận đấu giao hữu và giải đấu nội bộ.",
        soldCount = 87,
        revenue = 17_400_000L,
        stockTransactions = emptyList()
    )
)
