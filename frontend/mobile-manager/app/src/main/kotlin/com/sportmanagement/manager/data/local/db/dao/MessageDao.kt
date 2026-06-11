package com.sportmanagement.manager.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sportmanagement.manager.data.local.db.entity.MessageEntity

@Dao
interface MessageDao {

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY sentAt ASC")
    suspend fun getMessages(chatId: Int): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM chat_messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: Int)
}
