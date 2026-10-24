package com.example.fertilizerapp.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fertilizerapp.BuildConfig
import com.example.fertilizerapp.data.PhaseColorPreferences
import com.example.fertilizerapp.ui.components.SectionHeader
import com.example.fertilizerapp.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

private data class PhaseConfigItem(
    val key: String,
    val title: String
)

private val STANDARD_PHASES = listOf(
    PhaseConfigItem(PhaseColorPreferences.KEY_SAMLING, "Sämling"),
    PhaseConfigItem(PhaseColorPreferences.KEY_WACHSTUM, "Wachstum"),
    PhaseConfigItem(PhaseColorPreferences.KEY_BLUETE, "Blüte"),
    PhaseConfigItem(PhaseColorPreferences.KEY_TROCKNUNG, "Trocknung")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(viewModel: AppViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isTimer = remember { BuildConfig.FLAVOR == "timer" }

    val customPhaseNames by viewModel.customPhases.collectAsState()

    var activePickerPhase by remember { mutableStateOf<PhaseConfigItem?>(null) }
    var showConfirmRestore by remember { mutableStateOf(false) }
    var restoreData by remember { mutableStateOf("") }
    var newPhaseName by remember { mutableStateOf("") }
    var newPhaseError by remember { mutableStateOf(false) }
    var phaseToDelete by remember { mutableStateOf<String?>(null) }

    val customPhases = remember(customPhaseNames) {
        customPhaseNames.map { PhaseConfigItem(it.lowercase(), it) }
    }

    val createLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            scope.launch {
                val result = runCatching {
                    val json = viewModel.createBackupJson()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(it)?.use { os -> os.write(json.toByteArray()) }
                            ?: error("Datei konnte nicht geöffnet werden")
                    }
                }
                val msg = result.fold({ "Backup exportiert" }, { e -> "Export fehlgeschlagen: ${e.message}" })
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(it)?.bufferedReader()?.use { r -> r.readText() }
                            ?: error("Datei konnte nicht geöffnet werden")
                    }
                }.onSuccess { json ->
                    restoreData = json
                    showConfirmRestore = true
                }.onFailure { e ->
                    Toast.makeText(context, "Import fehlgeschlagen: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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
                    title = {
                        Text(
                            "Einstellungen",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
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
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // SECTION 1: PHASE COLORS
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(
                            title = "Phasen-Farben",
                            icon = Icons.Default.Palette
                        )

                        Text(
                            text = "Diese Phasen stehen im Zeiten-Tab zur Auswahl. Tippe auf eine Phase, um ihre Farbe anzupassen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Standardphasen",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                STANDARD_PHASES.forEach { phase ->
                                    val currentColor = viewModel.getPhaseColor(phase.title)
                                    PhaseColorRow(
                                        title = phase.title,
                                        color = currentColor,
                                        onClick = { activePickerPhase = phase }
                                    )
                                }

                                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                                Text(
                                    text = "Eigene Phasen",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                customPhases.forEach { phase ->
                                    val currentColor = viewModel.getPhaseColor(phase.title)
                                    PhaseColorRow(
                                        title = phase.title,
                                        color = currentColor,
                                        onClick = { activePickerPhase = phase },
                                        onDelete = { phaseToDelete = phase.title }
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = newPhaseName,
                                        onValueChange = { newPhaseName = it; newPhaseError = false },
                                        label = { Text("Neue Phase") },
                                        singleLine = true,
                                        isError = newPhaseError,
                                        supportingText = if (newPhaseError) {
                                            { Text("Leer oder schon vorhanden") }
                                        } else null,
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilledTonalIconButton(
                                        onClick = {
                                            if (viewModel.addCustomPhase(newPhaseName)) newPhaseName = ""
                                            else newPhaseError = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Phase hinzufügen")
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetPhaseColors() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Farben auf Standard zurücksetzen")
                        }
                    }

                    HorizontalDivider()

                    // SECTION 2: BACKUP & RESTORE
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(
                            title = "Backup & Restore",
                            icon = Icons.Default.CloudUpload
                        )

                        Text(
                            text = if (isTimer) "Sichere deine Daten (Samen, Zeiten, Phasen-Farben, eigene Phasen) oder stelle sie wieder her."
                                   else "Sichere alle deine Daten (Dünger, Nährlösungen, Samen, Zeiten, Phasen-Farben, eigene Phasen) oder stelle sie wieder her.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FilledTonalButton(
                            onClick = { createLauncher.launch("${if (isTimer) "Grow_Helper" else "Grow_Companion"}_Backup_${LocalDate.now()}.json") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudUpload, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Backup exportieren")
                        }

                        OutlinedButton(
                            onClick = { openLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudDownload, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Backup importieren")
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    // Color Picker Dialog
    if (activePickerPhase != null) {
        val item = activePickerPhase!!
        val currentColor = viewModel.getPhaseColor(item.title)
        PhaseColorPickerDialog(
            phaseTitle = item.title,
            initialColor = currentColor,
            onColorSelected = { selected ->
                viewModel.updatePhaseColor(item.key, selected.toArgb())
                activePickerPhase = null
            },
            onResetDefault = {
                val defaultColor = PhaseColorPreferences.DEFAULT_COLORS[item.key] ?: PhaseColorPreferences.DEFAULT_FALLBACK_COLOR
                viewModel.updatePhaseColor(item.key, defaultColor)
                activePickerPhase = null
            },
            onDismiss = { activePickerPhase = null }
        )
    }

    phaseToDelete?.let { name ->
        AlertDialog(
            onDismissRequest = { phaseToDelete = null },
            title = { Text("Phase entfernen") },
            text = { Text("'$name' wird nicht mehr zur Auswahl angeboten. Bereits eingetragene Phasen behalten ihren Namen.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.removeCustomPhase(name); phaseToDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Entfernen") }
            },
            dismissButton = {
                TextButton(onClick = { phaseToDelete = null }) { Text("Abbrechen") }
            }
        )
    }

    // Confirm Restore Dialog
    if (showConfirmRestore) {
        AlertDialog(
            onDismissRequest = { showConfirmRestore = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    "Restore bestätigen",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    if (isTimer) "Alle bestehenden Daten werden überschrieben! Fortfahren?"
                    else "Alle bestehenden Daten werden überschrieben! Bei einem Backup aus Grow Helper bleiben Dünger und Nährlösungen erhalten. Fortfahren?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = viewModel.restoreFromBackup(restoreData)
                            res.getOrNull()?.let { r ->
                                val msg = buildString {
                                    append("Erfolgreich importiert: ${r.seeds} Samen")
                                    // Grow Helper shows no nutrient solutions; a partial backup brings none
                                    if (!isTimer && !r.partial) append(", ${r.recipes} Nährlösungen")
                                    if (!isTimer && r.partial) append(". Dünger und Nährlösungen wurden beibehalten.")
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                onDismiss()
                            } ?: Toast.makeText(context, "Fehler: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            showConfirmRestore = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Ja, überschreiben")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRestore = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
private fun PhaseColorRow(
    title: String,
    color: Color,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Farbe ändern",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            if (onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Phase entfernen",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
