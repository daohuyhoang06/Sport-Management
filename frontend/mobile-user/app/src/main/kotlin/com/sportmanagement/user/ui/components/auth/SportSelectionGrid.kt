package com.sportmanagement.user.ui.components.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.max

private val SportGridHorizontalSpacing = 10.dp
private val SportGridVerticalSpacing = 10.dp
private val SportGridMinCardWidth = 112.dp
private const val SportGridMaxColumns = 4

@Composable
fun SportSelectionGrid(
    sports: List<SportOption>,
    selectedSports: Set<String>,
    onToggleSport: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = calculateSportGridColumns(maxWidth)
        val rows = ((sports.size + columns - 1) / columns).coerceAtLeast(1)
        val cardSize = (maxWidth - SportGridHorizontalSpacing * (columns - 1)) / columns
        val gridHeight = cardSize * rows + SportGridVerticalSpacing * max(0, rows - 1)

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight),
            horizontalArrangement = Arrangement.spacedBy(SportGridHorizontalSpacing),
            verticalArrangement = Arrangement.spacedBy(SportGridVerticalSpacing),
            userScrollEnabled = false
        ) {
            items(sports, key = { it.name }) { sport ->
                SportCard(
                    sport = sport,
                    selected = selectedSports.contains(sport.name),
                    onClick = { onToggleSport(sport.name) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun calculateSportGridColumns(maxWidth: androidx.compose.ui.unit.Dp): Int {
    val rawColumns = ((maxWidth + SportGridHorizontalSpacing) /
        (SportGridMinCardWidth + SportGridHorizontalSpacing)).toInt()
    return rawColumns.coerceIn(1, SportGridMaxColumns)
}
