package com.sportmanagement.manager.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sportmanagement.manager.data.local.db.dao.ConversationDao
import com.sportmanagement.manager.data.local.db.dao.MessageDao
import com.sportmanagement.manager.data.local.db.entity.ConversationEntity
import com.sportmanagement.manager.data.local.db.entity.MessageEntity

@Database(
    entities = [MessageEntity::class, ConversationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao

    companion object {
        const val DATABASE_NAME = "chat_db"
    }
}
