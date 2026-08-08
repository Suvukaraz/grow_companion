package com.example.fertilizerapp.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fertilizerapp.data.model.Fertilizer
import com.example.fertilizerapp.data.model.FertilizerType
import com.example.fertilizerapp.data.model.Recipe
import com.example.fertilizerapp.data.model.RecipeIngredient
import com.example.fertilizerapp.ui.components.NPKResultBadgeRow
import com.example.fertilizerapp.ui.components.SectionHeader
import com.example.fertilizerapp.viewmodel.AppViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeEditDialog(viewModel: AppViewModel, recipe: Recipe?, onDismiss: () -> Unit) {
    val fertilizers by viewModel.fertilizers.collectAsState()
    
    var recipeName by remember { mutableStateOf(recipe?.name ?: "") }
    var totalVolume by remember { mutableStateOf(recipe?.totalVolumeMl?.toString() ?: "") }
    var dilution by remember { mutableStateOf(recipe?.dilutionInstruction ?: "") }
    val tempIngredients = remember { mutableStateListOf<RecipeIngredient>().apply { 
        recipe?.ingredients?.let { addAll(it) }
    } }
    
    var selectedFert by remember { mutableStateOf<Fertilizer?>(null) }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

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
                    title = { Text(if (recipe == null) "Neues Rezept" else "Rezept bearbeiten", fontWeight = FontWeight.Bold) },
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
                        value = recipeName,
                        onValueChange = { recipeName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = totalVolume,
                        onValueChange = { totalVolume = it },
                        label = { Text("Gesamtvolumen (ml)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dilution,
                        onValueChange = { dilution = it },
                        label = { Text("Anwendung (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider()
                    
                    SectionHeader(title = "Zutaten", icon = Icons.Default.Add)
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedFert?.name ?: "Dünger wählen",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            fertilizers.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f.name) },
                                    onClick = { 
                                        selectedFert = f
                                        expanded = false 
                                    }
                                )
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Menge") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Button(
                            onClick = {
                                val a = amount.toDoubleOrNull() ?: 0.0
                                if (selectedFert != null && a > 0) {
                                    tempIngredients.add(RecipeIngredient(selectedFert!!, a))
                                    amount = ""
                                    selectedFert = null
                                }
                            },
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }

                    if (tempIngredients.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tempIngredients.forEach { ingredient ->
                                InputChip(
                                    selected = true,
                                    onClick = { tempIngredients.remove(ingredient) },
                                    label = { Text("${ingredient.amount}${if (ingredient.fertilizer.type == FertilizerType.LIQUID) "ml" else "g"} - ${ingredient.fertilizer.name}") },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Entfernen",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }

                        // Preview NPK
                        val previewRecipe = Recipe(
                            id = "preview",
                            name = "Preview",
                            totalVolumeMl = totalVolume.toIntOrNull() ?: 1000,
                            dilutionInstruction = "",
                            ingredients = tempIngredients.toList()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("NPK Vorschau", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                NPKResultBadgeRow(npkResult = previewRecipe.npkResult)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val v = totalVolume.toIntOrNull() ?: 0
                                if (recipeName.isNotBlank() && v > 0) {
                                    val r = Recipe(
                                        id = recipe?.id ?: UUID.randomUUID().toString(),
                                        name = recipeName,
                                        totalVolumeMl = v,
                                        dilutionInstruction = dilution,
                                        ingredients = tempIngredients.toList()
                                    )
                                    if (recipe == null) viewModel.addRecipe(r) else viewModel.updateRecipe(r)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Speichern")
                        }

                        if (recipe != null) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.removeRecipe(recipe)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
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
