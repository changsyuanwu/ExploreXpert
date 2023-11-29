package com.example.explorexpert.modules

import com.example.explorexpert.data.implementation.EventRepoImplementation
import com.example.explorexpert.data.repository.EventRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
abstract class EventModule {
    @Binds
    abstract fun bindEventRepository(
        eventRepoImplementation: EventRepoImplementation
    ): EventRepository
}