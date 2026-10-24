package com.example.fertilizerapp.ui.plants

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fertilizerapp.data.PhaseColorPreferences
import com.example.fertilizerapp.data.model.PlantObject
import com.example.fertilizerapp.ui.components.EmptyState
import com.example.fertilizerapp.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun TimeCalculationTab(viewModel: AppViewModel) {
    var selectedObj by remember { mutableStateOf<PlantObject?>(null) }
    if (selectedObj == null) ObjectListScreen(viewModel) { selectedObj = it }
    else ObjectDetailScreen(viewModel, selectedObj!!) { selectedObj = null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectListScreen(viewModel: AppViewModel, onClick: (PlantObject) -> Unit) {
    val objects by viewModel.plantObjects.collectAsState()
    val phases by viewModel.objectPhases.collectAsState()
    val phaseColors by viewModel.phaseColors.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var toDel by remember { mutableStateOf<PlantObject?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { p ->
        Column(
            Modifier
                .padding(p)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                "Meine Objekte",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (objects.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Yard,
                    title = "Keine Objekte",
                    subtitle = "Tippe auf + um ein neues Objekt anzulegen."
                )
            } else {
                // Local copy that follows the finger while dragging; saved on drop
                var ordered by remember { mutableStateOf(objects) }
                LaunchedEffect(objects) { ordered = objects }

                val haptic = LocalHapticFeedback.current
                val listState = rememberLazyListState()
                val reorderState = rememberReorderableLazyListState(listState) { from, to ->
                    ordered = ordered.toMutableList().apply { add(to.index, removeAt(from.index)) }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp) // keep the last card clear of the FAB
                ) {
                    items(ordered, key = { it.id }) { obj ->
                        val ps = phases.filter { it.objectId == obj.id }
                        val last = ps.mapNotNull {
                            try { LocalDate.parse(it.dateIso) to it }
                            catch(e: Exception) { null }
                        }.maxByOrNull { it.first }

                        ReorderableItem(reorderState, key = obj.id) { isDragging ->
                            val elevation by animateDpAsState(if (isDragging) 8.dp else 1.dp, label = "dragElevation")
                            val dragModifier = Modifier.longPressDraggableHandle(
                                onDragStarted = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                                onDragStopped = { viewModel.reorderPlantObjects(ordered) }
                            )
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(dragModifier),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
                                onClick = { onClick(obj) }
                            ) {
                                Row(
                                    Modifier.padding(start = 4.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Drag handle: drag immediately; long press anywhere on the card works too
                                    Icon(
                                        Icons.Default.DragIndicator,
                                        contentDescription = "Verschieben",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .draggableHandle(
                                                onDragStarted = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                                                onDragStopped = { viewModel.reorderPlantObjects(ordered) }
                                            )
                                            .padding(8.dp)
                                            .size(24.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = obj.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        if (last != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            val phaseName = last.second.name
                                            val daysCount = ChronoUnit.DAYS.between(last.first, LocalDate.now())
                                            val phaseColor = PhaseColorPreferences.getColorForPhase(phaseName, phaseColors)

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = phaseColor,
                                                    shape = MaterialTheme.shapes.small
                                                ) {
                                                    Text(
                                                        text = phaseName,
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "$daysCount Tage",
                                                    color = phaseColor,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                    IconButton(onClick = { toDel = obj }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Löschen",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showAdd) {
        var n by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Neu") },
            text = {
                OutlinedTextField(
                    value = n,
                    onValueChange = { n = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (n.isNotBlank()) {
                            viewModel.addPlantObject(PlantObject(name = n))
                            showAdd = false
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false }) { Text("Abbruch") }
            }
        )
    }
    
    if (toDel != null) {
        AlertDialog(
            onDismissRequest = { toDel = null },
            title = { Text("Löschen") },
            text = { Text("Wirklich '${toDel?.name}' löschen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        toDel?.let { viewModel.removePlantObject(it) }
                        toDel = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { toDel = null }) { Text("Abbrechen") }
            }
        )
    }
}
