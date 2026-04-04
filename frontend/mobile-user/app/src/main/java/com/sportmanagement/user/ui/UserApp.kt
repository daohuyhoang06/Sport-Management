package com.sportmanagement.user.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class UserField(val name: String, val location: String, val price: String, val rating: String)
data class UserBooking(val fieldName: String, val dateTime: String, val status: String)

private enum class UserTab(val title: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    Booking("Booking", Icons.Default.BookOnline),
    History("History", Icons.Default.History),
    Profile("Profile", Icons.Default.Person)
}

@Composable
fun UserApp() {
    var selectedTab by remember { mutableStateOf(UserTab.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                UserTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            UserTab.Home -> UserHomeScreen(padding)
            UserTab.Booking -> UserBookingScreen(padding)
            UserTab.History -> UserHistoryScreen(padding)
            UserTab.Profile -> UserProfileScreen(padding)
        }
    }
}

@Composable
private fun UserHomeScreen(padding: PaddingValues) {
    val fields = listOf(
        UserField("Arena Alpha", "Quan 1", "250.000đ/h", "4.8"),
        UserField("Green Pitch", "Quan 7", "220.000đ/h", "4.6"),
        UserField("Sunlight Field", "Thu Duc", "280.000đ/h", "4.9")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Xin chao, ban A", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Dat san nhanh trong 30 giay",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(fields) { field ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(field.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text("Khu vuc: ${field.location}")
                    Text("Gia: ${field.price}")
                    Text("Danh gia: ${field.rating}/5")
                }
            }
        }
    }
}

@Composable
private fun UserBookingScreen(padding: PaddingValues) {
    val slots = listOf("17:00 - 18:30", "19:00 - 20:30", "21:00 - 22:30")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Dat san", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Chon khung gio con trong tai Arena Alpha")

        slots.forEach { slot ->
            Card(shape = RoundedCornerShape(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(slot, style = MaterialTheme.typography.titleMedium)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Dat ngay", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserHistoryScreen(padding: PaddingValues) {
    val history = listOf(
        UserBooking("Arena Alpha", "03/04/2026 19:00", "Da thanh toan"),
        UserBooking("Green Pitch", "30/03/2026 17:00", "Hoan tat"),
        UserBooking("Night Pro", "26/03/2026 21:00", "Da huy")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Lich su dat san", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        items(history) { item ->
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(item.fieldName, fontWeight = FontWeight.SemiBold)
                    Text(item.dateTime)
                    Text(item.status, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun UserProfileScreen(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Tai khoan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ten: Nguyen Van A")
                Text("Email: user@sport.local")
                Text("SDT: 09xx xxx xxx")
                Text("Hang thanh vien: Gold")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatPill("12", "Lan dat")
            StatPill("4.8", "Diem uy tin")
        }
    }
}

@Composable
private fun StatPill(value: String, label: String) {
    Card(
        modifier = Modifier.size(width = 150.dp, height = 82.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label)
        }
    }
}
