import sequelize from '../config/database.js';
import { sendPushToUser } from './user/pushNotificationService.js';

/**
 * Get or create chat between user and manager
 */
export const getOrCreateChatService = async (userId, managerId) => {
  try {
    const [chats] = await sequelize.query(
      `SELECT * FROM chats
       WHERE (user_id = ? AND manager_id = ?)
       OR (user_id = ? AND manager_id = ?)
       LIMIT 1`,
      { replacements: [userId, managerId, managerId, userId] }
    );

    if (chats.length > 0) return chats[0];

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
        SELECT
          c.chat_id,
          c.user_id   AS customer_id,
          c.manager_id,
          p.person_name  AS customer_name,
          p.phone        AS customer_phone,
          p.avatar_url   AS customer_avatar,
          COALESCE(c.last_message,
            (SELECT COALESCE(content, message_text)
             FROM messages WHERE chat_id = c.chat_id ORDER BY created_at DESC LIMIT 1)
          ) AS last_message,
          COALESCE(c.last_message_at,
            (SELECT created_at FROM messages WHERE chat_id = c.chat_id ORDER BY created_at DESC LIMIT 1)
          ) AS last_message_time,
          (SELECT COUNT(*) FROM messages
           WHERE chat_id = c.chat_id AND sender_id != ? AND is_read = 0) AS unread_count
        FROM chats c
        JOIN person p ON c.user_id = p.person_id
        WHERE c.manager_id = ?
        ORDER BY c.updated_at DESC
      `;
    } else {
      query = `
        SELECT
          c.chat_id,
          c.user_id   AS customer_id,
          c.manager_id,
          p.person_name  AS customer_name,
          p.phone        AS customer_phone,
          p.avatar_url   AS customer_avatar,
          COALESCE(c.last_message,
            (SELECT COALESCE(content, message_text)
             FROM messages WHERE chat_id = c.chat_id ORDER BY created_at DESC LIMIT 1)
          ) AS last_message,
          COALESCE(c.last_message_at,
            (SELECT created_at FROM messages WHERE chat_id = c.chat_id ORDER BY created_at DESC LIMIT 1)
          ) AS last_message_time,
          (SELECT COUNT(*) FROM messages
           WHERE chat_id = c.chat_id AND sender_id != ? AND is_read = 0) AS unread_count
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
      `SELECT
         m.message_id,
         m.chat_id,
         m.sender_id,
         COALESCE(m.content, m.message_text) AS content,
         m.created_at AS sent_at,
         m.is_read,
         p.person_name AS sender_name
       FROM messages m
       JOIN person p ON m.sender_id = p.person_id
       WHERE m.chat_id = ?
       ORDER BY m.created_at ASC, m.message_id ASC`,
      { replacements: [chatId] }
    );

    // Mark received messages as read
    await sequelize.query(
      `UPDATE messages
       SET is_read = 1
       WHERE chat_id = ? AND sender_id != ? AND is_read = 0`,
      { replacements: [chatId, personId] }
    );

    // Cast is_read from MySQL tinyint(0/1) to JS boolean so Gson on Android parses correctly
    return messages.map(msg => ({ ...msg, is_read: msg.is_read === 1 || msg.is_read === true }));
  } catch (error) {
    throw new Error('Lỗi khi lấy tin nhắn: ' + error.message);
  }
};

/**
 * Send a message
 */
export const sendMessageService = async (chatId, senderId, messageText) => {
  try {
    const [insertResult] = await sequelize.query(
      `INSERT INTO messages (chat_id, sender_id, message_text, content, is_read)
       VALUES (?, ?, ?, ?, 0)`,
      { replacements: [chatId, senderId, messageText, messageText] }
    );

    const newMessageId = insertResult;

    await sequelize.query(
      `UPDATE chats
       SET last_message = ?, last_message_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
       WHERE chat_id = ?`,
      { replacements: [messageText, chatId] }
    );

    const [chatRows] = await sequelize.query(
      `SELECT user_id, manager_id
       FROM chats
       WHERE chat_id = ?
       LIMIT 1`,
      { replacements: [chatId] }
    );

    const chat = chatRows?.[0];
    const recipientUserId =
      Number(chat?.user_id) === Number(senderId)
        ? Number(chat?.manager_id)
        : Number(chat?.user_id);

    if (Number.isInteger(recipientUserId) && recipientUserId !== Number(senderId)) {
      await sendPushToUser({
        userId: recipientUserId,
        title: 'Tin nhắn mới',
        body: String(messageText || 'Bạn có tin nhắn mới').slice(0, 160),
        data: {
          type: 'message',
          targetType: 'conversation',
          targetId: Number(chatId),
          conversationId: Number(chatId),
          messageId: newMessageId,
        },
      }).catch((error) => {
        console.error('send legacy message push error:', error.message);
      });
    }

    return {
      message_id: newMessageId,
      chat_id: Number(chatId),
      sender_id: senderId,
      content: messageText,
      sent_at: new Date().toISOString(),
      is_read: false
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
      `SELECT person_id, person_name AS name, email
       FROM person
       WHERE role = 'manager' AND status = 'active'
       ORDER BY person_name ASC`
    );
    return managers;
  } catch (error) {
    throw new Error('Lỗi khi lấy danh sách manager: ' + error.message);
  }
};
