package com.example.fertilizerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "plant_objects")
data class PlantObject(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String
)

@Serializable
@Entity(tableName = "object_phases")
data class ObjectPhase(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val objectId: String,
    val name: String,
    val dateIso: String
)
