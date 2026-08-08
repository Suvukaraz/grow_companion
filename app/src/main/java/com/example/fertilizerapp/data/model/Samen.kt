package com.example.fertilizerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class AutoFem { AUTO, FEM }

@Serializable
@Entity(tableName = "seeds")
data class Samen(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val anzahl: Int,
    val autoFem: AutoFem,
    val strain: String,
    val breeder: String,
    val zeitTage: Int,
    val thcWert: Int
)
