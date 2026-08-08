package com.example.fertilizerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class FertilizerType { SOLID, LIQUID }

@Serializable
@Entity(tableName = "fertilizers")
data class Fertilizer(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val manufacturer: String = "",
    val type: FertilizerType = FertilizerType.SOLID,
    val density: Double = 1.0,
    val n: Double,
    val p: Double,
    val k: Double
)

@Serializable
data class RecipeIngredient(
    val fertilizer: Fertilizer,
    val amount: Double
)
