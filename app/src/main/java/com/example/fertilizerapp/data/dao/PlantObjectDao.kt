package com.example.fertilizerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fertilizerapp.data.model.ObjectPhase
import com.example.fertilizerapp.data.model.PlantObject
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantObjectDao {
    @Query("SELECT * FROM plant_objects")
    fun getAll(): Flow<List<PlantObject>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(o: PlantObject)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<PlantObject>)
    @Update
    suspend fun update(o: PlantObject)
    @Delete
    suspend fun delete(o: PlantObject)
    @Query("DELETE FROM plant_objects")
    suspend fun deleteAll()
}

@Dao
interface ObjectPhaseDao {
    @Query("SELECT * FROM object_phases")
    fun getAll(): Flow<List<ObjectPhase>>
    @Query("SELECT * FROM object_phases WHERE objectId = :objectId")
    fun getByObjectId(objectId: String): Flow<List<ObjectPhase>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(p: ObjectPhase)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<ObjectPhase>)
    @Query("DELETE FROM object_phases WHERE objectId = :objectId")
    suspend fun deleteByObjectId(objectId: String)
    @Delete
    suspend fun delete(p: ObjectPhase)
    @Query("DELETE FROM object_phases")
    suspend fun deleteAll()
}
