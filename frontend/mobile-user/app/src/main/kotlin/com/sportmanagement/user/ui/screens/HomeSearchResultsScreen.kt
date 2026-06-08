package com.sportmanagement.user.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.ui.components.field.FieldDetailBottomSheet
import com.sportmanagement.user.ui.components.home.HomeVenueCard
import com.sportmanagement.user.ui.theme.AppInputCornerRadius
import java.text.Normalizer

@Composable
fun HomeSearchResultsScreen(
    padding: PaddingValues,
    fields: List<UserField>,
    favoriteFields: List<UserField>,
    isLoading: Boolean,
    title: String,
    emptyTitle: String,
    emptyBody: String,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit,
    onBookFieldClick: (UserField) -> Unit,
    onFavoriteFieldClick: (UserField, Boolean) -> Unit,
    onShareFieldClick: (UserField) -> Unit,
    showFilterButton: Boolean = true
) {
    BackHandler(onBack = onBackClick)

    val headerHeight = 118.dp
    val contentTopPadding = 158.dp
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFieldForDetail by remember { mutableStateOf<UserField?>(null) }
    val favoriteFieldIds = remember(favoriteFields) { favoriteFields.map { it.fieldId }.toSet() }
    val fieldSearchIndex = remember(fields) {
        fields.map { field ->
            SearchableResultsField(
                field = field,
                normalizedName = normalizeResultsSearch(field.name),
                normalizedLocation = normalizeResultsSearch(field.location)
            )
        }
    }
    val normalizedQuery = remember(searchQuery) { normalizeResultsSearch(searchQuery) }
    val visibleFields = remember(fieldSearchIndex, normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            fieldSearchIndex.map(SearchableResultsField::field)
        } else {
            fieldSearchIndex.filter { entry ->
                entry.normalizedName.contains(normalizedQuery) ||
                    entry.normalizedLocation.contains(normalizedQuery)
            }.map(SearchableResultsField::field)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = padding.calculateBottomPadding())
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLoading && fields.isEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = contentTopPadding, bottom = 20.dp)
            ) {
                items(4) {
                    HomeVenueSkeletonCard()
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        } else if (visibleFields.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = contentTopPadding, bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                SearchResultsEmptyState(
                    title = emptyTitle,
                    body = emptyBody
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = contentTopPadding, bottom = 20.dp)
            ) {
                items(visibleFields, key = { "field_${it.fieldId}_${it.name}" }) { field ->
                    HomeVenueCard(
                        field = field,
                        isFavorite = field.fieldId in favoriteFieldIds,
                        onCardClick = { selectedFieldForDetail = field },
                        onBookClick = { onBookFieldClick(field) },
                        onFavoriteClick = {
                            onFavoriteFieldClick(field, field.fieldId !in favoriteFieldIds)
                        },
                        onShareClick = { onShareFieldClick(field) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .zIndex(1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.banner_app),
                contentDescription = stringResource(R.string.home_banner_content_description),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.08f))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-4).dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.booking_back_content_description),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    if (showFilterButton) {
                        IconButton(
                            onClick = onFilterClick,
                            modifier = Modifier
                                .size(36.dp)
                                .align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = stringResource(R.string.home_filter_content_description),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        Spacer(
                            modifier = Modifier
                                .size(36.dp)
                                .align(Alignment.CenterEnd)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 24.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shadowElevation = 6.dp,
                shape = RoundedCornerShape(AppInputCornerRadius),
                color = MaterialTheme.colorScheme.surface
            ) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 13.dp),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isBlank()) {
                                    Text(
                                        text = stringResource(R.string.home_search_results_search_placeholder),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                innerTextField()
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }
        }

        selectedFieldForDetail?.let { selectedField ->
            FieldDetailBottomSheet(
                field = selectedField,
                isFavorite = selectedField.fieldId in favoriteFieldIds,
                onDismissRequest = { selectedFieldForDetail = null },
                onFavoriteClick = {
                    onFavoriteFieldClick(selectedField, selectedField.fieldId !in favoriteFieldIds)
                },
                onBookClick = { field ->
                    selectedFieldForDetail = null
                    onBookFieldClick(field)
                }
            )
        }
    }
}

private data class SearchableResultsField(
    val field: UserField,
    val normalizedName: String,
    val normalizedLocation: String
)

@Composable
private fun SearchResultsEmptyState(
    title: String,
    body: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_search_empty_state),
            contentDescription = null,
            modifier = Modifier.size(220.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
            textAlign = TextAlign.Center
        )
    }
}

private fun normalizeResultsSearch(text: String): String {
    val normalized = Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
    return normalized.replace("đ", "d").replace("\\p{M}+".toRegex(), "")
}
