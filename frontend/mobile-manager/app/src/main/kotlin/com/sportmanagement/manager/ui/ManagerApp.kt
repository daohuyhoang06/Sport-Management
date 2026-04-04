package com.sportmanagement.manager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Reviews
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ManagerBooking(val slot: String, val customer: String, val status: String)
data class ManagerReview(val user: String, val score: String, val comment: String)

private enum class ManagerTab(val title: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Default.Home),
    Schedule("Schedule", Icons.Default.EventNote),
    Reviews("Reviews", Icons.Default.Reviews),
    Reports("Reports", Icons.Default.Analytics)
}

@Composable
fun ManagerApp() {
    var selectedTab by remember { mutableStateOf(ManagerTab.Dashboard) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                ManagerTab.entries.forEach { tab ->
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
            ManagerTab.Dashboard -> ManagerDashboard(padding)
            ManagerTab.Schedule -> ManagerSchedule(padding)
            ManagerTab.Reviews -> ManagerReviews(padding)
            ManagerTab.Reports -> ManagerReports(padding)
        }
    }
}

@Composable
private fun ManagerDashboard(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background)
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Manager Dashboard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Tong quan van hanh trong ngay")

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("24", "Booking")
            MetricCard("91%", "Ty le lap day")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("7.8tr", "Doanh thu")
            MetricCard("4.7", "Diem review")
        }
    }
}

@Composable
private fun MetricCard(value: String, label: String) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(102.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label)
        }
    }
}

@Composable
private fun ManagerSchedule(padding: PaddingValues) {
    val list = listOf(
        ManagerBooking("17:00 - 18:30", "Nguyen Van B", "Checked-in"),
        ManagerBooking("19:00 - 20:30", "Tran Minh C", "Da thanh toan"),
        ManagerBooking("21:00 - 22:30", "Le Hoang D", "Cho xac nhan")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Lich dat hom nay", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        items(list) { booking ->
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(booking.slot, fontWeight = FontWeight.SemiBold)
                    Text("Khach: ${booking.customer}")
                    Text("Trang thai: ${booking.status}", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun ManagerReviews(padding: PaddingValues) {
    val reviews = listOf(
        ManagerReview("Pham T.", "5/5", "San dep, den sang tot"),
        ManagerReview("Khanh N.", "4/5", "Nhan vien ho tro nhanh"),
        ManagerReview("Minh H.", "3/5", "Can bo sung nuoc uong")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Danh gia moi", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        items(reviews) { review ->
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(review.user, fontWeight = FontWeight.SemiBold)
                    Text(review.score, color = MaterialTheme.colorScheme.primary)
                    Text(review.comment)
                }
            }
        }
    }
}

@Composable
private fun ManagerReports(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Bao cao nhanh", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Card(shape = RoundedCornerShape(14.dp)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Khung gio dong nhat")
                Text("19:00 - 20:30", fontWeight = FontWeight.SemiBold)
            }
        }

        Card(shape = RoundedCornerShape(14.dp)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Top san trong tuan")
                Text("Arena Alpha - 87 luot", fontWeight = FontWeight.SemiBold)
            }
        }

        Card(shape = RoundedCornerShape(14.dp)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Can xu ly")
                Text("2 booking cho xac nhan", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
