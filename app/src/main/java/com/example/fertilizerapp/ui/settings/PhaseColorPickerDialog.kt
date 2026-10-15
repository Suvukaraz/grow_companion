package com.example.fertilizerapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

val PRESET_COLORS = listOf(
    Color(0xFF8BC34A), // Light Green
    Color(0xFF43A047), // Green
    Color(0xFF2E7D32), // Dark Green
    Color(0xFF009688), // Teal
    Color(0xFF00BCD4), // Cyan
    Color(0xFF03A9F4), // Light Blue
    Color(0xFF1976D2), // Blue
    Color(0xFF3F51B5), // Indigo
    Color(0xFFAB47BC), // Purple
    Color(0xFF673AB7), // Deep Purple
    Color(0xFFE91E63), // Pink
    Color(0xFFF44336), // Red
    Color(0xFFFF5722), // Deep Orange
    Color(0xFFFF9800), // Orange
    Color(0xFFFFCA28), // Gold
    Color(0xFFFFEB3B), // Yellow
    Color(0xFF795548), // Brown
    Color(0xFF9E9E9E)  // Grey
)

@Composable
fun PhaseColorPickerDialog(
    phaseTitle: String,
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onResetDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Palette,
                contentDescription = null,
                tint = selectedColor
            )
        },
        title = {
            Text(
                text = "Farbe für '$phaseTitle'",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Preview Badge
                Surface(
                    color = selectedColor,
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 2.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = phaseTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Grid of preset colors
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    items(PRESET_COLORS) { color ->
                        val isSelected = color.toArgb() == selectedColor.toArgb()
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Ausgewählt",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(selectedColor)
                    onDismiss()
                }
            ) {
                Text("Umschalten")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        onResetDefault()
                        onDismiss()
                    }
                ) {
                    Text("Standard")
                }
                TextButton(onClick = onDismiss) {
                    Text("Abbrechen")
                }
            }
        }
    )
}
