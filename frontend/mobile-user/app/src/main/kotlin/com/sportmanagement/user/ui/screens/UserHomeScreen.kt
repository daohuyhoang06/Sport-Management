package com.sportmanagement.user.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmanagement.user.ui.model.SportCategory
import com.sportmanagement.user.ui.model.SportIconType
import com.sportmanagement.user.ui.model.UserField
import com.sportmanagement.user.ui.model.VenueCardType

private val KineticBlue = Color(0xFF1A4B8E)
private val KineticDarkBlue = Color(0xFF0D3B6E)
private val TagBorder = Color(0xFFBBDEFB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    padding: PaddingValues,
    fields: List<UserField>,
    sportCategories: List<SportCategory>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(-1) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color.White),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Banner header + search
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                Image(
                    painter = painterResource(id = com.sportmanagement.user.R.drawable.banner_app),
                    contentDescription = "Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmallCircleIcon(Icons.Default.Star, size = 28)
                        Spacer(Modifier.width(8.dp))
                        SmallCircleIcon(Icons.Default.Notifications, size = 28)
                    }

                    Spacer(Modifier.height(28.dp))

                    Text(
                        "Thứ sáu, 24/04/2026",
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Đào Huy Hoàng",
                        color = Color(0xFFFFF176),
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 24.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shadowElevation = 6.dp,
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Tìm kiếm",
                                    color = Color(0xFF616161),
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            },
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 14.sp
                            ),
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = KineticBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = KineticDarkBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedTextColor = Color(0xFF212121),
                                focusedTextColor = Color(0xFF212121),
                                unfocusedPlaceholderColor = Color(0xFF616161),
                                focusedPlaceholderColor = Color(0xFF616161),
                                cursorColor = KineticBlue
                            ),
                            singleLine = true
                        )
                    }

                    Surface(
                        modifier = Modifier.size(44.dp),
                        shadowElevation = 6.dp,
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = KineticBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }

        // Sport categories row
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sportCategories.size) { index ->
                    SportCategoryItem(
                        category = sportCategories[index],
                        isSelected = selectedCategoryIndex == index,
                        onClick = {
                            selectedCategoryIndex = if (selectedCategoryIndex == index) -1 else index
                        }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Venue cards
        items(fields) { field ->
            when (field.cardType) {
                VenueCardType.LARGE_IMAGE -> LargeVenueCard(field)
                VenueCardType.SMALL_HORIZONTAL -> SmallHorizontalCard(field)
                VenueCardType.SMALL_HORIZONTAL_NO_IMAGE -> SmallNoImageCard(field)
            }
            Spacer(Modifier.height(12.dp))
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
        targetValue = if (isSelected) KineticBlue else Color.White,
        animationSpec = tween(300), label = "bgColor"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else KineticBlue,
        animationSpec = tween(300), label = "iconColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) KineticBlue else Color.DarkGray,
        animationSpec = tween(300), label = "textColor"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 3.dp,
        animationSpec = tween(300), label = "elevation"
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
            border = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 0.dp else 1.5.dp,
                color = if (isSelected) Color.Transparent else KineticBlue.copy(alpha = 0.35f)
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
        SportIconType.FOOTBALL -> com.sportmanagement.user.R.drawable.football_25
        SportIconType.PICKLEBALL -> com.sportmanagement.user.R.drawable.pickleball
        SportIconType.TENNIS -> com.sportmanagement.user.R.drawable.tennis_25
        SportIconType.BADMINTON -> com.sportmanagement.user.R.drawable.badminton_25
        SportIconType.VOLLEYBALL -> com.sportmanagement.user.R.drawable.volleyball_25
    }
}

@Composable
private fun LargeVenueCard(field: UserField) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Field image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                Image(
                    painter = painterResource(id = com.sportmanagement.user.R.drawable.field_default),
                    contentDescription = field.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // PRO LEAGUE badge
                if (field.isProLeague) {
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .background(KineticDarkBlue, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "PRO LEAGUE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Rating badge
                if (field.isProLeague) {
                    Row(
                        modifier = Modifier
                            .padding(start = 120.dp, top = 12.dp)
                            .background(Color(0xFF1B5E20).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD600),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(field.rating, color = Color.White, fontSize = 12.sp)
                    }
                }

                // Favorite + share icons
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallCircleIcon(Icons.Default.FavoriteBorder)
                    SmallCircleIcon(Icons.Default.Share)
                }
            }

            // Info section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        field.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${field.location} â€¢ ${field.distance}",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(field.hours, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                BookButton()
            }
        }
    }
}

@Composable
private fun SmallHorizontalCard(field: UserField) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Field image
            Image(
                painter = painterResource(id = com.sportmanagement.user.R.drawable.field_default),
                contentDescription = field.name,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    field.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    field.distance,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (field.availability.isNotEmpty()) {
                    Text(
                        field.availability,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                if (field.tags.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        field.tags.forEach { tag ->
                            TagChip(tag)
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SmallCircleIcon(Icons.Default.FavoriteBorder, size = 28)
                    SmallCircleIcon(Icons.Default.Share, size = 28)
                }
                BookButton()
            }
        }
    }
}

@Composable
private fun SmallNoImageCard(field: UserField) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle avatar placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SportsTennis,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    field.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${field.distance} â€¢",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (field.availability.isNotEmpty()) {
                    Text(
                        field.availability,
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
                if (field.tags.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        field.tags.forEach { tag ->
                            TagChip(tag)
                        }
                    }
                }
            }

            BookButton()
        }
    }
}

@Composable
private fun BookButton() {
    Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(
            containerColor = KineticBlue
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Text("BOOK", fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun TagChip(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, TagBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 10.sp, color = KineticBlue, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SmallCircleIcon(icon: ImageVector, size: Int = 32) {
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
