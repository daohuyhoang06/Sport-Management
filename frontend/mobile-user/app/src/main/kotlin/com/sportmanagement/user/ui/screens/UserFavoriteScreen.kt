package com.sportmanagement.user.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.UserField

@Composable
fun UserFavoriteScreen(
    padding: PaddingValues,
    favorites: List<UserField>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                stringResource(R.string.favorite_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        items(favorites) { item ->
            Card(shape = RoundedCornerShape(14.dp)) {
                androidx.compose.foundation.layout.Column(modifier = Modifier.padding(14.dp)) {
                    Text(item.name, fontWeight = FontWeight.SemiBold)
                    Text(item.location)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        stringResource(R.string.favorite_price_rating, item.price, item.rating),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
