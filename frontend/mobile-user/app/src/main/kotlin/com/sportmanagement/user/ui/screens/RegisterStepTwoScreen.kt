package com.sportmanagement.user.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Pool
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.R
import com.sportmanagement.user.ui.components.auth.SportOption
import com.sportmanagement.user.ui.components.auth.SportSelectionGrid
import com.sportmanagement.user.ui.theme.AppCardCornerRadius

@Composable
fun RegisterStepTwoScreen(
    selectedSports: Set<String>,
    onToggleSport: (String) -> Unit,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val sports = listOf(
        SportOption("Bóng đá", drawableRes = R.drawable.football_25, iconScale = 0.96f),
        SportOption("Cầu lông", drawableRes = R.drawable.badminton_25, iconScale = 0.95f),
        SportOption("Tennis", drawableRes = R.drawable.tennis_25, iconScale = 0.94f),
        SportOption("Pickleball", drawableRes = R.drawable.pickleball, iconScale = 0.86f),
        SportOption("Bóng rổ", iconVector = Icons.Outlined.SportsBasketball, iconScale = 0.94f),
        SportOption("Bơi lội", iconVector = Icons.Outlined.Pool, iconScale = 0.94f),
        SportOption("Bóng chuyền", drawableRes = R.drawable.volleyball_25, iconScale = 0.80f),
        SportOption("Gym", iconVector = Icons.Outlined.FitnessCenter, iconScale = 0.92f)
    )

    RegisterStepScaffold(
        currentStep = 2,
        title = "Chọn môn thể thao yêu thích của bạn",
        subtitle = "Chọn một hoặc nhiều môn thể thao mà bạn yêu thích. Chúng tôi sẽ cá nhân hóa trải nghiệm dành cho bạn.",
        onBackClick = onBackClick,
        primaryButtonText = "Tiếp theo",
        secondaryButtonText = "Bỏ qua",
        onPrimaryClick = onNextClick,
        onSecondaryClick = onSkipClick
    ) {
        SportSelectionGrid(
            sports = sports,
            selectedSports = selectedSports,
            onToggleSport = onToggleSport,
            modifier = Modifier.height(328.dp)
        )

        Spacer(Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppCardCornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                        .padding(8.dp)
                )
                Column {
                    Text(
                        text = "Lợi ích khi chọn môn yêu thích",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Chúng tôi sẽ gợi ý sân, ưu đãi và nội dung phù hợp với sở thích của bạn.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
