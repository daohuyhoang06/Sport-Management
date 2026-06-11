package com.sportmanagement.manager.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["chatId"])]
)
data class MessageEntity(
    @PrimaryKey val messageId: Int,
    val chatId: Int,
    val senderId: Int,
    val content: String,
    val sentAt: String,
    val isRead: Boolean
)
