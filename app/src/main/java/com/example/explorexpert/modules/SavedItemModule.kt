package com.example.explorexpert.modules

import com.example.explorexpert.data.implementation.SavedItemRepoImplementation
import com.example.explorexpert.data.implementation.TripRepoImplementation
import com.example.explorexpert.data.repository.SavedItemRepository
import com.example.explorexpert.data.repository.TripRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
abstract class SavedItemModule {

    @Binds
    abstract fun bindTripRepository(
        savedItemRepoImplementation: SavedItemRepoImplementation
    ): SavedItemRepository
}