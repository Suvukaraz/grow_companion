package com.example.fertilizerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val totalVolumeMl: Int,
    val dilutionInstruction: String,
    val ingredients: List<RecipeIngredient>
) {
    val npkResult: String
        get() {
            if (totalVolumeMl <= 0) return "Fehler: Volumen 0"
            if (ingredients.isEmpty()) return "Keine Zutaten"

            return try {
                val totalN = ingredients.sumOf { (it.amount * (if (it.fertilizer.type == FertilizerType.LIQUID) it.fertilizer.density else 1.0) * it.fertilizer.n) / 100.0 }
                val totalP = ingredients.sumOf { (it.amount * (if (it.fertilizer.type == FertilizerType.LIQUID) it.fertilizer.density else 1.0) * it.fertilizer.p) / 100.0 }
                val totalK = ingredients.sumOf { (it.amount * (if (it.fertilizer.type == FertilizerType.LIQUID) it.fertilizer.density else 1.0) * it.fertilizer.k) / 100.0 }

                val nPercent = (totalN / totalVolumeMl) * 100.0
                val pPercent = (totalP / totalVolumeMl) * 100.0
                val kPercent = (totalK / totalVolumeMl) * 100.0

                String.format(java.util.Locale.ROOT, "N: %.2f%% | P: %.2f%% | K: %.2f%%", nPercent, pPercent, kPercent)
            } catch (_: Exception) {
                "Berechnungsfehler"
            }
        }
}
