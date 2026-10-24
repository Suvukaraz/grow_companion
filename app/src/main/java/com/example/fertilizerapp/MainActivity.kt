package com.example.fertilizerapp

import com.example.fertilizerapp.BuildConfig
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fertilizerapp.ui.components.AppTabRow
import com.example.fertilizerapp.ui.components.TabItem
import com.example.fertilizerapp.ui.fertilizer.FertilizerTab
import com.example.fertilizerapp.ui.plants.TimeCalculationTab
import com.example.fertilizerapp.ui.recipe.RecipeTab
import com.example.fertilizerapp.ui.seeds.SamenTab
import com.example.fertilizerapp.ui.settings.SettingsDialog
import com.example.fertilizerapp.ui.splash.TimerSplashScreen
import com.example.fertilizerapp.ui.theme.FertilizerAppTheme
import com.example.fertilizerapp.viewmodel.AppViewModel
import kotlinx.coroutines.launch

// --- MAIN ACTIVITY ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            // auto: dark icons in light mode, light icons in dark mode
            statusBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
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

private val fullTabs = listOf(
    TabItem("Dünger", Icons.Default.Eco),
    TabItem("Nährlösungen", Icons.Default.Science),
    TabItem("Zeiten", Icons.Default.Schedule),
    TabItem("SamenDB", Icons.Default.Spa)
)

private val timerTabs = listOf(
    TabItem("Zeiten", Icons.Default.Schedule),
    TabItem("SamenDB", Icons.Default.Spa)
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
    var showSettings by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Grow Companion",
                onInfo = { showInfo = true },
                onSettings = { showSettings = true }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AppTabRow(
                tabs = fullTabs,
                selectedIndex = pagerState.currentPage,
                scrollPosition = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
                onSelect = { scope.launch { pagerState.animateScrollToPage(it) } }
            )

            HorizontalPager(
                state = pagerState,
                // Keep all tabs composed so swiping never has to build a page mid-animation
                beyondViewportPageCount = pagerState.pageCount - 1,
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
    if (showSettings) {
        SettingsDialog(viewModel) { showSettings = false }
    }
    if (showInfo) {
        InfoDialog(appName = "Grow Companion", onDismiss = { showInfo = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(title: String, onInfo: () -> Unit, onSettings: () -> Unit) {
    TopAppBar(
        title = {
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onInfo) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            IconButton(onClick = onSettings) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Einstellungen",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimerFlavorScreen(viewModel: AppViewModel) {
    var showSplash by remember { mutableStateOf(true) }
    if (showSplash) {
        TimerSplashScreen { showSplash = false }
    } else {
        val pagerState = rememberPagerState(pageCount = { timerTabs.size })
        val scope = rememberCoroutineScope()
        var showSettings by remember { mutableStateOf(false) }
        var showInfo by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                AppTopBar(
                    title = "Grow Helper",
                    onInfo = { showInfo = true },
                    onSettings = { showSettings = true }
                )
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                AppTabRow(
                    tabs = timerTabs,
                    selectedIndex = pagerState.currentPage,
                    scrollPosition = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
                    onSelect = { scope.launch { pagerState.animateScrollToPage(it) } }
                )
                HorizontalPager(
                    state = pagerState,
                    // Keep all tabs composed so swiping never has to build a page mid-animation
                    beyondViewportPageCount = pagerState.pageCount - 1,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        0 -> TimeCalculationTab(viewModel)
                        1 -> SamenTab(viewModel)
                    }
                }
            }
        }

        if (showSettings) {
            SettingsDialog(viewModel) { showSettings = false }
        }
        if (showInfo) {
            InfoDialog(appName = "Grow Helper", onDismiss = { showInfo = false })
        }
    }
}

// --- INFO DIALOG ---

@Composable
private fun InfoDialog(appName: String, onDismiss: () -> Unit) {
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
                    appName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    // VERSION_NAME carries the flavor suffix ("1.5.0-full")
                    "Version ${BuildConfig.VERSION_NAME.substringBefore('-')}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
