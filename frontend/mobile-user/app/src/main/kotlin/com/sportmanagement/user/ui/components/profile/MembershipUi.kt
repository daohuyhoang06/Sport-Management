package com.sportmanagement.user.ui.components.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale

data class MembershipUi(
    val label: String,
    val icon: ImageVector,
    val iconTint: Color,
    val chipBackground: Color,
    val chipBorder: Color,
    val textColor: Color
)

fun resolveMembershipUi(
    rawMembership: String,
    bookingCount: String? = null
): MembershipUi {
    val bookingCountValue = bookingCount
        ?.trim()
        ?.toIntOrNull()

    if (bookingCountValue != null) {
        if (bookingCountValue > 200) {
            return MembershipUi(
                label = "Vàng",
                icon = Icons.Filled.WorkspacePremium,
                iconTint = Color(0xFFB88900),
                chipBackground = Color(0xFFFFF4D6),
                chipBorder = Color(0xFFE2C66D),
                textColor = Color(0xFF9A6B00)
            )
        }

        if (bookingCountValue > 50) {
            return MembershipUi(
                label = "Bạc",
                icon = Icons.Filled.MilitaryTech,
                iconTint = Color(0xFF5A6B7C),
                chipBackground = Color(0xFFEAF0F7),
                chipBorder = Color(0xFFB2C1D1),
                textColor = Color(0xFF3D4B59)
            )
        }
    }

    val normalized = rawMembership.trim().lowercase(Locale.ROOT)
    return when (normalized) {
        "bạc", "bac" -> MembershipUi(
            label = "Bạc",
            icon = Icons.Filled.MilitaryTech,
            iconTint = Color(0xFF5A6B7C),
            chipBackground = Color(0xFFEAF0F7),
            chipBorder = Color(0xFFB2C1D1),
            textColor = Color(0xFF3D4B59)
        )
        "vàng", "vang" -> MembershipUi(
            label = "Vàng",
            icon = Icons.Filled.WorkspacePremium,
            iconTint = Color(0xFFB88900),
            chipBackground = Color(0xFFFFF4D6),
            chipBorder = Color(0xFFE2C66D),
            textColor = Color(0xFF9A6B00)
        )
        else -> MembershipUi(
            label = "Đồng",
            icon = Icons.Filled.MonetizationOn,
            iconTint = Color(0xFF9A5B35),
            chipBackground = Color(0xFFF8E4D6),
            chipBorder = Color(0xFFCF9A78),
            textColor = Color(0xFF7C4525)
        )
    }
}
