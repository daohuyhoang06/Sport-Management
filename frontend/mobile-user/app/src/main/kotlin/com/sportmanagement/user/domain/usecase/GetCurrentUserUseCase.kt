package com.sportmanagement.user.domain.usecase

import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.domain.repository.UserRepository

class GetCurrentUserUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): UserProfile {
        return repository.getProfile()
    }
}
