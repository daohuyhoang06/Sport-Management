import sequelize from '../config/database.js';

/**
 * Get or create chat between user and manager
 */
export const getOrCreateChatService = async (userId, managerId) => {
  try {
    // Check if chat exists
    const [chats] = await sequelize.query(
      `SELECT * FROM chats 
       WHERE (user_id = ? AND manager_id = ?) 
       OR (user_id = ? AND manager_id = ?)
       LIMIT 1`,
      { replacements: [userId, managerId, managerId, userId] }
    );

    if (chats.length > 0) {
      return chats[0];
    }

    // Create new chat
    const [result] = await sequelize.query(
      `INSERT INTO chats (user_id, manager_id, created_at, updated_at) 
       VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)`,
      { replacements: [userId, managerId] }
    );

    return {
      chat_id: result,
      user_id: userId,
      manager_id: managerId,
      created_at: new Date(),
      updated_at: new Date()
    };
  } catch (error) {
    throw new Error('Lỗi khi tạo/lấy chat: ' + error.message);
  }
};

/**
 * Get all chats for a user (as customer or manager)
 */
export const getUserChatsService = async (personId, role) => {
  try {
    let query;
    if (role === 'manager') {
      query = `
        SELECT c.*, 
               p.person_name as user_name,
               p.email as user_email,
               (SELECT message_text FROM messages 
                WHERE chat_id = c.chat_id 
                ORDER BY created_at DESC LIMIT 1) as last_message,
               (SELECT created_at FROM messages 
                WHERE chat_id = c.chat_id 
                ORDER BY created_at DESC LIMIT 1) as last_message_time,
               (SELECT COUNT(*) FROM messages 
                WHERE chat_id = c.chat_id AND sender_id != ? AND is_read = 0) as unread_count
        FROM chats c
        JOIN person p ON c.user_id = p.person_id
        WHERE c.manager_id = ?
        ORDER BY c.updated_at DESC
      `;
    } else {
      query = `
        SELECT c.*, 
               p.person_name as manager_name,
               p.email as manager_email,
               (SELECT message_text FROM messages 
                WHERE chat_id = c.chat_id 
                ORDER BY created_at DESC LIMIT 1) as last_message,
               (SELECT created_at FROM messages 
                WHERE chat_id = c.chat_id 
                ORDER BY created_at DESC LIMIT 1) as last_message_time,
               (SELECT COUNT(*) FROM messages 
                WHERE chat_id = c.chat_id AND sender_id != ? AND is_read = 0) as unread_count
        FROM chats c
        JOIN person p ON c.manager_id = p.person_id
        WHERE c.user_id = ?
        ORDER BY c.updated_at DESC
      `;
    }

    const [chats] = await sequelize.query(query, {
      replacements: [personId, personId]
    });

    return chats;
  } catch (error) {
    throw new Error('Lỗi khi lấy danh sách chat: ' + error.message);
  }
};

/**
 * Get messages for a chat
 */
export const getChatMessagesService = async (chatId, personId) => {
  try {
    const [messages] = await sequelize.query(
      `SELECT m.*, p.person_name as sender_name
       FROM messages m
       JOIN person p ON m.sender_id = p.person_id
       WHERE m.chat_id = ?
       ORDER BY m.created_at ASC`,
      { replacements: [chatId] }
    );

    // Mark messages as read
    await sequelize.query(
      `UPDATE messages 
       SET is_read = 1 
       WHERE chat_id = ? AND sender_id != ? AND is_read = 0`,
      { replacements: [chatId, personId] }
    );

    return messages;
  } catch (error) {
    throw new Error('Lỗi khi lấy tin nhắn: ' + error.message);
  }
};

/**
 * Send a message
 */
export const sendMessageService = async (chatId, senderId, messageText) => {
  try {
    const [result] = await sequelize.query(
      `INSERT INTO messages (chat_id, sender_id, message_text, is_read, created_at) 
       VALUES (?, ?, ?, 0, CURRENT_TIMESTAMP)`,
      { replacements: [chatId, senderId, messageText] }
    );

    // Update chat's updated_at
    await sequelize.query(
      `UPDATE chats SET updated_at = CURRENT_TIMESTAMP WHERE chat_id = ?`,
      { replacements: [chatId] }
    );

    return {
      message_id: result,
      chat_id: chatId,
      sender_id: senderId,
      message_text: messageText,
      is_read: 0,
      created_at: new Date()
    };
  } catch (error) {
    throw new Error('Lỗi khi gửi tin nhắn: ' + error.message);
  }
};

/**
 * Get list of managers to chat with
 */
export const getAvailableManagersService = async () => {
  try {
    const [managers] = await sequelize.query(
      `SELECT person_id, person_name, email 
       FROM person 
       WHERE role = 'manager' AND status = 'active'
       ORDER BY person_name ASC`
    );

    return managers;
  } catch (error) {
    throw new Error('Lỗi khi lấy danh sách manager: ' + error.message);
  }
};
