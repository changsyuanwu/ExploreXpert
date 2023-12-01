package com.example.explorexpert.modules

import com.example.explorexpert.data.implementation.FirebaseStorageRepoImplementation
import com.example.explorexpert.data.repository.FirebaseStorageRepository
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent


@Module
@InstallIn(FragmentComponent::class)
abstract class FirebaseStorageModule {

    @Binds
    abstract fun bindFirebaseStorageRepository(
        firebaseStorageRepoImplementation: FirebaseStorageRepoImplementation
    ): FirebaseStorageRepository

    companion object {
        @Provides
        fun provideFirebaseStorage(): FirebaseStorage {
            return FirebaseStorage.getInstance()
        }
    }
}