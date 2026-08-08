package com.example.fertilizerapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fertilizerapp.data.dao.FertilizerDao
import com.example.fertilizerapp.data.dao.ObjectPhaseDao
import com.example.fertilizerapp.data.dao.PlantObjectDao
import com.example.fertilizerapp.data.dao.RecipeDao
import com.example.fertilizerapp.data.dao.SamenDao
import com.example.fertilizerapp.data.model.AutoFem
import com.example.fertilizerapp.data.model.Fertilizer
import com.example.fertilizerapp.data.model.FertilizerType
import com.example.fertilizerapp.data.model.ObjectPhase
import com.example.fertilizerapp.data.model.PlantObject
import com.example.fertilizerapp.data.model.Recipe
import com.example.fertilizerapp.data.model.RecipeIngredient
import com.example.fertilizerapp.data.model.Samen
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AppTypeConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromIngredientList(value: List<RecipeIngredient>?): String = json.encodeToString(value ?: emptyList())

    @TypeConverter
    fun toIngredientList(value: String?): List<RecipeIngredient> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<RecipeIngredient>>(value)
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromFertilizerType(value: FertilizerType?): String = value?.name ?: FertilizerType.SOLID.name

    @TypeConverter
    fun toFertilizerType(value: String?): FertilizerType = try {
        value?.let { FertilizerType.valueOf(it) } ?: FertilizerType.SOLID
    } catch (_: Exception) {
        FertilizerType.SOLID
    }

    @TypeConverter
    fun fromAutoFem(value: AutoFem?): String = value?.name ?: AutoFem.AUTO.name

    @TypeConverter
    fun toAutoFem(value: String?): AutoFem = try {
        value?.let { AutoFem.valueOf(it) } ?: AutoFem.AUTO
    } catch (_: Exception) {
        AutoFem.AUTO
    }
}

@Database(entities = [Fertilizer::class, Recipe::class, PlantObject::class, ObjectPhase::class, Samen::class], version = 2, exportSchema = false)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fertilizerDao(): FertilizerDao
    abstract fun recipeDao(): RecipeDao
    abstract fun plantObjectDao(): PlantObjectDao
    abstract fun objectPhaseDao(): ObjectPhaseDao
    abstract fun samenDao(): SamenDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Helper to check if a column exists
                fun columnExists(tableName: String, columnName: String): Boolean {
                    val cursor = db.query("PRAGMA table_info($tableName)")
                    var exists = false
                    while (cursor.moveToNext()) {
                        val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                        if (name == columnName) {
                            exists = true
                            break
                        }
                    }
                    cursor.close()
                    return exists
                }

                // 1. Update fertilizers table: add density column if missing
                if (!columnExists("fertilizers", "density")) {
                    db.execSQL("ALTER TABLE fertilizers ADD COLUMN density REAL NOT NULL DEFAULT 1.0")
                }
                
                // 2. Update recipes table: Recreate to fix types and rename column
                val hasWaterVolume = columnExists("recipes", "waterVolumeMl")
                val hasDilution = columnExists("recipes", "dilutionInstruction")
                
                // Create new table with correct schema (Room expects totalVolumeMl as INTEGER)
                db.execSQL("""
                    CREATE TABLE recipes_new (
                        id TEXT NOT NULL PRIMARY KEY, 
                        name TEXT NOT NULL, 
                        totalVolumeMl INTEGER NOT NULL, 
                        dilutionInstruction TEXT NOT NULL, 
                        ingredients TEXT NOT NULL
                    )
                """.trimIndent())

                val volumeSource = if (hasWaterVolume) "waterVolumeMl" else "totalVolumeMl"
                val dilutionSource = if (hasDilution) "dilutionInstruction" else "''"

                // Copy data with CAST to ensure INTEGER type for volume
                db.execSQL("""
                    INSERT INTO recipes_new (id, name, totalVolumeMl, dilutionInstruction, ingredients)
                    SELECT id, name, CAST($volumeSource AS INTEGER), $dilutionSource, ingredients FROM recipes
                """.trimIndent())

                // Swap tables
                db.execSQL("DROP TABLE recipes")
                db.execSQL("ALTER TABLE recipes_new RENAME TO recipes")
            }
        }

        @Volatile private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "fertilizer_db")
                    .addMigrations(MIGRATION_1_2)
                    .build().also { instance = it }
            }
        }
    }
}
