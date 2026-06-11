package com.sportmanagement.user.domain.model

data class FieldReview(
    val reviewId: Int = 0,
    val fieldId: Int = 0,
    val customerName: String = "",
    val customerAvatarUrl: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: String = "",
    val imageUrls: List<String> = emptyList()
)

data class FieldReviewStats(
    val averageRating: Double = 0.0,
    val totalReviews: Int = 0,
    val fiveStar: Int = 0,
    val fourStar: Int = 0,
    val threeStar: Int = 0,
    val twoStar: Int = 0,
    val oneStar: Int = 0
)
