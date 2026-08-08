package com.example.fertilizerapp.ui.plants

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fertilizerapp.data.model.ObjectPhase
import com.example.fertilizerapp.data.model.PlantObject
import com.example.fertilizerapp.ui.components.SectionHeader
import com.example.fertilizerapp.viewmodel.AppViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectDetailScreen(viewModel: AppViewModel, obj: PlantObject, onBack: () -> Unit) {
    val allPhases by viewModel.objectPhases.collectAsState()
    var name by remember { mutableStateOf(obj.name) }
    val initial = remember(obj.id) { allPhases.filter { it.objectId == obj.id } }
    val phases = remember { mutableStateListOf<ObjectPhase>().apply { addAll(initial) } }
    val changed = name != obj.name || phases.toList() != initial
    var discard by remember { mutableStateOf(false) }
    
    BackHandler { if (changed) discard = true else onBack() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name) },
                navigationIcon = {
                    IconButton(onClick = { if (changed) discard = true else onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.updatePlantObject(obj.copy(name = name))
                            viewModel.updatePhasesForObject(obj.id, phases.toList())
                            onBack()
                        },
                        enabled = changed
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { p ->
        Column(
            Modifier
                .padding(p)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionHeader(
                title = "Phasen",
                icon = Icons.Default.Timeline,
                trailing = {
                    FilledTonalButton(
                        onClick = {
                            phases.add(
                                ObjectPhase(
                                    objectId = obj.id,
                                    name = "",
                                    dateIso = LocalDate.now().toString()
                                )
                            )
                        }
                    ) {
                        Text("+ Phase")
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    items = phases.sortedWith(compareByDescending { 
                        try { LocalDate.parse(it.dateIso) } catch(e: Exception) { LocalDate.MIN } 
                    }),
                    key = { it.id }
                ) { ph ->
                    PhaseRow(
                        ph = ph,
                        allNames = allPhases.map { it.name }.distinct().filter { it.isNotBlank() },
                        onUp = { up -> 
                            val i = phases.indexOfFirst { it.id == ph.id }
                            if (i != -1) phases[i] = up 
                        },
                        onDel = { phases.removeIf { it.id == ph.id } }
                    )
                }
            }
        }
    }
    
    if (discard) {
        AlertDialog(
            onDismissRequest = { discard = false },
            title = { Text("Verwerfen?") },
            text = { Text("Ungespeicherte Änderungen verwerfen?") },
            confirmButton = {
                TextButton(onClick = { discard = false; onBack() }) { Text("Ja") }
            },
            dismissButton = {
                TextButton(onClick = { discard = false }) { Text("Nein") }
            }
        )
    }
}
