package com.example.photocatalog.di

import android.content.Context
import com.example.photocatalog.data.source.AuthRepository
import com.example.photocatalog.data.source.CameraService
import com.example.photocatalog.data.source.PhotosRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepository()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun providePhotosRepository(
        @ApplicationContext context: Context,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): PhotosRepository {
        return PhotosRepository(context, firestore, storage)
    }

    @Provides
    @Singleton
    fun provideCameraService(
        @ApplicationContext context: Context
    ): CameraService {
        return CameraService(context)
    }
}
