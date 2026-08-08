package com.example.fertilizerapp

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fertilizerapp.ui.fertilizer.FertilizerTab
import com.example.fertilizerapp.ui.plants.TimeCalculationTab
import com.example.fertilizerapp.ui.recipe.RecipeTab
import com.example.fertilizerapp.ui.seeds.SamenTab
import com.example.fertilizerapp.ui.settings.BackupRestoreDialog
import com.example.fertilizerapp.ui.splash.TimerSplashScreen
import com.example.fertilizerapp.ui.theme.FertilizerAppTheme
import com.example.fertilizerapp.viewmodel.AppViewModel
import kotlinx.coroutines.launch

// --- MAIN ACTIVITY ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        setContent {
            FertilizerAppTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

// --- TAB DEFINITION ---

private data class TabItem(
    val title: String,
    val icon: ImageVector
)

private val fullTabs = listOf(
    TabItem("Dünger", Icons.Default.Eco),
    TabItem("Rezepte", Icons.AutoMirrored.Filled.MenuBook),
    TabItem("Zeiten", Icons.Default.Schedule),
    TabItem("Samen", Icons.Default.Grass)
)

private val timerTabs = listOf(
    TabItem("Zeiten", Icons.Default.Schedule),
    TabItem("Samen", Icons.Default.Grass)
)

// --- MAIN SCREEN ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: AppViewModel = viewModel()) {
    val isTimerFlavor = remember {
        BuildConfig.FLAVOR == "timer"
    }

    if (isTimerFlavor) {
        TimerFlavorScreen(viewModel)
    } else {
        FullFlavorScreen(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullFlavorScreen(viewModel: AppViewModel) {
    val pagerState = rememberPagerState(pageCount = { fullTabs.size })
    val scope = rememberCoroutineScope()
    var showBackupRestore by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Grow Companion",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showInfo = true }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showBackupRestore = true }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Backup & Restore",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Styled Tab Row - Changed to PrimaryTabRow for full width
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            ) {
                fullTabs.forEachIndexed { index, tab ->
                    val selected = pagerState.currentPage == index
                    val animatedColor by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        animationSpec = tween(300),
                        label = "tabColor"
                    )
                    Tab(
                        selected = selected,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                tab.title,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = animatedColor
                            )
                        },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = tab.title,
                                tint = animatedColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> FertilizerTab(viewModel)
                    1 -> RecipeTab(viewModel)
                    2 -> TimeCalculationTab(viewModel)
                    3 -> SamenTab(viewModel)
                }
            }
        }
    }

    // Dialogs
    if (showBackupRestore) {
        BackupRestoreDialog(viewModel) { showBackupRestore = false }
    }
    if (showInfo) {
        InfoDialog(onDismiss = { showInfo = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimerFlavorScreen(viewModel: AppViewModel) {
    var showSplash by remember { mutableStateOf(true) }
    if (showSplash) {
        TimerSplashScreen { showSplash = false }
    } else {
        val pagerState = rememberPagerState(pageCount = { timerTabs.size })
        val scope = rememberCoroutineScope()
        Scaffold { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    timerTabs.forEachIndexed { index, tab ->
                        val selected = pagerState.currentPage == index
                        Tab(
                            selected = selected,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(tab.title) },
                            icon = {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        0 -> TimeCalculationTab(viewModel)
                        1 -> SamenTab(viewModel)
                    }
                }
            }
        }
    }
}

// --- INFO DIALOG ---

@Composable
private fun InfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "App Info",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.gc_logo),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(128.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Grow Companion",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "entwickelt von Sven Kersten",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )
}
