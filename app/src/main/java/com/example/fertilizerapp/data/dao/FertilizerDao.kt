package com.example.fertilizerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fertilizerapp.data.model.Fertilizer
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizerDao {
    @Query("SELECT * FROM fertilizers")
    fun getAll(): Flow<List<Fertilizer>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(f: Fertilizer)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Fertilizer>)
    @androidx.room.Update
    suspend fun update(f: Fertilizer)
    @Delete
    suspend fun delete(f: Fertilizer)
    @Query("DELETE FROM fertilizers")
    suspend fun deleteAll()
}
