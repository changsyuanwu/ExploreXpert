package com.example.explorexpert.modules

import com.example.explorexpert.data.implementation.UserRepoImplementation
import com.example.explorexpert.data.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
abstract class UserModule {

    @Binds
    abstract fun bindUserRepository(
        userRepoImplementation: UserRepoImplementation
    ): UserRepository
}