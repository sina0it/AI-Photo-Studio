package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val imagePath: String,
    val thumbnailPath: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val width: Int = 0,
    val height: Int = 0,
    val filterName: String? = null,
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f
)
