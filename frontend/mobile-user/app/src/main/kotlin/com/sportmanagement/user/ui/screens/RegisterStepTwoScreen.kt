package com.sportmanagement.user.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
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
        SportOption("Bóng chuyền", drawableRes = R.drawable.volleyball_25, iconScale = 0.80f)
    )

    AuthScreenScaffold(
        title = "Đăng ký",
        onBackClick = onBackClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            RegisterStepProgressCompact(
                currentStep = 2,
                totalSteps = 2
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Môn thể thao yêu thích",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Chọn một hoặc nhiều môn thể thao để cá nhân hóa gợi ý sân và ưu đãi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))

            SportSelectionGrid(
                sports = sports,
                selectedSports = selectedSports,
                onToggleSport = onToggleSport,
                modifier = Modifier.fillMaxWidth()
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

            Spacer(Modifier.height(20.dp))

            AuthPrimaryButton(
                text = "Hoàn tất đăng ký",
                onClick = onNextClick
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Bỏ qua",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSkipClick)
                    .padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun RegisterStepProgressCompact(
    currentStep: Int,
    totalSteps: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val color = if (index < currentStep) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            }

            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(5.dp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(999.dp)
                    )
            )
        }
    }
}
