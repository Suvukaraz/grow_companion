package com.example.fertilizerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fertilizerapp.data.model.Samen
import kotlinx.coroutines.flow.Flow

@Dao
interface SamenDao {
    @Query("SELECT * FROM seeds")
    fun getAll(): Flow<List<Samen>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(s: Samen)
    @Update
    suspend fun update(s: Samen)
    @Delete
    suspend fun delete(s: Samen)
    @Query("DELETE FROM seeds")
    suspend fun deleteAll()
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Samen>)
}
