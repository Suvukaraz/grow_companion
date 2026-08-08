package com.example.fertilizerapp.ui.fertilizer

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fertilizerapp.data.model.Fertilizer
import com.example.fertilizerapp.data.model.FertilizerType
import com.example.fertilizerapp.ui.components.EmptyState
import com.example.fertilizerapp.ui.components.NPKBadgeRow
import com.example.fertilizerapp.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FertilizerTab(viewModel: AppViewModel) {
    val fertilizers by viewModel.fertilizers.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editFert by remember { mutableStateOf<Fertilizer?>(null) }
    
    val clipboard = LocalClipboard.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dünger hinzufügen")
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (fertilizers.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.Eco,
                        title = "Keine Dünger vorhanden",
                        subtitle = "Tippe auf + um deinen ersten Dünger hinzuzufügen."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = fertilizers,
                        key = { it.id }
                    ) { f ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .combinedClickable(
                                    onClick = {
                                        val copyText = "${f.name} (🧪 N: ${f.n}% | P: ${f.p}% | K: ${f.k}%)"
                                        scope.launch {
                                            clipboard.setClipEntry(ClipData.newPlainText("Fertilizer Info", copyText).toClipEntry())
                                            snackbarHostState.showSnackbar(
                                                message = "Kopiert: $copyText",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    },
                                    onLongClick = { editFert = f }
                                ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isSolid = f.type == FertilizerType.SOLID
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSolid) Icons.Default.Eco else Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        modifier = Modifier.padding(8.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = f.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.tertiaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (isSolid) "Feststoff" else "Flüssig",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (f.manufacturer.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = f.manufacturer,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    NPKBadgeRow(n = f.n, p = f.p, k = f.k)
                                }

                                IconButton(
                                    onClick = { viewModel.removeFertilizer(f) },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Löschen")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd || editFert != null) {
        FertilizerEditDialog(viewModel, editFert) {
            showAdd = false
            editFert = null
        }
    }
}
