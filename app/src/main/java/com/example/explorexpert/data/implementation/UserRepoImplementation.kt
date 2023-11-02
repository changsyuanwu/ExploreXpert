package com.example.explorexpert.data.implementation

import android.util.Log
import com.example.explorexpert.data.model.User
import com.example.explorexpert.data.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepoImplementation @Inject constructor(
    db: FirebaseFirestore
): UserRepository {

    companion object {
        private const val TAG = "UserRepository"
    }

    private val userCollection = db.collection("users")

    override suspend fun setUser(user: User): String {
        return withContext(Dispatchers.IO) {
            val deferred = CompletableDeferred<String>()

            userCollection.document(user.id)
                .set(user)
                .addOnSuccessListener {
                    Log.d(TAG, "Created an user with id ${user.id}")
                    deferred.complete(user.id)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to create user with id ${user.id}", e)
                    deferred.completeExceptionally(e)
                }

            deferred.await()
        }
    }

    override suspend fun getUserById(userId: String): User? {
        return withContext(Dispatchers.IO) {
            val deferred = CompletableDeferred<User?>()

            userCollection.document(userId)
                .get()
                .addOnSuccessListener { doc ->
                    try {
                        val user = doc.toObject(User::class.java)
                        deferred.complete(user)
                    } catch (e: Exception) {
                        Log.e(
                            TAG,
                            "Error casting user with ID $userId to User object: ${e.message}",
                            e
                        )
                        deferred.complete(null)
                    }
                }
            deferred.await()
        }
    }
}