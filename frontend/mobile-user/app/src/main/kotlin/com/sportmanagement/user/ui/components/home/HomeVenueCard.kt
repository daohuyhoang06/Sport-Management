package com.sportmanagement.user.ui.components.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.domain.model.SportIconType
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.domain.model.VenueCardType
import com.sportmanagement.user.ui.components.SportCircleAvatar
import com.sportmanagement.user.ui.components.sportFieldDrawableRes
import com.sportmanagement.user.R
import com.sportmanagement.user.ui.theme.AppHomeVenueCornerRadius
import com.sportmanagement.user.ui.theme.AppMediaCornerRadius

@Composable
fun HomeVenueCard(
    field: UserField,
    displayRating: String = field.rating,
    isFavorite: Boolean,
    onCardClick: () -> Unit,
    onBookClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    when (field.cardType) {
        VenueCardType.LARGE_IMAGE -> LargeVenueCard(field, displayRating, isFavorite, onCardClick, onBookClick, onFavoriteClick, onShareClick)
        VenueCardType.SMALL_HORIZONTAL -> SmallHorizontalCard(field, displayRating, isFavorite, onCardClick, onBookClick, onFavoriteClick, onShareClick)
        VenueCardType.SMALL_HORIZONTAL_NO_IMAGE -> SmallNoImageCard(field, displayRating, isFavorite, onCardClick, onBookClick, onFavoriteClick, onShareClick)
    }
}

@Composable
private fun LargeVenueCard(
    field: UserField,
    displayRating: String,
    isFavorite: Boolean,
    onCardClick: () -> Unit,
    onBookClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(AppHomeVenueCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 7.4f)
            ) {
                Image(
                    painter = painterResource(id = sportFieldDrawableRes(field.sportIconType)),
                    contentDescription = field.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeSportNameChip(sportIconType = field.sportIconType)
                    HomeRatingChip(rating = displayRating)
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeSmallCircleIcon(
                        icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        onClick = onFavoriteClick,
                        tint = if (isFavorite) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HomeSmallCircleIcon(
                        icon = Icons.Default.Share,
                        onClick = onShareClick
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = Color.White,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                shape = RoundedCornerShape(AppHomeVenueCornerRadius)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    SportCircleAvatar(iconType = field.sportIconType)

                    Column(modifier = Modifier.weight(1f)) {
                        HomeVenueTitleText(text = field.name)
                        Spacer(Modifier.height(2.dp))
                        HomeVenueDistanceLocationText(
                            distance = field.distance,
                            location = field.location
                        )
                        Spacer(Modifier.height(2.dp))
                        HomeVenueHoursText(hours = field.hours)
                    }
                    HomeBookButton(
                        sportIconType = field.sportIconType,
                        onClick = onBookClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallHorizontalCard(
    field: UserField,
    displayRating: String,
    isFavorite: Boolean,
    onCardClick: () -> Unit,
    onBookClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(AppHomeVenueCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HomeSportNameChip(sportIconType = field.sportIconType)
                HomeRatingChip(rating = displayRating)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 42.dp, bottom = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = sportFieldDrawableRes(field.sportIconType)),
                    contentDescription = field.name,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(AppMediaCornerRadius)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(12.dp))

                SportCircleAvatar(
                    iconType = field.sportIconType,
                    size = 48.dp,
                    iconSize = 24.dp
                )
                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    HomeVenueTitleText(text = field.name)
                    Spacer(Modifier.height(2.dp))
                    HomeVenueDistanceLocationText(
                        distance = field.distance,
                        location = field.location
                    )
                    if (field.availability.isNotEmpty()) {
                        Text(
                            field.availability,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        HomeSmallCircleIcon(
                            icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            onClick = onFavoriteClick,
                            tint = if (isFavorite) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HomeSmallCircleIcon(
                            icon = Icons.Default.Share,
                            onClick = onShareClick
                        )
                    }
                    HomeBookButton(
                        sportIconType = field.sportIconType,
                        onClick = onBookClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallNoImageCard(
    field: UserField,
    displayRating: String,
    isFavorite: Boolean,
    onCardClick: () -> Unit,
    onBookClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(AppHomeVenueCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HomeSportNameChip(sportIconType = field.sportIconType)
                HomeRatingChip(rating = displayRating)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 42.dp, bottom = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                SportCircleAvatar(iconType = field.sportIconType, size = 60.dp, iconSize = 30.dp)

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    HomeVenueTitleText(text = field.name)
                    Spacer(Modifier.height(2.dp))
                    HomeVenueDistanceLocationText(
                        distance = field.distance,
                        location = field.location
                    )
                    if (field.availability.isNotEmpty()) {
                        Text(
                            field.availability,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        HomeSmallCircleIcon(
                            icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            onClick = onFavoriteClick,
                            tint = if (isFavorite) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HomeSmallCircleIcon(
                            icon = Icons.Default.Share,
                            onClick = onShareClick
                        )
                    }
                    HomeBookButton(
                        sportIconType = field.sportIconType,
                        onClick = onBookClick
                    )
                }
            }
        }
    }
}

