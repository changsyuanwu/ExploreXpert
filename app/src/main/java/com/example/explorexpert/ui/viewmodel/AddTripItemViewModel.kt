package com.example.explorexpert.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.explorexpert.data.repository.TripRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class AddTripItemViewModel@Inject constructor(
    private val auth: FirebaseAuth,
    private val tripRepo: TripRepository,
) : ViewModel() {

}