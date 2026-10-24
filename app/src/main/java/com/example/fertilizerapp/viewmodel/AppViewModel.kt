package com.example.fertilizerapp.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.fertilizerapp.data.PhaseColorPreferences
import com.example.fertilizerapp.data.db.AppBackup
import com.example.fertilizerapp.data.db.AppDatabase
import com.example.fertilizerapp.data.model.Fertilizer
import com.example.fertilizerapp.data.model.ObjectPhase
import com.example.fertilizerapp.data.model.PlantObject
import com.example.fertilizerapp.data.model.Recipe
import com.example.fertilizerapp.data.model.Samen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    val fertilizers = db.fertilizerDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recipes = db.recipeDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val plantObjects = db.plantObjectDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val objectPhases = db.objectPhaseDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Phase Colors State
    private val phaseColorPrefs = PhaseColorPreferences(application)
    private val _phaseColors = MutableStateFlow(phaseColorPrefs.getAllColors())
    val phaseColors: StateFlow<Map<String, Int>> = _phaseColors.asStateFlow()

    fun getPhaseColor(phaseName: String): Color {
        return PhaseColorPreferences.getColorForPhase(phaseName, _phaseColors.value)
    }

    fun updatePhaseColor(phaseKey: String, colorInt: Int) {
        phaseColorPrefs.setColor(phaseKey, colorInt)
        _phaseColors.value = phaseColorPrefs.getAllColors()
    }

    fun resetPhaseColors() {
        phaseColorPrefs.resetToDefaults()
        _phaseColors.value = phaseColorPrefs.getAllColors()
    }

    // Custom phases (managed in settings, selectable in the Zeiten tab)
    private val _customPhases = MutableStateFlow(phaseColorPrefs.getCustomPhases() ?: emptyList())
    val customPhases: StateFlow<List<String>> = _customPhases.asStateFlow()

    /** All phases selectable in the Zeiten tab: standard phases first, then custom ones. */
    val phaseOptions: StateFlow<List<String>> = _customPhases
        .map { PhaseColorPreferences.STANDARD_PHASES + it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PhaseColorPreferences.STANDARD_PHASES + _customPhases.value)

    /** Returns false if the name is empty or already exists. */
    fun addCustomPhase(name: String): Boolean {
        val trimmed = name.trim()
        val exists = (PhaseColorPreferences.STANDARD_PHASES + _customPhases.value).any { it.equals(trimmed, ignoreCase = true) }
        if (trimmed.isEmpty() || exists) return false
        setCustomPhases(_customPhases.value + trimmed)
        return true
    }

    /** Existing entries in the Zeiten tab keep their name; it just isn't offered anymore. */
    fun removeCustomPhase(name: String) {
        setCustomPhases(_customPhases.value - name)
        phaseColorPrefs.removeColor(name)
        _phaseColors.value = phaseColorPrefs.getAllColors()
    }

    private fun setCustomPhases(names: List<String>) {
        phaseColorPrefs.setCustomPhases(names)
        _customPhases.value = names
    }

    private fun customPhasesFrom(phases: List<ObjectPhase>): List<String> = phases
        .map { it.name.trim() }
        .filter { it.isNotBlank() && !PhaseColorPreferences.isStandardPhase(it) }
        .distinctBy { it.lowercase() }

    // Samen DB State
    private val _rawSeeds = db.samenDao().getAll()
    var seedSortColumn = mutableStateOf("strain")
    var seedSortAscending = mutableStateOf(true)

    val seeds: StateFlow<List<Samen>> = combine(
        _rawSeeds,
        snapshotFlow { seedSortColumn.value },
        snapshotFlow { seedSortAscending.value }
    ) { list, col, asc ->
        val sorted = when (col) {
            "anzahl" -> list.sortedWith(compareBy<Samen> { it.anzahl }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.strain })
            "autoFem" -> list.sortedWith(compareBy<Samen> { it.autoFem }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.strain })
            "breeder" -> list.sortedWith(compareBy<Samen, String>(String.CASE_INSENSITIVE_ORDER) { it.breeder }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.strain })
            "zeit" -> list.sortedWith(compareBy<Samen> { it.zeitTage }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.strain })
            "thc" -> list.sortedWith(compareBy<Samen> { it.thcWert }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.strain })
            else -> list.sortedWith(compareBy<Samen, String>(String.CASE_INSENSITIVE_ORDER) { it.strain })
        }
        if (asc) sorted else sorted.reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        ensureDefaultData()
        migrateCustomPhases()
    }

    // Older versions had free-text phase names; adopt the ones already in use once
    private fun migrateCustomPhases() {
        if (phaseColorPrefs.getCustomPhases() != null) return
        viewModelScope.launch {
            setCustomPhases(customPhasesFrom(db.objectPhaseDao().getAll().first()))
        }
    }

    private fun ensureDefaultData() {
        viewModelScope.launch {
            val currentFerts = db.fertilizerDao().getAll().first()
            if (currentFerts.isEmpty()) {
                db.fertilizerDao().insertAll(listOf(
                    Fertilizer(name = "Hakaphos Blau", manufacturer = "Compo Expert", n = 15.0, p = 10.0, k = 15.0),
                    Fertilizer(name = "Hakaphos Rot", manufacturer = "Compo Expert", n = 8.0, p = 12.0, k = 24.0),
                    Fertilizer(name = "Hakaphos Soft Ultra", manufacturer = "Compo Expert", n = 18.0, p = 8.0, k = 18.0),
                    Fertilizer(name = "Hakaphos Naranja", manufacturer = "Compo Expert", n = 15.0, p = 5.0, k = 30.0),
                    Fertilizer(name = "Poly-Feed Libretto", manufacturer = "Haifa", n = 15.0, p = 10.0, k = 15.0),
                    Fertilizer(name = "Poly-Feed Tardo", manufacturer = "Haifa", n = 8.0, p = 12.0, k = 24.0),
                    Fertilizer(name = "Bittersalz", manufacturer = "", n = 0.0, p = 0.0, k = 0.0)
                ))
            }
        }
    }

    fun addFertilizer(f: Fertilizer) = viewModelScope.launch { db.fertilizerDao().insert(f) }
    fun updateFertilizer(f: Fertilizer) = viewModelScope.launch { db.fertilizerDao().update(f) }
    fun removeFertilizer(f: Fertilizer) = viewModelScope.launch { db.fertilizerDao().delete(f) }
    fun addRecipe(r: Recipe) = viewModelScope.launch { db.recipeDao().insert(r) }
    fun updateRecipe(r: Recipe) = viewModelScope.launch { db.recipeDao().update(r) }
    fun removeRecipe(r: Recipe) = viewModelScope.launch { db.recipeDao().delete(r) }
    fun addPlantObject(o: PlantObject) = viewModelScope.launch { db.plantObjectDao().insert(o) }
    fun updatePlantObject(o: PlantObject) = viewModelScope.launch { db.plantObjectDao().update(o) }
    fun removePlantObject(o: PlantObject) = viewModelScope.launch {
        db.plantObjectDao().delete(o)
        db.objectPhaseDao().deleteByObjectId(o.id)
    }
    fun updatePhasesForObject(objectId: String, newPhases: List<ObjectPhase>) = viewModelScope.launch {
        db.objectPhaseDao().deleteByObjectId(objectId)
        db.objectPhaseDao().insertAll(newPhases)
    }
    fun addSamen(s: Samen) = viewModelScope.launch { db.samenDao().insert(s) }
    fun updateSamen(s: Samen) = viewModelScope.launch { db.samenDao().update(s) }
    fun removeSamen(s: Samen) = viewModelScope.launch { db.samenDao().delete(s) }

    suspend fun createBackupJson(): String {
        val data = AppBackup(
            fertilizers = db.fertilizerDao().getAll().first(),
            recipes = db.recipeDao().getAll().first(),
            plantObjects = db.plantObjectDao().getAll().first(),
            objectPhases = db.objectPhaseDao().getAll().first(),
            seeds = db.samenDao().getAll().first(),
            phaseColors = _phaseColors.value,
            customPhases = _customPhases.value
        )
        return json.encodeToString(data)
    }

    suspend fun restoreFromBackup(jsonStr: String): Result<Pair<Int, Int>> {
        return try {
            // Support legacy "waterVolumeMl" field by replacing it with "totalVolumeMl" in the raw JSON string
            val adjustedJson = jsonStr.replace("\"waterVolumeMl\":", "\"totalVolumeMl\":")
            val b = json.decodeFromString<AppBackup>(adjustedJson)
            
            if (b.version > 1) return Result.failure(Exception("Version nicht unterstützt"))
            db.withTransaction {
                db.fertilizerDao().deleteAll(); db.recipeDao().deleteAll()
                db.plantObjectDao().deleteAll(); db.objectPhaseDao().deleteAll(); db.samenDao().deleteAll()
                db.fertilizerDao().insertAll(b.fertilizers); db.recipeDao().insertAll(b.recipes)
                db.plantObjectDao().insertAll(b.plantObjects); db.objectPhaseDao().insertAll(b.objectPhases)
                db.samenDao().insertAll(b.seeds)
            }
            if (b.phaseColors.isNotEmpty()) {
                phaseColorPrefs.setAllColors(b.phaseColors)
                _phaseColors.value = phaseColorPrefs.getAllColors()
            }
            // Backups from older versions have no list: take the custom names used in the restored data
            setCustomPhases(b.customPhases ?: customPhasesFrom(b.objectPhases))
            Result.success(b.seeds.size to b.recipes.size)
        } catch (e: Exception) { Result.failure(e) }
    }
}
