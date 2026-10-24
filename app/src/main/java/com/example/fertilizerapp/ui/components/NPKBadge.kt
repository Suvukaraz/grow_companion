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
import com.example.fertilizerapp.data.model.NpkValues
import com.example.fertilizerapp.ui.theme.nitrogenColor
import com.example.fertilizerapp.ui.theme.phosphorusColor
import com.example.fertilizerapp.ui.theme.potassiumColor
import com.example.fertilizerapp.util.formatCompact
import java.util.Locale
import kotlin.math.roundToInt

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
            text = "${value.formatCompact()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color.copy(alpha = 0.85f)
        )
    }
}

@Composable
fun NPKResultBadgeRow(
    npk: NpkValues?,
    modifier: Modifier = Modifier
) {
    if (npk == null) {
        Text(
            text = "Keine Zutaten oder Volumen 0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
        return
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NPKResultChip("N", npk.n, MaterialTheme.colorScheme.nitrogenColor)
            NPKResultChip("P", npk.p, MaterialTheme.colorScheme.phosphorusColor)
            NPKResultChip("K", npk.k, MaterialTheme.colorScheme.potassiumColor)
        }
        // Very diluted solutions round to 0.00 %, so also show mg/L
        Text(
            text = "≙ N ${formatPpm(npk.nPpm)} · P ${formatPpm(npk.pPpm)} · K ${formatPpm(npk.kPpm)} mg/L",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NPKResultChip(label: String, percent: Double, color: Color) {
    Text(
        text = "$label: ${formatPercent(percent)}%",
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = color
    )
}

private fun formatPercent(value: Double): String =
    String.format(Locale.ROOT, if (value != 0.0 && value < 0.1) "%.3f" else "%.2f", value)

private fun formatPpm(value: Double): String =
    if (value >= 10) value.roundToInt().toString() else value.formatCompact(1)
