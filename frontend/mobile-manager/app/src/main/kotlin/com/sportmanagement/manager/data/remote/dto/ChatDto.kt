package com.sportmanagement.manager.data.remote.dto

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

// MySQL returns is_read as tinyint (0/1); backend may also return boolean.
// This adapter handles both so Gson never throws on type mismatch.
private class FlexBooleanAdapter : JsonDeserializer<Boolean?> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): Boolean? {
        return when {
            json.isJsonNull    -> null
            json.isJsonPrimitive -> {
                val prim = json.asJsonPrimitive
                when {
                    prim.isBoolean -> prim.asBoolean
                    prim.isNumber  -> prim.asInt != 0
                    prim.isString  -> prim.asString.equals("true", ignoreCase = true)
                    else -> null
                }
            }
            else -> null
        }
    }
}

data class ChatListDto(
    @SerializedName("chat_id") val chatId: Int,
    @SerializedName("customer_id") val customerId: Int?,
    @SerializedName("manager_id") val managerId: Int?,
    @SerializedName("customer_name") val customerName: String?,
    @SerializedName("customer_phone") val customerPhone: String?,
    @SerializedName("customer_avatar") val customerAvatar: String?,
    @SerializedName("last_message") val lastMessage: String?,
    @SerializedName("last_message_time") val lastMessageTime: String?,
    @SerializedName("unread_count") val unreadCount: Int?
)

data class ChatMessageDto(
    @SerializedName("message_id") val messageId: Int,
    @SerializedName("chat_id") val chatId: Int,
    @SerializedName("sender_id") val senderId: Int,
    val content: String?,
    @SerializedName("sent_at") val sentAt: String?,
    @SerializedName("is_read") @JsonAdapter(FlexBooleanAdapter::class) val isRead: Boolean?
)

data class SendMessageRequest(val content: String)

data class SendMessageResponse(
    val success: Boolean?,
    val message: String?,
    val data: ChatMessageDto?
)

data class ChatListResponse(
    val success: Boolean?,
    val data: List<ChatListDto>?
)

data class ChatMessagesResponse(
    val success: Boolean?,
    val data: List<ChatMessageDto>?
)
