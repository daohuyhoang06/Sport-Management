package com.sportmanagement.manager.domain.model

enum class ServiceItemStatus(val label: String) {
    AVAILABLE("Còn hàng"),
    OUT_OF_STOCK("Hết hàng"),
    DISABLED("Ngừng bán")
}

enum class ServiceCategory(val label: String) {
    BEVERAGE("Đồ uống"),
    FOOD("Thức ăn"),
    EQUIPMENT("Dụng cụ"),
    PERSONNEL("Nhân sự"),
    OTHER("Khác")
}

enum class StockTxType(val label: String) {
    IMPORT("Nhập hàng"),
    SALE("Bán hàng"),
    ADJUST("Điều chỉnh"),
    RETURN("Trả hàng")
}

data class StockTransaction(
    val id: String,
    val type: StockTxType,
    val quantity: Int,
    val note: String,
    val timestamp: String
)

data class ServiceDetailItem(
    val id: String,
    val name: String,
    val category: ServiceCategory,
    val price: Long,
    val stock: Int,
    val maxStock: Int = 200,
    val status: ServiceItemStatus,
    val isActive: Boolean,
    val description: String = "",
    val soldCount: Int = 0,
    val revenue: Long = 0L,
    val stockTransactions: List<StockTransaction> = emptyList()
)
