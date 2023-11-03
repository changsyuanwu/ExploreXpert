package com.example.explorexpert.modules

import com.example.explorexpert.data.implementation.TripRepoImplementation
import com.example.explorexpert.data.implementation.UserRepoImplementation
import com.example.explorexpert.data.repository.TripRepository
import com.example.explorexpert.data.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
abstract class TripModule {

    @Binds
    abstract fun bindTripRepository(
        tripRepoImplementation: TripRepoImplementation
    ): TripRepository
}