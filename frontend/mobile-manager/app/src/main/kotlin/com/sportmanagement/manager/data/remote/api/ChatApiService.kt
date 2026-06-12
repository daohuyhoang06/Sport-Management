package com.sportmanagement.manager.data.remote.api

import com.sportmanagement.manager.data.remote.dto.ChatListResponse
import com.sportmanagement.manager.data.remote.dto.ChatMessagesResponse
import com.sportmanagement.manager.data.remote.dto.SendMessageRequest
import com.sportmanagement.manager.data.remote.dto.SendMessageResponse
import com.sportmanagement.manager.data.remote.dto.StartChatRequest
import com.sportmanagement.manager.data.remote.dto.StartChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApiService {

    @GET("api/chat/list")
    suspend fun getChatList(): Response<ChatListResponse>

    @GET("api/chat/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: Int): Response<ChatMessagesResponse>

    @POST("api/chat/{chatId}/send")
    suspend fun sendMessage(
        @Path("chatId") chatId: Int,
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>

    @POST("api/manager/chat/start")
    suspend fun startManagerChat(@Body request: StartChatRequest): Response<StartChatResponse>
}
