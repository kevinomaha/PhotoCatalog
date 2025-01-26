package com.example.photocatalog.data.models

import java.util.Date

data class Photo(
    val id: String = "",
    val projectId: String = "",
    val googlePhotosId: String = "",
    val url: String = "",
    val thumbnailUrl: String = "",
    val location: Location? = null,
    val cost: Double? = null,
    val observations: String = "",
    val rating: Int = 0,
    val timestamp: Date = Date(),
    val tags: List<String> = emptyList()
)

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
)
