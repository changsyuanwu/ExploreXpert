package com.example.explorexpert.ui.viewmodel

import com.example.explorexpert.data.repository.TripRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val tripRepo: TripRepository,
) {
}