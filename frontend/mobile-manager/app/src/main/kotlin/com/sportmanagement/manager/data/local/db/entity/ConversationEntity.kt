package com.sportmanagement.manager.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_conversations")
data class ConversationEntity(
    @PrimaryKey val chatId: Int,
    val customerId: Int,
    val managerId: Int,
    val customerName: String,
    val customerPhone: String?,
    val customerAvatar: String?,
    val lastMessage: String?,
    val lastMessageTime: String?,
    val unreadCount: Int,
    val savedAt: Long
)
