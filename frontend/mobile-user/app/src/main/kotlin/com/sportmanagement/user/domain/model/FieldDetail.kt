package com.sportmanagement.user.domain.model

data class FieldDetailCourt(
    val id: String,
    val courtCode: String,
    val courtName: String,
    val isActive: Boolean
)

data class FieldDetailService(
    val serviceName: String,
    val isFree: Boolean,
    val price: Long = 0L
)

data class FieldDetailPolicy(
    val policyType: String,
    val title: String,
    val content: String
)

data class FieldDetail(
    val id: String,
    val name: String,
    val sportType: String,
    val location: String,
    val phone: String,
    val hours: String,
    val rating: Float,
    val reviewCount: Int,
    val pricePerSlot: Long,
    val slotMinutes: Int,
    val cardImageUrl: String,
    val avatarImageUrl: String,
    val galleryUrls: List<String>,
    val courts: List<FieldDetailCourt>,
    val services: List<FieldDetailService>,
    val policies: List<FieldDetailPolicy>,
    val tags: List<String> = emptyList()
)

fun mockFieldDetail() = FieldDetail(
    id = "field_001",
    name = "Sân bóng Mỹ Đình A",
    sportType = "Bóng đá",
    location = "Số 141 Đường Lê Đức Thọ, Nam Từ Liêm, Hà Nội",
    phone = "024 3765 8888",
    hours = "06:00 - 22:00",
    rating = 4.8f,
    reviewCount = 128,
    pricePerSlot = 450_000L,
    slotMinutes = 60,
    cardImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDqSz0OShmApq_057DXZpM1fkV8AOCMpF4vbsPSIP5wbRdwUNJB9sIqph_QdgwxxSTl1NqfJLzThKTFqs7YlhK9Ra31rjZ_Ircaj96pRJWHT6pwbLDrH81RCMwlLDXzgXuFdz-bKAiD75IlIT_HI0LLD7mj4hlGmOVQwKshH3MterQBZcc7D7PXq1RdPsoxEkevPcsHqF6_dv6XGQLvEOD1fO2k9x2PIvokwE9LYiXllT8nwai3cg_r4Dt0FjvrMqguN2W-Bc4W8CUL",
    avatarImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA_Vi9Xi_oX6bTETw0833W5-7pZs9tNEsO_f-nD9MI1RMjBrrsDBchRiEUV9J-go8Cnc47FXzzoWKBTrOKD87rlUsaqSSkKy6-_ucCtjcOqbnb8J47EvvWR2YYcT7BfcnxyIDTsbbAZ4kQBgSJ_fydZsim6Yb3y_QTbWYrdcyCez6d-vzeG0S-Z_R_7NabV3sKNueuSUdbCLnRvrme",
    galleryUrls = listOf(
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDyNTazlfrAd9-bjGc44ndIQ0kBAN4P8SX6jiWt1kp1lpF99h4RbT7-d-jjULIonpdLNFBfao_GK9RPeDlhuwS5vjdp_kRySMFKUR4nCE_qM2Pqx4Y3OBj80g_D4Hy-tFvRragT91kHpj-gwMOM-jUc74274VtGKINAhCHlmqjTh8aTPUQyoBCOS7mbwv5KoCkF86wxYVqLl82vjXE8YyXpn-BgqhOmCGLMYoAsX2MNGDbAZiQQbAqfHb5zrDBKPFXIcSQS42MOf_fK"
    ),
    courts = listOf(
        FieldDetailCourt("c1", "S1", "Sân 1", true),
        FieldDetailCourt("c2", "S2", "Sân 2", true),
        FieldDetailCourt("c3", "S3", "Sân 3", false)
    ),
    services = listOf(
        FieldDetailService("Cho thuê giày", false, 30_000L),
        FieldDetailService("Nước uống", true),
        FieldDetailService("Gửi xe miễn phí", true),
        FieldDetailService("Đèn chiếu sáng", false, 50_000L)
    ),
    policies = listOf(
        FieldDetailPolicy("payment", "Thanh toán", "Thanh toán trước khi sử dụng sân. Chấp nhận tiền mặt và chuyển khoản ngân hàng."),
        FieldDetailPolicy("cancellation", "Hủy đặt sân", "Hủy trước 2 giờ được hoàn tiền 100%. Hủy trong vòng 2 giờ không được hoàn tiền."),
        FieldDetailPolicy("rules", "Nội quy sân", "Không mang đồ ăn uống vào sân. Mặc đồ thể thao phù hợp khi thi đấu.")
    ),
    tags = listOf("Bóng đá 5", "Cỏ nhân tạo", "Đèn ban đêm", "Phòng thay đồ")
)
