package com.sportmanagement.user.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmanagement.user.R

@Composable
internal fun HomeBookButton() {
    Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(
            containerColor = HomeKineticBlue
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Text(stringResource(R.string.home_book_button), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
internal fun HomeTagChip(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, HomeTagBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 10.sp, color = HomeKineticBlue, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun HomeSmallCircleIcon(icon: ImageVector, size: Int = 32) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(Color.White.copy(alpha = 0.9f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size((size * 0.55f).dp),
            tint = Color.DarkGray
        )
    }
}
