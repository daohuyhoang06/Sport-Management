package com.sportmanagement.user.ui.components.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.ui.theme.AppAccentCitrus
import com.sportmanagement.user.ui.theme.AppCardCornerRadius
import com.sportmanagement.user.ui.theme.AppPillCornerRadius

@Composable
fun ProfileHeaderSection(
    profile: UserProfile,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    // =========================
    // MAIN CONTAINER
    // =========================
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {

        // =========================================================
        // BANNER IMAGE
        // =========================================================
        // Đây là ảnh nền phía trên cùng
        // Chứa background runner giống design
        // =========================================================
        Image(
            painter = painterResource(id = R.drawable.banner_app),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            contentScale = ContentScale.FillBounds
        )

        // =========================================================
        // TITLE + NOTIFICATION
        // =========================================================
        // Phần tiêu đề "Tài khoản"
        // và icon chuông thông báo
        // =========================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // =========================
            // TITLE
            // =========================
            Text(
                text = "Tài khoản",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            // =========================
            // NOTIFICATION ICON
            // =========================
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        // =========================================================
        // PROFILE CARD
        // =========================================================
        // Card trắng chứa:
        // - Avatar
        // - Tên user
        // - Membership
        // - Email
        // - Phone
        // - Button chỉnh sửa
        // =========================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 110.dp),
            shape = RoundedCornerShape(AppCardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                // =================================================
                // TOP PROFILE INFO
                // =================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // =========================================
                        // AVATAR
                        // =========================================
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(40.dp)
                            )

                            // =====================================
                            // CAMERA ICON
                            // =====================================
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(22.dp)
                                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {

                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // =========================================
                        // USER NAME + MEMBERSHIP
                        // =========================================
                        Column {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = profile.name.ifBlank { "Nguyễn Văn An" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                            }
                        }
                    }

                    // =============================================
                    // EDIT BUTTON
                    // =============================================
                    Surface(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(42.dp)
                            .clickable(onClick = onEditClick),
                        shape = RoundedCornerShape(AppPillCornerRadius),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                            // Edit icon
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Button text
                            Text(
                                text = "Chỉnh sửa",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // =================================================
                // EMAIL
                // =================================================
                ProfileInfoRow(
                    icon = Icons.Outlined.Email,
                    text = profile.email.ifBlank { "user1@gmail.com" }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // =================================================
                // PHONE
                // =================================================
                ProfileInfoRow(
                    icon = Icons.Outlined.Phone,
                    text = profile.phone.ifBlank { "0907890123" }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // =================================================
                // MEMBERSHIP INFO
                // =================================================
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Hạng thành viên: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = profile.membership.ifBlank { "Vàng" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // =================================================
                // STATS CARD SECTION
                // =================================================
                // Bao gồm:
                // - Lần đặt
                // - Điểm uy tín
                // =================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    StatsItem(
                        modifier = Modifier.weight(1f),
                        value = profile.bookingCount.ifBlank { "12" },
                        label = "Lần đặt",
                        icon = Icons.Outlined.CalendarMonth
                    )

                    StatsItem(
                        modifier = Modifier.weight(1f),
                        value = profile.rating.ifBlank { "4.8" },
                        label = "Điểm uy tín",
                        icon = Icons.Outlined.Star
                    )
                }
            }
        }
    }
}

// =====================================================
// PROFILE INFO ROW
// =====================================================
// Dùng cho:
// - email
// - phone
// =====================================================
@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    text: String
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// =====================================================
// STATS ITEM
// =====================================================
// Card vàng cho:
// - Lần đặt
// - Điểm uy tín
// =====================================================
@Composable
private fun StatsItem(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = AppAccentCitrus
        )
    ) {

        Column(
            modifier = Modifier.padding(14.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}