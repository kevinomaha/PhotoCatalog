package com.example.photocatalog.data.source

import android.content.Context
import android.net.Uri
import com.example.photocatalog.data.models.Photo
import com.example.photocatalog.data.models.PhotoUpload
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotosRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val photosCollection = firestore.collection("photos")
    private val storageRef = storage.reference.child("photos")

    suspend fun uploadPhoto(photoUpload: PhotoUpload): Photo = withContext(Dispatchers.IO) {
        // Upload file to Firebase Storage
        val photoId = UUID.randomUUID().toString()
        val photoRef = storageRef.child("$photoId.jpg")
        
        val uploadTask = photoRef.putFile(photoUpload.uri).await()
        val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

        // Create photo document in Firestore
        val photo = Photo(
            id = photoId,
            projectId = photoUpload.projectId,
            url = downloadUrl,
            thumbnailUrl = downloadUrl, // You might want to create a separate thumbnail
            location = photoUpload.metadata.location,
            cost = photoUpload.metadata.cost,
            observations = photoUpload.metadata.observations,
            rating = photoUpload.metadata.rating,
            timestamp = Date(),
            tags = photoUpload.metadata.tags
        )

        // Save to Firestore
        photosCollection.document(photoId).set(photo).await()

        return@withContext photo
    }

    suspend fun getPhotos(projectId: String? = null): List<Photo> = withContext(Dispatchers.IO) {
        val query = if (projectId != null) {
            photosCollection.whereEqualTo("projectId", projectId)
        } else {
            photosCollection
        }

        return@withContext query.get().await().toObjects(Photo::class.java)
    }

    suspend fun getPhoto(photoId: String): Photo? = withContext(Dispatchers.IO) {
        return@withContext photosCollection.document(photoId).get().await()
            .toObject(Photo::class.java)
    }

    suspend fun deletePhoto(photoId: String) = withContext(Dispatchers.IO) {
        // Delete from Storage
        storageRef.child("$photoId.jpg").delete().await()
        
        // Delete from Firestore
        photosCollection.document(photoId).delete().await()
    }

    suspend fun updatePhotoMetadata(photoId: String, metadata: Map<String, Any>) = withContext(Dispatchers.IO) {
        photosCollection.document(photoId).update(metadata).await()
    }

    fun getPhotoFile(fileName: String): File {
        val photosDir = File(context.filesDir, "photos")
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }
        return File(photosDir, fileName)
    }
}
