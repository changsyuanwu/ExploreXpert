package com.example.explorexpert.data.repository

import android.net.Uri

interface FirebaseStorageRepository {

    suspend fun uploadFile(folderPath: String, fileUri: Uri): String

    suspend fun deleteFile(fileUrl: String)
}