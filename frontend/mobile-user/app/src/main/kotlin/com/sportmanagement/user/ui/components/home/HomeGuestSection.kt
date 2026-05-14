package com.sportmanagement.user.ui.components.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun HomeGuestSection(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Text(
        text = "Xin chào, Khách",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary
    )

    Spacer(Modifier.height(6.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HomeGuestActionButton(
            text = "Đăng nhập",
            filled = true,
            onClick = onLoginClick
        )
        HomeGuestActionButton(
            text = "Đăng ký",
            filled = false,
            onClick = onRegisterClick
        )
    }
}

@Composable
private fun HomeGuestActionButton(
    text: String,
    filled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (filled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    }

    val contentColor = if (filled) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier
            .height(30.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        border = if (filled) null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
