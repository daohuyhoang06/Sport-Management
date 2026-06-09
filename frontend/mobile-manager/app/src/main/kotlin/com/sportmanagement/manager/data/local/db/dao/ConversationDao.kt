package com.sportmanagement.manager.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sportmanagement.manager.data.local.db.entity.ConversationEntity

@Dao
interface ConversationDao {

    @Query("SELECT * FROM chat_conversations ORDER BY savedAt DESC")
    suspend fun getAllConversations(): List<ConversationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Query("UPDATE chat_conversations SET unreadCount = 0 WHERE chatId = :chatId")
    suspend fun markAsRead(chatId: Int)
}
