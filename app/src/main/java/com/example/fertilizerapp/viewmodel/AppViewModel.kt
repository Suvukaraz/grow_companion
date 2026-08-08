package com.example.fertilizerapp.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.fertilizerapp.data.db.AppBackup
import com.example.fertilizerapp.data.db.AppDatabase
import com.example.fertilizerapp.data.model.Fertilizer
import com.example.fertilizerapp.data.model.ObjectPhase
import com.example.fertilizerapp.data.model.PlantObject
import com.example.fertilizerapp.data.model.Recipe
import com.example.fertilizerapp.data.model.Samen
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
            "anzahl" -> list.sortedWith(compareBy<Samen> { it.anzahl }.thenBy { it.strain })
            "autoFem" -> list.sortedWith(compareBy<Samen> { it.autoFem }.thenBy { it.strain })
            "breeder" -> list.sortedWith(compareBy<Samen> { it.breeder }.thenBy { it.strain })
            "zeit" -> list.sortedWith(compareBy<Samen> { it.zeitTage }.thenBy { it.strain })
            "thc" -> list.sortedWith(compareBy<Samen> { it.thcWert }.thenBy { it.strain })
            else -> list.sortedBy { it.strain }
        }
        if (asc) sorted else sorted.reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        ensureDefaultData()
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
            seeds = db.samenDao().getAll().first()
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
            Result.success(b.seeds.size to b.recipes.size)
        } catch (e: Exception) { Result.failure(e) }
    }
}
