package com.example.explorexpert.data.implementation

import com.example.explorexpert.data.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class UserRepoImplementation @Inject constructor(
    db: FirebaseFirestore
): UserRepository {

}