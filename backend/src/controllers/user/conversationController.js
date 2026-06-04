import sequelize from "../../config/database.js";

const getFieldById = async (fieldId) => {
  const [rows] = await sequelize.query(
    "SELECT field_id, manager_id FROM fields WHERE field_id = ? LIMIT 1",
    { replacements: [fieldId] },
  );
  return rows?.[0] || null;
};

const getBookingById = async (bookingId, userId) => {
  const [rows] = await sequelize.query(
    "SELECT booking_id, field_id FROM bookings WHERE booking_id = ? AND customer_id = ? LIMIT 1",
    { replacements: [bookingId, userId] },
  );
  return rows?.[0] || null;
};

const findConversation = async ({ userId, managerId, fieldId, bookingId }) => {
  const [rows] = await sequelize.query(
    `SELECT chat_id, user_id, manager_id, field_id, booking_id
     FROM chats
     WHERE user_id = ?
       AND manager_id = ?
       AND field_id <=> ?
       AND booking_id <=> ?
     LIMIT 1`,
    { replacements: [userId, managerId, fieldId ?? null, bookingId ?? null] },
  );
  return rows?.[0] || null;
};

// POST /api/user/conversations
export const createConversation = async (req, res) => {
  try {
    const userId = req.user?.id;
    const { fieldId, bookingId } = req.body || {};

    if (!userId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    if (!fieldId && !bookingId) {
      return res.status(400).json({
        success: false,
        message: "fieldId or bookingId is required",
      });
    }

    let resolvedFieldId = fieldId ? Number(fieldId) : null;
    let resolvedBookingId = bookingId ? Number(bookingId) : null;

    if (resolvedBookingId) {
      const booking = await getBookingById(resolvedBookingId, userId);
      if (!booking) {
        return res.status(404).json({
          success: false,
          message: "Booking not found",
        });
      }
      if (resolvedFieldId && Number(booking.field_id) !== resolvedFieldId) {
        return res.status(400).json({
          success: false,
          message: "fieldId does not match booking",
        });
      }
      resolvedFieldId = Number(booking.field_id);
    }

    if (!resolvedFieldId) {
      return res.status(400).json({
        success: false,
        message: "fieldId is required",
      });
    }

    const field = await getFieldById(resolvedFieldId);
    if (!field || !field.manager_id) {
      return res.status(404).json({
        success: false,
        message: "Field owner not found",
      });
    }

    const managerId = Number(field.manager_id);

    const existing = await findConversation({
      userId,
      managerId,
      fieldId: resolvedFieldId,
      bookingId: resolvedBookingId,
    });

    if (existing) {
      return res.json({
        success: true,
        data: {
          conversationId: existing.chat_id,
          fieldId: existing.field_id,
          bookingId: existing.booking_id,
        },
      });
    }

    const [result] = await sequelize.query(
      `INSERT INTO chats
        (user_id, manager_id, field_id, booking_id, created_at, updated_at)
       VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)`,
      { replacements: [userId, managerId, resolvedFieldId, resolvedBookingId] },
    );

    const conversationId = result?.insertId || result;

    return res.json({
      success: true,
      data: {
        conversationId,
        fieldId: resolvedFieldId,
        bookingId: resolvedBookingId,
      },
    });
  } catch (error) {
    console.error("createConversation error:", error);
    return res.status(500).json({
      success: false,
      message: "Loi server khi tao cuoc tro chuyen",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

// GET /api/user/conversations
export const listConversations = async (req, res) => {
  try {
    const userId = req.user?.id;

    if (!userId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    const [rows] = await sequelize.query(
      `SELECT
        c.chat_id,
        c.field_id,
        c.booking_id,
        c.last_message,
        c.last_message_at,
        c.updated_at,
        f.field_name,
        f.avatar_image_url,
        f.card_image_url,
        f.phone AS field_phone,
        m.person_name AS owner_name,
        m.phone AS owner_phone,
        (
          SELECT message_text
          FROM messages
          WHERE chat_id = c.chat_id
          ORDER BY created_at DESC
          LIMIT 1
        ) AS last_message_text,
        (
          SELECT created_at
          FROM messages
          WHERE chat_id = c.chat_id
          ORDER BY created_at DESC
          LIMIT 1
        ) AS last_message_time,
        (
          SELECT COUNT(*)
          FROM messages
          WHERE chat_id = c.chat_id
            AND sender_id != ?
            AND is_read = 0
        ) AS unread_count
      FROM chats c
      LEFT JOIN fields f ON c.field_id = f.field_id
      LEFT JOIN person m ON c.manager_id = m.person_id
      WHERE c.user_id = ?
      ORDER BY COALESCE(c.last_message_at, c.updated_at) DESC`,
      { replacements: [userId, userId] },
    );

    const items = rows.map((row) => ({
      conversationId: row.chat_id,
      fieldId: row.field_id,
      fieldName: row.field_name || null,
      fieldAvatar: row.card_image_url || row.avatar_image_url || null,
      ownerName: row.owner_name || null,
      ownerPhone: row.field_phone || row.owner_phone || null,
      isOnline: false,
      lastMessage: row.last_message || row.last_message_text || null,
      lastMessageTime:
        row.last_message_at || row.last_message_time || row.updated_at,
      unreadCount: Number(row.unread_count || 0),
      updatedAt: row.updated_at,
    }));

    return res.json({
      success: true,
      data: items,
    });
  } catch (error) {
    console.error("listConversations error:", error);
    return res.status(500).json({
      success: false,
      message: "Loi server khi lay danh sach cuoc tro chuyen",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

// GET /api/user/conversations/:conversationId/messages
export const getConversationMessages = async (req, res) => {
  try {
    const userId = req.user?.id;
    const { conversationId } = req.params;

    if (!userId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    if (!conversationId) {
      return res.status(400).json({
        success: false,
        message: "Conversation ID is required",
      });
    }

    const [conversations] = await sequelize.query(
      `SELECT
        c.chat_id,
        c.field_id,
        c.booking_id,
        f.field_name,
        f.avatar_image_url,
        f.card_image_url,
        f.phone AS field_phone,
        m.person_name AS owner_name,
        m.phone AS owner_phone
      FROM chats c
      LEFT JOIN fields f ON c.field_id = f.field_id
      LEFT JOIN person m ON c.manager_id = m.person_id
      WHERE c.chat_id = ? AND c.user_id = ?
      LIMIT 1`,
      { replacements: [conversationId, userId] },
    );

    const conversation = conversations?.[0];
    if (!conversation) {
      return res.status(404).json({
        success: false,
        message: "Conversation not found",
      });
    }

    const [messages] = await sequelize.query(
      `SELECT
        m.message_id,
        m.sender_id,
        m.sender_type,
        m.message_type,
        m.message_text,
        m.content,
        m.image_url,
        m.metadata,
        m.created_at
      FROM messages m
      WHERE m.chat_id = ?
      ORDER BY m.created_at ASC`,
      { replacements: [conversationId] },
    );

    await sequelize.query(
      `UPDATE messages
       SET is_read = 1, updated_at = CURRENT_TIMESTAMP
       WHERE chat_id = ? AND sender_id != ? AND is_read = 0`,
      { replacements: [conversationId, userId] },
    );

    const fieldAvatar =
      conversation.card_image_url || conversation.avatar_image_url || null;
    const ownerPhone =
      conversation.field_phone || conversation.owner_phone || null;

    res.json({
      success: true,
      data: {
        conversation: {
          conversationId: conversation.chat_id,
          fieldName: conversation.field_name || null,
          fieldAvatar,
          ownerName: conversation.owner_name || null,
          ownerPhone,
          isOnline: false,
        },
        messages: messages.map((row) => ({
          messageId: row.message_id,
          senderId: row.sender_id,
          senderType: row.sender_type || "user",
          messageType: row.message_type || "text",
          content: row.content || row.message_text || "",
          imageUrl: row.image_url || null,
          metadata: row.metadata || null,
          createdAt: row.created_at,
          isMine: row.sender_id === userId,
        })),
      },
    });
  } catch (error) {
    console.error("getConversationMessages error:", error);
    res.status(500).json({
      success: false,
      message: "Loi server khi lay tin nhan",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

// POST /api/user/conversations/:conversationId/messages
export const sendConversationMessage = async (req, res) => {
  try {
    const userId = req.user?.id;
    const { conversationId } = req.params;
    const { messageType, content, imageUrl, metadata } = req.body || {};

    if (!userId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    if (!conversationId) {
      return res.status(400).json({
        success: false,
        message: "Conversation ID is required",
      });
    }

    const trimmedContent = typeof content === "string" ? content.trim() : "";
    const resolvedType = (messageType || "text").toLowerCase();

    if (!trimmedContent && !imageUrl) {
      return res.status(400).json({
        success: false,
        message: "Message content is required",
      });
    }

    const [conversations] = await sequelize.query(
      "SELECT chat_id FROM chats WHERE chat_id = ? AND user_id = ? LIMIT 1",
      { replacements: [conversationId, userId] },
    );

    const conversation = conversations?.[0];
    if (!conversation) {
      return res.status(404).json({
        success: false,
        message: "Conversation not found",
      });
    }

    const [insertResult] = await sequelize.query(
      `INSERT INTO messages
        (chat_id, sender_id, sender_type, message_type, message_text, content, image_url, metadata, is_read, created_at, updated_at)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)`,
      {
        replacements: [
          conversationId,
          userId,
          "user",
          resolvedType,
          trimmedContent || null,
          trimmedContent || null,
          imageUrl || null,
          metadata ? JSON.stringify(metadata) : null,
        ],
      },
    );

    await sequelize.query(
      `UPDATE chats
       SET last_message = ?, last_message_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP,
           owner_unread_count = owner_unread_count + 1
       WHERE chat_id = ?`,
      { replacements: [trimmedContent || "", conversationId] },
    );

    const messageId = insertResult?.insertId || insertResult;

    return res.json({
      success: true,
      data: {
        messageId,
        senderId: userId,
        senderType: "user",
        messageType: resolvedType,
        content: trimmedContent,
        imageUrl: imageUrl || null,
        metadata: metadata || null,
        createdAt: new Date().toISOString(),
        isMine: true,
      },
    });
  } catch (error) {
    console.error("sendConversationMessage error:", error);
    return res.status(500).json({
      success: false,
      message: "Loi server khi gui tin nhan",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

// PATCH /api/user/conversations/:conversationId/read
export const markConversationRead = async (req, res) => {
  try {
    const userId = req.user?.id;
    const { conversationId } = req.params;

    if (!userId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    if (!conversationId) {
      return res.status(400).json({
        success: false,
        message: "Conversation ID is required",
      });
    }

    const [conversations] = await sequelize.query(
      "SELECT chat_id FROM chats WHERE chat_id = ? AND user_id = ? LIMIT 1",
      { replacements: [conversationId, userId] },
    );

    if (!conversations?.[0]) {
      return res.status(404).json({
        success: false,
        message: "Conversation not found",
      });
    }

    await sequelize.query(
      `UPDATE messages
       SET is_read = 1, updated_at = CURRENT_TIMESTAMP
       WHERE chat_id = ? AND sender_id != ? AND is_read = 0`,
      { replacements: [conversationId, userId] },
    );

    await sequelize.query(
      `UPDATE chats
       SET user_unread_count = 0, updated_at = CURRENT_TIMESTAMP
       WHERE chat_id = ?`,
      { replacements: [conversationId] },
    );

    return res.json({
      success: true,
      data: {
        conversationId: Number(conversationId),
        unreadCount: 0,
      },
    });
  } catch (error) {
    console.error("markConversationRead error:", error);
    return res.status(500).json({
      success: false,
      message: "Loi server khi cap nhat trang thai doc",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};
