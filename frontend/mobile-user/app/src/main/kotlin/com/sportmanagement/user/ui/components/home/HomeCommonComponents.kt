package com.sportmanagement.user.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.R
import com.sportmanagement.user.ui.theme.AppCtaAmber
import com.sportmanagement.user.ui.theme.AppCtaCompactHorizontalPadding
import com.sportmanagement.user.ui.theme.AppCtaCompactVerticalPadding
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppMediaCornerRadius
import com.sportmanagement.user.ui.theme.AppOnCtaAmber

@Composable
internal fun HomeBookButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.defaultMinSize(minWidth = 64.dp, minHeight = 30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppCtaAmber,
            contentColor = AppOnCtaAmber
        ),
        shape = RoundedCornerShape(AppCtaCornerRadius),
        contentPadding = PaddingValues(
            horizontal = (AppCtaCompactHorizontalPadding - 3.dp),
            vertical = (AppCtaCompactVerticalPadding - 1.dp)
        )
    ) {
        Text(
            text = stringResource(R.string.home_book_button),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
internal fun HomeTagChip(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(AppMediaCornerRadius))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
internal fun HomeSmallCircleIcon(icon: ImageVector, size: Int = 30) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size((size * 0.5f).dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
