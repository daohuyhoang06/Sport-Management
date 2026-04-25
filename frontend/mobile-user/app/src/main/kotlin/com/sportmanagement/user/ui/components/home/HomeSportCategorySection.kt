package com.sportmanagement.user.ui.components.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.SportIconType

@Composable
fun HomeSportCategorySection(
    sportCategories: List<SportCategory>,
    selectedCategoryIndex: Int,
    onCategorySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(sportCategories) { index, category ->
            SportCategoryItem(
                category = category,
                isSelected = selectedCategoryIndex == index,
                onClick = { onCategorySelected(index) }
            )
        }
    }
}

@Composable
private fun SportCategoryItem(
    category: SportCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) HomeKineticBlue else Color.White,
        animationSpec = tween(300),
        label = "bgColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) HomeKineticBlue else Color.DarkGray,
        animationSpec = tween(300),
        label = "textColor"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 3.dp,
        animationSpec = tween(300),
        label = "elevation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(10.dp),
            shadowElevation = elevation,
            color = bgColor,
            border = BorderStroke(
                width = if (isSelected) 0.dp else 1.5.dp,
                color = if (isSelected) Color.Transparent else HomeKineticBlue.copy(alpha = 0.35f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = getSportDrawable(category.iconType)),
                    contentDescription = category.name,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            category.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

private fun getSportDrawable(type: SportIconType): Int {
    return when (type) {
        SportIconType.FOOTBALL -> R.drawable.football_25
        SportIconType.PICKLEBALL -> R.drawable.pickleball
        SportIconType.TENNIS -> R.drawable.tennis_25
        SportIconType.BADMINTON -> R.drawable.badminton_25
        SportIconType.VOLLEYBALL -> R.drawable.volleyball_25
    }
}
