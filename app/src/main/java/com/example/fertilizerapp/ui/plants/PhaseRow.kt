package com.example.fertilizerapp.ui.plants

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fertilizerapp.data.model.ObjectPhase
import com.example.fertilizerapp.ui.theme.floweringPhase
import com.example.fertilizerapp.ui.theme.harvestPhase
import com.example.fertilizerapp.ui.theme.seedlingPhase
import com.example.fertilizerapp.ui.theme.vegetativePhase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhaseRow(
    ph: ObjectPhase,
    allNames: List<String>,
    onUp: (ObjectPhase) -> Unit,
    onDel: () -> Unit
) {
    val ctx = LocalContext.current
    val date = try { LocalDate.parse(ph.dateIso) } catch (e: Exception) { LocalDate.now() }
    
    val phaseColor = when {
        ph.name.contains("Keimling", ignoreCase = true) || ph.name.contains("Sämling", ignoreCase = true) -> MaterialTheme.colorScheme.seedlingPhase
        ph.name.contains("Veg", ignoreCase = true) -> MaterialTheme.colorScheme.vegetativePhase
        ph.name.contains("Blüte", ignoreCase = true) -> MaterialTheme.colorScheme.floweringPhase
        ph.name.contains("Ernte", ignoreCase = true) -> MaterialTheme.colorScheme.harvestPhase
        else -> MaterialTheme.colorScheme.outline
    }

    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                var exp by remember { mutableStateOf(false) }
                
                Box(Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = ph.name,
                        onValueChange = { onUp(ph.copy(name = it)) },
                        label = { Text("Phase") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = phaseColor,
                            unfocusedBorderColor = phaseColor.copy(alpha = 0.5f)
                        ),
                        trailingIcon = { 
                            if (allNames.isNotEmpty()) {
                                IconButton(onClick = { exp = true }) { 
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown") 
                                } 
                            }
                        }
                    )
                    
                    DropdownMenu(
                        expanded = exp,
                        onDismissRequest = { exp = false }
                    ) {
                        allNames.forEach { s -> 
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = { onUp(ph.copy(name = s)); exp = false }
                            ) 
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(onClick = onDel) { 
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    ) 
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { 
                        DatePickerDialog(
                            ctx,
                            { _, y, m, d -> 
                                onUp(ph.copy(dateIso = LocalDate.of(y, m + 1, d).toString())) 
                            },
                            date.year,
                            date.monthValue - 1,
                            date.dayOfMonth
                        ).show() 
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Calendar", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                }
                
                Spacer(Modifier.width(16.dp))
                
                val daysCount = ChronoUnit.DAYS.between(date, LocalDate.now())
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "seit $daysCount Tagen",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
