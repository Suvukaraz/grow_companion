package com.example.fertilizerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/** Nutrient concentration of a solution in % (w/v, i.e. g per 100 ml). */
data class NpkValues(val n: Double, val p: Double, val k: Double) {
    // 1 % (w/v) = 10 g/L = 10000 mg/L
    val nPpm get() = n * 10_000
    val pPpm get() = p * 10_000
    val kPpm get() = k * 10_000
}

@Serializable
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val totalVolumeMl: Int,
    val dilutionInstruction: String,
    val ingredients: List<RecipeIngredient>
) {
    /**
     * Mass of each nutrient (g) divided by the total solution volume (ml).
     * Liquid fertilizers are dosed in ml, so their mass is ml * density (g/ml);
     * the label percentages are treated as w/w.
     */
    fun calculateNpk(): NpkValues? {
        if (totalVolumeMl <= 0 || ingredients.isEmpty()) return null

        fun nutrientGrams(percent: (Fertilizer) -> Double) = ingredients.sumOf {
            val grams = if (it.fertilizer.type == FertilizerType.LIQUID) it.amount * it.fertilizer.density else it.amount
            grams * percent(it.fertilizer) / 100.0
        }

        return NpkValues(
            n = nutrientGrams { it.n } / totalVolumeMl * 100.0,
            p = nutrientGrams { it.p } / totalVolumeMl * 100.0,
            k = nutrientGrams { it.k } / totalVolumeMl * 100.0
        )
    }

    /**
     * Ingredients store a copy of the fertilizer from the moment they were added.
     * Replace those copies with the current fertilizer data so later corrections
     * (NPK, density, type) are reflected. Deleted fertilizers keep their copy.
     */
    fun withCurrentFertilizers(current: List<Fertilizer>): Recipe {
        val byId = current.associateBy { it.id }
        return copy(ingredients = ingredients.map { ing ->
            byId[ing.fertilizer.id]?.let { ing.copy(fertilizer = it) } ?: ing
        })
    }
}
