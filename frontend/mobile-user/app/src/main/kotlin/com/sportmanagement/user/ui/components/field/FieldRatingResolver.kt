package com.sportmanagement.user.ui.components.field

import com.sportmanagement.user.domain.model.FieldReview
import com.sportmanagement.user.domain.model.FieldReviewStats
import java.util.Locale

internal data class ResolvedFieldReviewMetrics(
    val averageRating: Double,
    val reviewCount: Int
)

internal fun resolveFieldReviewMetrics(
    reviewStats: FieldReviewStats?,
    reviews: List<FieldReview>,
    fallbackRating: String = "0.0"
): ResolvedFieldReviewMetrics {
    if (reviews.isNotEmpty()) {
        return ResolvedFieldReviewMetrics(
            averageRating = reviews.map { it.rating }.average(),
            reviewCount = reviews.size
        )
    }

    reviewStats?.takeIf { it.totalReviews > 0 }?.let { stats ->
        return ResolvedFieldReviewMetrics(
            averageRating = stats.averageRating,
            reviewCount = stats.totalReviews
        )
    }

    return ResolvedFieldReviewMetrics(
        averageRating = fallbackRating.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }
            ?: 0.0,
        reviewCount = 0
    )
}

internal fun formatFieldRating(value: Double): String {
    return if (value.isFinite() && value > 0.0) {
        String.format(Locale.US, "%.1f", value)
    } else {
        "0.0"
    }
}
