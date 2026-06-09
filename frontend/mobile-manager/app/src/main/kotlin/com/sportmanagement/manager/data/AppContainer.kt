package com.sportmanagement.manager.data

import android.content.Context
import androidx.room.Room
import com.sportmanagement.manager.data.local.SessionManager
import com.sportmanagement.manager.data.local.db.ChatDatabase
import com.sportmanagement.manager.data.remote.NetworkClient
import com.sportmanagement.manager.data.remote.api.AuthApiService
import com.sportmanagement.manager.data.remote.api.BookingApiService
import com.sportmanagement.manager.data.remote.api.ChatApiService
import com.sportmanagement.manager.data.remote.api.DashboardApiService
import com.sportmanagement.manager.data.remote.api.FieldApiService
import com.sportmanagement.manager.data.remote.api.ProfileApiService
import com.sportmanagement.manager.data.repository.AuthRepository
import com.sportmanagement.manager.data.repository.BookingRepository
import com.sportmanagement.manager.data.repository.ChatRepository
import com.sportmanagement.manager.data.repository.DashboardRepository
import com.sportmanagement.manager.data.repository.FieldRepository
import com.sportmanagement.manager.data.repository.ProfileRepository

object AppContainer {

    private lateinit var sessionManager: SessionManager

    lateinit var authRepository: AuthRepository
        private set
    lateinit var dashboardRepository: DashboardRepository
        private set
    lateinit var fieldRepository: FieldRepository
        private set
    lateinit var bookingRepository: BookingRepository
        private set
    lateinit var chatRepository: ChatRepository
        private set
    lateinit var profileRepository: ProfileRepository
        private set

    private var initialized = false

    fun initialize(context: Context) {
        if (initialized) return
        initialized = true

        sessionManager = SessionManager(context.applicationContext)

        val okHttpClient = NetworkClient.buildOkHttpClient { sessionManager.getToken() }

        val authApiService = NetworkClient.createService(AuthApiService::class.java, okHttpClient)
        val dashboardApiService = NetworkClient.createService(DashboardApiService::class.java, okHttpClient)
        val fieldApiService = NetworkClient.createService(FieldApiService::class.java, okHttpClient)
        val bookingApiService = NetworkClient.createService(BookingApiService::class.java, okHttpClient)
        val chatApiService = NetworkClient.createService(ChatApiService::class.java, okHttpClient)
        val profileApiService = NetworkClient.createService(ProfileApiService::class.java, okHttpClient)

        val chatDatabase = Room.databaseBuilder(
            context.applicationContext,
            ChatDatabase::class.java,
            ChatDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()

        authRepository = AuthRepository(authApiService, sessionManager)
        dashboardRepository = DashboardRepository(dashboardApiService)
        fieldRepository = FieldRepository(fieldApiService)
        bookingRepository = BookingRepository(bookingApiService)
        chatRepository = ChatRepository(chatApiService, chatDatabase)
        profileRepository = ProfileRepository(profileApiService)
    }
}
