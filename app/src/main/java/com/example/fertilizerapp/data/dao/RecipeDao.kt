package com.example.fertilizerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fertilizerapp.data.model.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes")
    fun getAll(): Flow<List<Recipe>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(r: Recipe)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Recipe>)
    @androidx.room.Update
    suspend fun update(r: Recipe)
    @Delete
    suspend fun delete(r: Recipe)
    @Query("DELETE FROM recipes")
    suspend fun deleteAll()
}
