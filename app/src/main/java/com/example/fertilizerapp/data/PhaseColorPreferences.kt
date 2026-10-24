package com.example.fertilizerapp.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color

class PhaseColorPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("phase_color_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_SAMLING = "sämling"
        const val KEY_WACHSTUM = "wachstum"
        const val KEY_BLUETE = "blüte"
        const val KEY_TROCKNUNG = "trocknung"

        val DEFAULT_COLORS = mapOf(
            KEY_SAMLING to 0xFF8BC34A.toInt(),  // Seedling Green
            KEY_WACHSTUM to 0xFF43A047.toInt(), // Vegetative Green
            KEY_BLUETE to 0xFFAB47BC.toInt(),   // Flowering Purple
            KEY_TROCKNUNG to 0xFFFFCA28.toInt() // Drying/Harvest Gold
        )

        const val DEFAULT_FALLBACK_COLOR = 0xFF73796E.toInt()

        /** Built-in phases, in growing order. */
        val STANDARD_PHASES = listOf("Sämling", "Wachstum", "Blüte", "Trocknung")

        private const val KEY_CUSTOM_PHASES = "custom_phases"

        /** True if the name is covered by one of the standard phases (same keywords as the color matching). */
        fun isStandardPhase(phaseName: String): Boolean {
            val norm = phaseName.lowercase().trim()
            return listOf("sämling", "keimling", "veg", "wachstum", "blüte", "blute", "trocknung", "ernte", "curing")
                .any { norm.contains(it) }
        }

        fun getColorForPhase(phaseName: String, colorMap: Map<String, Int>): Color {
            val norm = phaseName.lowercase().trim()
            if (norm.isEmpty()) return Color(DEFAULT_FALLBACK_COLOR)

            // Direct match
            if (colorMap.containsKey(norm)) {
                return Color(colorMap[norm]!!)
            }

            // Keyword match
            return when {
                norm.contains("sämling") || norm.contains("keimling") -> {
                    Color(colorMap[KEY_SAMLING] ?: DEFAULT_COLORS[KEY_SAMLING]!!)
                }
                norm.contains("wachstum") || norm.contains("veg") -> {
                    Color(colorMap[KEY_WACHSTUM] ?: DEFAULT_COLORS[KEY_WACHSTUM]!!)
                }
                norm.contains("blüte") || norm.contains("blute") -> {
                    Color(colorMap[KEY_BLUETE] ?: DEFAULT_COLORS[KEY_BLUETE]!!)
                }
                norm.contains("trocknung") || norm.contains("ernte") || norm.contains("curing") -> {
                    Color(colorMap[KEY_TROCKNUNG] ?: DEFAULT_COLORS[KEY_TROCKNUNG]!!)
                }
                else -> {
                    Color(colorMap["default"] ?: DEFAULT_FALLBACK_COLOR)
                }
            }
        }
    }

    fun getAllColors(): Map<String, Int> {
        val result = DEFAULT_COLORS.toMutableMap()
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("color_") && value is Int) {
                val phaseKey = key.removePrefix("color_")
                result[phaseKey] = value
            }
        }
        return result
    }

    fun setColor(phaseKey: String, colorInt: Int) {
        val normKey = phaseKey.lowercase().trim()
        prefs.edit().putInt("color_$normKey", colorInt).apply()
    }

    fun setAllColors(map: Map<String, Int>) {
        val editor = prefs.edit()
        map.forEach { (key, value) ->
            editor.putInt("color_${key.lowercase().trim()}", value)
        }
        editor.apply()
    }

    fun resetToDefaults() {
        // Only colors; the list of custom phases stays
        val editor = prefs.edit()
        prefs.all.keys.filter { it.startsWith("color_") }.forEach { editor.remove(it) }
        editor.apply()
    }

    fun removeColor(phaseKey: String) {
        prefs.edit().remove("color_${phaseKey.lowercase().trim()}").apply()
    }

    /** Null if the list was never saved (used to migrate names typed in older versions). */
    fun getCustomPhases(): List<String>? =
        prefs.getString(KEY_CUSTOM_PHASES, null)?.split('\n')?.filter { it.isNotBlank() }

    fun setCustomPhases(names: List<String>) {
        prefs.edit().putString(KEY_CUSTOM_PHASES, names.joinToString("\n")).apply()
    }
}
