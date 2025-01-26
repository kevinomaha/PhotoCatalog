package com.example.photocatalog.data.models

import java.util.Date

data class Project(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val created: Date = Date(),
    val updated: Date = Date(),
    val photoCount: Int = 0,
    val coverPhotoUrl: String? = null
)
