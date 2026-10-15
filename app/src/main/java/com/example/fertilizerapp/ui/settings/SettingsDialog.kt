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
import com.example.fertilizerapp.data.PhaseColorPreferences
import com.example.fertilizerapp.ui.components.SectionHeader
import com.example.fertilizerapp.viewmodel.AppViewModel
import kotlinx.coroutines.launch
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

    val objectPhases by viewModel.objectPhases.collectAsState()

    var activePickerPhase by remember { mutableStateOf<PhaseConfigItem?>(null) }
    var showConfirmRestore by remember { mutableStateOf(false) }
    var restoreData by remember { mutableStateOf("") }

    // Custom phases from user data that are not standard keywords
    val customPhases = remember(objectPhases) {
        objectPhases
            .map { it.name.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .filter { name ->
                val norm = name.lowercase()
                !norm.contains("sämling") && !norm.contains("keimling") &&
                !norm.contains("veg") && !norm.contains("wachstum") &&
                !norm.contains("blüte") && !norm.contains("blute") &&
                !norm.contains("trocknung") && !norm.contains("ernte")
            }
            .map { PhaseConfigItem(it.lowercase(), it) }
    }

    val createLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            scope.launch {
                val json = viewModel.createBackupJson()
                context.contentResolver.openOutputStream(it)?.use { os -> os.write(json.toByteArray()) }
                Toast.makeText(context, "Backup exportiert", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val json = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { r -> r.readText() } ?: ""
            restoreData = json
            showConfirmRestore = true
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
                            text = "Passe die Farben der Entwicklungsphasen aus dem Zeiten-Tab an.",
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

                                if (customPhases.isNotEmpty()) {
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
                                            onClick = { activePickerPhase = phase }
                                        )
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
                            text = "Sichere alle deine Daten (Dünger, Nährlösungen, Samen, Zeiten, Phasenfarben) oder stelle sie wieder her.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FilledTonalButton(
                            onClick = { createLauncher.launch("Hanf_Backup_${LocalDate.now()}.json") },
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
                    "Alle bestehenden Daten werden überschrieben! Fortfahren?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = viewModel.restoreFromBackup(restoreData)
                            if (res.isSuccess) {
                                Toast.makeText(context, "Erfolgreich importiert", Toast.LENGTH_LONG).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, "Fehler: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
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
    onClick: () -> Unit
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

        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Farbe ändern",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
    }
}
