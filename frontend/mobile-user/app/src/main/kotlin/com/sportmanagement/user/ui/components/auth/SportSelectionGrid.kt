package com.sportmanagement.user.ui.components.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SportSelectionGrid(
    sports: List<SportOption>,
    selectedSports: Set<String>,
    onToggleSport: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        userScrollEnabled = false
    ) {
        items(sports, key = { it.name }) { sport ->
            SportCard(
                sport = sport,
                selected = selectedSports.contains(sport.name),
                onClick = { onToggleSport(sport.name) },
                modifier = Modifier.height(158.dp)
            )
        }
    }
}
