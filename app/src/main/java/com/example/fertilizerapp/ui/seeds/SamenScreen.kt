package com.example.fertilizerapp.ui.seeds

import android.content.ClipData
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fertilizerapp.data.model.AutoFem
import com.example.fertilizerapp.data.model.Samen
import com.example.fertilizerapp.ui.components.EmptyState
import com.example.fertilizerapp.viewmodel.AppViewModel
import kotlinx.coroutines.launch

// If Grass is not available in basic icons, we fallback to a similar eco icon or assume it exists in extended.
// Since the prompt asks for Icons.Default.Grass specifically, we import it.
import androidx.compose.material.icons.filled.Grass

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SamenTab(viewModel: AppViewModel) {
    val seeds by viewModel.seeds.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editSeed by remember { mutableStateOf<Samen?>(null) }
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
                Icon(Icons.Default.Add, contentDescription = "Samen hinzufügen")
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { p ->
        Column(
            Modifier
                .padding(p)
                .padding(8.dp)
        ) {
            if (seeds.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Grass,
                    title = "Keine Samen",
                    subtitle = "Tippe auf + um einen neuen Samen hinzuzufügen."
                )
            } else {
                val totalSeeds = seeds.sumOf { it.anzahl }
                val totalFem = seeds.filter { it.autoFem == AutoFem.FEM }.sumOf { it.anzahl }
                val totalAuto = seeds.filter { it.autoFem == AutoFem.AUTO }.sumOf { it.anzahl }

                // Statistics Row
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fem: $totalFem",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Gesamt: $totalSeeds",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Auto: $totalAuto",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeaderCell("⚖️", "anzahl", viewModel, 0.08f)
                    HeaderCell("Typ", "autoFem", viewModel, 0.10f)
                    HeaderCell("Strain", "strain", viewModel, 0.32f)
                    Spacer(Modifier.width(8.dp))
                    HeaderCell("Breeder", "breeder", viewModel, 0.25f)
                    HeaderCell("Zeit", "zeit", viewModel, 0.15f)
                    HeaderCell("THC", "thc", viewModel, 0.10f)
                }
                LazyColumn {
                    itemsIndexed(seeds, key = { _, s -> s.id }) { index, s ->
                        val bgColor = if (index % 2 == 0) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        }

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .background(bgColor)
                                .combinedClickable(
                                    onClick = {
                                        val copyText = "🧬 ${s.strain}\n🌱 ${s.breeder}\n⏳ ${s.zeitTage} Tage | 🔥 ${s.thcWert}% THC"
                                        scope.launch {
                                            clipboard.setClipEntry(ClipData.newPlainText("Seed Info", copyText).toClipEntry())
                                            snackbarHostState.showSnackbar(
                                                message = "Kopiert: $copyText",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    },
                                    onLongClick = { editSeed = s }
                                )
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${s.anzahl}",
                                modifier = Modifier.weight(0.08f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            // Typ Chip
                            Box(
                                modifier = Modifier
                                    .weight(0.10f)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            ) {
                                val isAuto = s.autoFem == AutoFem.AUTO
                                // Grünstich für Auto, Lilastich für Fem
                                val chipBg = if (isAuto) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                val chipText = if (isAuto) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(chipBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isAuto) "A" else "F",
                                        color = chipText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = s.strain,
                                modifier = Modifier.weight(0.32f),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = s.breeder,
                                modifier = Modifier.weight(0.25f),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            // Zeit
                            Row(
                                modifier = Modifier.weight(0.15f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${s.zeitTage}T",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // THC Chip
                            Box(
                                modifier = Modifier
                                    .weight(0.10f)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            ) {
                                val thcLevel = s.thcWert
                                val (thcBg, thcColor) = when {
                                    thcLevel >= 30 -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                                    thcLevel >= 21 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(thcBg.copy(alpha = 0.6f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${s.thcWert}%",
                                        color = thcColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }

    if (showAdd || editSeed != null) {
        SamenEditDialog(viewModel, editSeed) {
            showAdd = false
            editSeed = null
        }
    }
}

@Composable
fun RowScope.HeaderCell(label: String, col: String, viewModel: AppViewModel, weight: Float) {
    val current = viewModel.seedSortColumn.value
    val asc = viewModel.seedSortAscending.value
    
    Row(
        Modifier
            .weight(weight)
            .clickable {
                if (current == col) {
                    viewModel.seedSortAscending.value = !asc
                } else {
                    viewModel.seedSortColumn.value = col
                    viewModel.seedSortAscending.value = true
                }
            }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        if (current == col) {
            Icon(
                imageVector = if (asc) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
