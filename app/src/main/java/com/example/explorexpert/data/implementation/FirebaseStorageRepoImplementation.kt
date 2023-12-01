package com.example.explorexpert.data.implementation

import android.net.Uri
import android.util.Log
import com.example.explorexpert.data.repository.FirebaseStorageRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class FirebaseStorageRepoImplementation @Inject constructor(
    private val storage: FirebaseStorage
): FirebaseStorageRepository {
    override suspend fun uploadFile(folderPath: String, fileUri: Uri): String {
        return withContext(Dispatchers.IO) {
            val fileName = "${fileUri.lastPathSegment}_${UUID.randomUUID()}"
            val storageRef = storage
                .reference
                .child("$folderPath/$fileName")
            val upload = storageRef.putFile(fileUri).await()




            if (upload.task.isSuccessful) {
                val filePublicURL = upload.metadata?.reference?.downloadUrl?.await()
                return@withContext filePublicURL.toString()
            } else {
                val errorMsg = upload.task.exception?.message
                throw Exception("Failed to upload file: $errorMsg")
            }
        }
    }

    override suspend fun deleteFile(fileUrl: String) {
        return withContext(Dispatchers.IO) {

            val imageRef = storage.getReferenceFromUrl(fileUrl)
            imageRef.delete()
                .addOnSuccessListener {
                    Log.i("FirebaseStorageRepoImpl", "File successfully deleted: $fileUrl")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseStorageRepoImpl", "Error deleting file: ${e.message}", e)
                }
        }
    }
}