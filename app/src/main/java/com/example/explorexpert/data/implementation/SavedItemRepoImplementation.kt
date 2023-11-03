package com.example.explorexpert.data.implementation

import com.example.explorexpert.data.repository.SavedItemRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class SavedItemRepoImplementation @Inject constructor(
    db: FirebaseFirestore
): SavedItemRepository {

}