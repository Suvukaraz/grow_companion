package com.example.fertilizerapp

import com.example.fertilizerapp.data.model.Fertilizer
import com.example.fertilizerapp.data.model.FertilizerType
import com.example.fertilizerapp.data.model.Recipe
import com.example.fertilizerapp.data.model.RecipeIngredient
import com.example.fertilizerapp.util.formatCompact
import com.example.fertilizerapp.util.toDecimalOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecipeNpkTest {
    private val hakaphosBlau = Fertilizer(id = "blau", name = "Hakaphos Blau", n = 15.0, p = 10.0, k = 15.0)
    private val liquid = Fertilizer(id = "flk", name = "Flüssig", type = FertilizerType.LIQUID, density = 1.25, n = 4.0, p = 2.0, k = 6.0)

    private fun recipe(volumeMl: Int, vararg ingredients: RecipeIngredient) =
        Recipe(name = "Test", totalVolumeMl = volumeMl, dilutionInstruction = "", ingredients = ingredients.toList())

    @Test
    fun solidFertilizer() {
        // 100 g with 15 % N = 15 g N in 1000 ml -> 1.5 % (w/v) = 15000 mg/L
        val npk = recipe(1000, RecipeIngredient(hakaphosBlau, 100.0)).calculateNpk()!!
        assertEquals(1.5, npk.n, 1e-9)
        assertEquals(1.0, npk.p, 1e-9)
        assertEquals(1.5, npk.k, 1e-9)
        assertEquals(15_000.0, npk.nPpm, 1e-6)
    }

    @Test
    fun liquidFertilizerUsesDensity() {
        // 100 ml * 1.25 g/ml = 125 g, 4 % N = 5 g N in 500 ml -> 1 %
        val npk = recipe(500, RecipeIngredient(liquid, 100.0)).calculateNpk()!!
        assertEquals(1.0, npk.n, 1e-9)
        assertEquals(0.5, npk.p, 1e-9)
        assertEquals(1.5, npk.k, 1e-9)
    }

    @Test
    fun mixedIngredientsAreSummed() {
        // 15 g + 5 g N = 20 g N in 1000 ml -> 2 %
        val npk = recipe(1000, RecipeIngredient(hakaphosBlau, 100.0), RecipeIngredient(liquid, 100.0)).calculateNpk()!!
        assertEquals(2.0, npk.n, 1e-9)
    }

    @Test
    fun dilutedSolutionInPpm() {
        // 1 g Hakaphos Blau in 10 L -> 0.15 g N / 10 L = 15 mg/L
        val npk = recipe(10_000, RecipeIngredient(hakaphosBlau, 1.0)).calculateNpk()!!
        assertEquals(15.0, npk.nPpm, 1e-9)
    }

    @Test
    fun invalidInputGivesNull() {
        assertNull(recipe(0, RecipeIngredient(hakaphosBlau, 1.0)).calculateNpk())
        assertNull(recipe(1000).calculateNpk())
    }

    @Test
    fun currentFertilizerDataIsUsed() {
        val old = recipe(1000, RecipeIngredient(hakaphosBlau.copy(n = 0.0), 100.0))
        val npk = old.withCurrentFertilizers(listOf(hakaphosBlau)).calculateNpk()!!
        assertEquals(1.5, npk.n, 1e-9)
    }

    @Test
    fun decimalCommaIsAccepted() {
        assertEquals(1.25, "1,25".toDecimalOrNull()!!, 1e-9)
        assertEquals(1.25, " 1.25 ".toDecimalOrNull()!!, 1e-9)
        assertNull("abc".toDecimalOrNull())
    }

    @Test
    fun compactFormatting() {
        assertEquals("5", 5.0.formatCompact())
        assertEquals("2.5", 2.50.formatCompact())
        assertEquals("1.25", 1.25.formatCompact(4))
    }
}
