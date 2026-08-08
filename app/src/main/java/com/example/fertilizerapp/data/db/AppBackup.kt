package com.example.fertilizerapp.data.db

import com.example.fertilizerapp.data.model.Fertilizer
import com.example.fertilizerapp.data.model.ObjectPhase
import com.example.fertilizerapp.data.model.PlantObject
import com.example.fertilizerapp.data.model.Recipe
import com.example.fertilizerapp.data.model.Samen
import kotlinx.serialization.Serializable

@Serializable
data class AppBackup(
    val version: Int = 1,
    val fertilizers: List<Fertilizer>,
    val recipes: List<Recipe>,
    val plantObjects: List<PlantObject>,
    val objectPhases: List<ObjectPhase>,
    val seeds: List<Samen>
)
