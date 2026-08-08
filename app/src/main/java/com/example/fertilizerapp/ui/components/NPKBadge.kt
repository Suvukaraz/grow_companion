package com.example.fertilizerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fertilizerapp.ui.theme.nitrogenColor
import com.example.fertilizerapp.ui.theme.phosphorusColor
import com.example.fertilizerapp.ui.theme.potassiumColor

@Composable
fun NPKBadgeRow(
    n: Double,
    p: Double,
    k: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        NPKChip(label = "N", value = n, color = MaterialTheme.colorScheme.nitrogenColor)
        NPKChip(label = "P", value = p, color = MaterialTheme.colorScheme.phosphorusColor)
        NPKChip(label = "K", value = k, color = MaterialTheme.colorScheme.potassiumColor)
    }
}

@Composable
fun NPKChip(
    label: String,
    value: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = if (value == value.toLong().toDouble()) "${value.toLong()}%" else "%.1f%%".format(value),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color.copy(alpha = 0.85f)
        )
    }
}

@Composable
fun NPKResultBadgeRow(
    npkResult: String,
    modifier: Modifier = Modifier
) {
    // Parse result string like "N: 0.15% | P: 0.10% | K: 0.15%"
    val parts = npkResult.split("|").map { it.trim() }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        parts.forEachIndexed { index, part ->
            val color = when (index) {
                0 -> MaterialTheme.colorScheme.nitrogenColor
                1 -> MaterialTheme.colorScheme.phosphorusColor
                else -> MaterialTheme.colorScheme.potassiumColor
            }
            Text(
                text = part,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}
