package com.example.fertilizerapp.data.db

import com.example.fertilizerapp.data.model.Fertilizer
import com.example.fertilizerapp.data.model.ObjectPhase
import com.example.fertilizerapp.data.model.PlantObject
import com.example.fertilizerapp.data.model.Recipe
import com.example.fertilizerapp.data.model.Samen
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AppBackup(
    val version: Int = 1,
    val fertilizers: List<Fertilizer>,
    val recipes: List<Recipe>,
    val plantObjects: List<PlantObject>,
    val objectPhases: List<ObjectPhase>,
    val seeds: List<Samen>,
    val phaseColors: Map<String, Int> = emptyMap(),
    val customPhases: List<String>? = null,
    /** "full" or "timer"; missing in old backups (only the full app could create them). */
    val appType: String? = null
) {
    /**
     * Grow Helper has no fertilizer/recipe screens, so its backups must not replace
     * fertilizers and nutrient solutions when imported into the full app.
     */
    val isPartial: Boolean get() = appType == APP_TYPE_TIMER

    companion object {
        const val APP_TYPE_TIMER = "timer"
    }
}

data class RestoreResult(val seeds: Int, val recipes: Int, val partial: Boolean)

val BackupJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    coerceInputValues = true
}

fun decodeBackup(jsonStr: String): AppBackup {
    // Support legacy "waterVolumeMl" field by replacing it with "totalVolumeMl" in the raw JSON string
    val adjustedJson = jsonStr.replace("\"waterVolumeMl\":", "\"totalVolumeMl\":")
    return BackupJson.decodeFromString<AppBackup>(adjustedJson)
}
