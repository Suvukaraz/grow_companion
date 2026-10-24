package com.example.fertilizerapp

import com.example.fertilizerapp.data.db.AppBackup
import com.example.fertilizerapp.data.db.BackupJson
import com.example.fertilizerapp.data.db.decodeBackup
import com.example.fertilizerapp.data.model.Fertilizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupTest {
    private val oldBackup = """
        {
          "version": 1,
          "fertilizers": [{"id": "f1", "name": "Hakaphos Blau", "n": 15.0, "p": 10.0, "k": 15.0}],
          "recipes": [],
          "plantObjects": [{"id": "o1", "name": "Zelt 1"}],
          "objectPhases": [],
          "seeds": [{"id": 1, "anzahl": 3, "autoFem": "FEM", "strain": "X", "breeder": "Y", "zeitTage": 60, "thcWert": 22}]
        }
    """.trimIndent()

    @Test
    fun oldBackupWithoutAppTypeIsFullBackup() {
        val b = decodeBackup(oldBackup)
        assertNull(b.appType)
        assertFalse(b.isPartial)
        // Old integer THC values and missing sortOrder still load
        assertEquals(22.0, b.seeds.single().thcWert, 1e-9)
        assertEquals(0, b.plantObjects.single().sortOrder)
    }

    @Test
    fun timerBackupIsPartial() {
        val b = decodeBackup(oldBackup.replace("\"version\": 1,", "\"version\": 1, \"appType\": \"timer\","))
        assertTrue(b.isPartial)
    }

    @Test
    fun appTypeSurvivesRoundTrip() {
        val original = AppBackup(
            fertilizers = listOf(Fertilizer(id = "f1", name = "Test", n = 1.0, p = 2.0, k = 3.0)),
            recipes = emptyList(),
            plantObjects = emptyList(),
            objectPhases = emptyList(),
            seeds = emptyList(),
            appType = "full"
        )
        val restored = decodeBackup(BackupJson.encodeToString(AppBackup.serializer(), original))
        assertEquals("full", restored.appType)
        assertFalse(restored.isPartial)
    }
}
