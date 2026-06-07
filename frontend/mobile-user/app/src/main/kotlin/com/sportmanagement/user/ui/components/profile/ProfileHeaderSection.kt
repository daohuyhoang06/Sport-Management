package com.sportmanagement.user.ui.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.ui.theme.AppHomeVenueCornerRadius
import com.sportmanagement.user.ui.theme.responsiveSharedTitleStyle

@Composable
fun ProfileHeaderSection(
    profile: UserProfile,
    onEditClick: () -> Unit,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isLoggedIn) 392.dp else 420.dp)
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.banner_app),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 96.dp),
            shape = RoundedCornerShape(AppHomeVenueCornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF1F4FC)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (isLoggedIn) {
                    LoggedInProfileContent(profile = profile, onEditClick = onEditClick)
                } else {
                    ProfileGuestSection(
                        onLoginClick = onLoginClick,
                        onRegisterClick = onRegisterClick
                    )
                }
            }
        }
    }
}

@Composable
private fun LoggedInProfileContent(
    profile: UserProfile,
    onEditClick: () -> Unit
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val compactScreen = screenWidthDp < 360
    val mediumScreen = screenWidthDp in 360 until 400
    val avatarSize = when {
        compactScreen -> 58.dp
        mediumScreen -> 62.dp
        else -> 66.dp
    }
    val avatarIconSize = when {
        compactScreen -> 26.dp
        mediumScreen -> 28.dp
        else -> 30.dp
    }
    val headerSpacing = when {
        compactScreen -> 8.dp
        mediumScreen -> 10.dp
        else -> 12.dp
    }
    val headerNameStyle = MaterialTheme.typography.titleLarge.copy(
        fontSize = when {
            compactScreen -> 18.sp
            mediumScreen -> 19.sp
            else -> 20.sp
        },
        lineHeight = when {
            compactScreen -> 21.sp
            mediumScreen -> 22.sp
            else -> 23.sp
        }
    )
    val profileInfoStyle = responsiveSharedTitleStyle(screenWidthDp)

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            onClick = onEditClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp),
            shape = RoundedCornerShape(7.dp),
            color = Color.Transparent,
            border = null
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_edit_30),
                contentDescription = "Chỉnh sửa",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(18.dp)
                    .padding(2.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 46.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (profile.avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = profile.avatarUrl,
                        contentDescription = "Ảnh đại diện",
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(avatarIconSize)
                    )
                }
            }

            Spacer(modifier = Modifier.width(headerSpacing))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp)
            ) {
                Text(
                    text = profile.name.ifBlank { "Người dùng" },
                    style = headerNameStyle,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    softWrap = true,
                    overflow = TextOverflow.Clip
                )
                Spacer(modifier = Modifier.height(4.dp))
                MembershipBadge(
                    membership = profile.membership,
                    bookingCount = profile.bookingCount
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    ProfileInfoRow(
        icon = Icons.Outlined.Email,
        text = profile.email,
        textStyle = profileInfoStyle
    )
    Spacer(modifier = Modifier.height(10.dp))
    ProfileInfoRow(
        icon = Icons.Outlined.Phone,
        text = profile.phone,
        textStyle = profileInfoStyle
    )

    Spacer(modifier = Modifier.height(18.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.CalendarMonth,
            value = profile.bookingCount.ifBlank { "0" },
            valueTextStyle = profileInfoStyle,
            label = "Lần đặt"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.Star,
            value = profile.rating.ifBlank { "0.0" },
            valueTextStyle = profileInfoStyle,
            label = "Đánh giá"
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    text: String,
    textStyle: androidx.compose.ui.text.TextStyle
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            style = textStyle,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            softWrap = true,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    icon: ImageVector,
    value: String,
    valueTextStyle: androidx.compose.ui.text.TextStyle,
    label: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8FAFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDE3F0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = value,
                    style = valueTextStyle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MembershipBadge(
    membership: String,
    bookingCount: String
) {
    val membershipUi = resolveMembershipUi(
        rawMembership = membership,
        bookingCount = bookingCount
    )
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = membershipUi.chipBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, membershipUi.chipBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = membershipUi.icon,
                contentDescription = null,
                tint = membershipUi.iconTint,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = membershipUi.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                ),
                color = membershipUi.textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
