package com.example.fertilizerapp.ui.fertilizer

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
import com.example.fertilizerapp.data.model.Fertilizer
import com.example.fertilizerapp.data.model.FertilizerType
import com.example.fertilizerapp.util.formatCompact
import com.example.fertilizerapp.util.toDecimalOrNull
import com.example.fertilizerapp.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerEditDialog(viewModel: AppViewModel, fertilizer: Fertilizer?, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(fertilizer?.name ?: "") }
    var manufacturer by remember { mutableStateOf(fertilizer?.manufacturer ?: "") }
    var type by remember { mutableStateOf(fertilizer?.type ?: FertilizerType.SOLID) }
    var density by remember { mutableStateOf(fertilizer?.density?.takeIf { it != 1.0 }?.formatCompact(4) ?: "") }
    var n by remember { mutableStateOf(fertilizer?.n?.takeIf { it != 0.0 }?.formatCompact(4) ?: "") }
    var p by remember { mutableStateOf(fertilizer?.p?.takeIf { it != 0.0 }?.formatCompact(4) ?: "") }
    var k by remember { mutableStateOf(fertilizer?.k?.takeIf { it != 0.0 }?.formatCompact(4) ?: "") }
    var showErrors by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = { Text(if (fertilizer == null) "Neuer Dünger" else "Dünger bearbeiten", fontWeight = FontWeight.Bold) },
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        isError = showErrors && name.isBlank(),
                        supportingText = if (showErrors && name.isBlank()) {
                            { Text("Bitte einen Namen eingeben") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = manufacturer,
                        onValueChange = { manufacturer = it },
                        label = { Text("Hersteller") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Dünger-Typ", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = type == FertilizerType.SOLID,
                            onClick = { type = FertilizerType.SOLID },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("Feststoff (g)")
                        }
                        SegmentedButton(
                            selected = type == FertilizerType.LIQUID,
                            onClick = { type = FertilizerType.LIQUID },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("Flüssigkeit (ml)")
                        }
                    }

                    if (type == FertilizerType.LIQUID) {
                        OutlinedTextField(
                            value = density,
                            onValueChange = { density = it },
                            label = { Text("Dichte (g/ml) - z.B. 1.25") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            placeholder = { Text("1.0") },
                            supportingText = { Text("Wird für w/w Angaben benötigt") }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = n,
                            onValueChange = { n = it },
                            label = { Text("N %") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = p,
                            onValueChange = { p = it },
                            label = { Text("P %") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = k,
                            onValueChange = { k = it },
                            label = { Text("K %") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showErrors = true
                                if (name.isNotBlank()) {
                                    val f = Fertilizer(
                                        id = fertilizer?.id ?: java.util.UUID.randomUUID().toString(),
                                        name = name,
                                        manufacturer = manufacturer,
                                        type = type,
                                        density = density.toDecimalOrNull()?.takeIf { it > 0 } ?: 1.0,
                                        n = n.toDecimalOrNull() ?: 0.0,
                                        p = p.toDecimalOrNull() ?: 0.0,
                                        k = k.toDecimalOrNull() ?: 0.0
                                    )
                                    if (fertilizer == null) viewModel.addFertilizer(f) else viewModel.updateFertilizer(f)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Speichern")
                        }

                        if (fertilizer != null) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.removeFertilizer(fertilizer)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
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
