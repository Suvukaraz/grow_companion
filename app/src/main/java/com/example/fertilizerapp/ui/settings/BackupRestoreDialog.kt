package com.example.fertilizerapp.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fertilizerapp.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun BackupRestoreDialog(viewModel: AppViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showConfirmRestore by remember { mutableStateOf(false) }
    var restoreData by remember { mutableStateOf("") }

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

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CloudUpload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                "Backup & Restore",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                "Sichere alle deine Daten (Dünger, Rezepte, Samen, Zeiten) oder stelle sie wieder her.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                TextButton(onClick = onDismiss) {
                    Text("Schließen")
                }
            }
        },
        dismissButton = {}
    )

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
                                Toast.makeText(context, "Erfolgreich importiert: ${res.getOrNull()?.first} Samen, ${res.getOrNull()?.second} Rezepte", Toast.LENGTH_LONG).show()
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
