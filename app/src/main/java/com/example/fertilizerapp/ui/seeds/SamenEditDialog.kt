package com.example.fertilizerapp.ui.seeds

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fertilizerapp.data.model.AutoFem
import com.example.fertilizerapp.data.model.Samen
import com.example.fertilizerapp.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SamenEditDialog(viewModel: AppViewModel, seed: Samen?, onDismiss: () -> Unit) {
    var anzahl by remember { mutableStateOf(seed?.anzahl?.toString() ?: "") }
    var autoFem by remember { mutableStateOf(seed?.autoFem ?: AutoFem.AUTO) }
    var strain by remember { mutableStateOf(seed?.strain ?: "") }
    var breeder by remember { mutableStateOf(seed?.breeder ?: "") }
    var zeit by remember { mutableStateOf(seed?.zeitTage?.toString() ?: "") }
    var thc by remember { mutableStateOf(seed?.thcWert?.toString() ?: "") }

    val seeds by viewModel.seeds.collectAsState()
    val allBreeders = remember(seeds) { seeds.map { it.breeder }.distinct().filter { it.isNotBlank() }.sorted() }
    var breederExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full screen dialog
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Title Bar
                CenterAlignedTopAppBar(
                    title = { Text(if (seed == null) "Neuer Samen" else "Samen bearbeiten", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Schließen")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = anzahl,
                        onValueChange = { anzahl = it },
                        label = { Text("Anzahl") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Auto/Fem Selection via Segmented Buttons
                    Text("Samen-Typ", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = autoFem == AutoFem.AUTO,
                            onClick = { autoFem = AutoFem.AUTO },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("A (Auto)")
                        }
                        SegmentedButton(
                            selected = autoFem == AutoFem.FEM,
                            onClick = { autoFem = AutoFem.FEM },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("F (Fem)")
                        }
                    }

                    OutlinedTextField(
                        value = strain,
                        onValueChange = { strain = it },
                        label = { Text("Strain") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Breeder Dropdown
                    ExposedDropdownMenuBox(
                        expanded = breederExpanded,
                        onExpandedChange = { breederExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = breeder,
                            onValueChange = { breeder = it },
                            label = { Text("Breeder") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                            trailingIcon = {
                                if (allBreeders.isNotEmpty()) {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = breederExpanded)
                                }
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        if (allBreeders.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = breederExpanded,
                                onDismissRequest = { breederExpanded = false }
                            ) {
                                allBreeders.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text(b) },
                                        onClick = {
                                            breeder = b
                                            breederExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = zeit,
                            onValueChange = { zeit = it },
                            label = { Text("Zeit (Tage)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = thc,
                            onValueChange = { thc = it },
                            label = { Text("THC %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val s = Samen(
                                    id = seed?.id ?: 0,
                                    anzahl = anzahl.toIntOrNull() ?: 0,
                                    autoFem = autoFem,
                                    strain = strain,
                                    breeder = breeder,
                                    zeitTage = zeit.toIntOrNull() ?: 0,
                                    thcWert = thc.toIntOrNull() ?: 0
                                )
                                if (seed == null) viewModel.addSamen(s) else viewModel.updateSamen(s)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Speichern")
                        }

                        if (seed != null) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.removeSamen(seed)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Löschen")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
