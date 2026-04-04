package com.sportmanagement.user.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.ui.components.StatPill
import com.sportmanagement.user.ui.model.UserProfile
import com.sportmanagement.user.ui.model.UserStat

@Composable
fun UserProfileScreen(
    padding: PaddingValues,
    profile: UserProfile,
    stats: List<UserStat>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Tài khoản", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Tên: ${profile.name}")
                Text("Email: ${profile.email}")
                Text("SĐT: ${profile.phone}")
                Text("Hạng thành viên: ${profile.membership}")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            stats.forEach { stat ->
                StatPill(stat.value, stat.label)
            }
        }
    }
}
