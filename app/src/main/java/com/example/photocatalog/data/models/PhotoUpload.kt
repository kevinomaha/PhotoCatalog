package com.example.photocatalog.data.models

import android.net.Uri
import java.io.File

data class PhotoUpload(
    val file: File,
    val uri: Uri,
    val description: String = "",
    val projectId: String = "",
    val metadata: PhotoMetadata = PhotoMetadata()
)

data class PhotoMetadata(
    val cost: Double? = null,
    val rating: Int = 0,
    val observations: String = "",
    val location: Location? = null,
    val tags: List<String> = emptyList()
)
