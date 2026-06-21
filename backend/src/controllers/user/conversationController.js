import sequelize from "../../config/database.js";
import { sendPushToUser } from "../../services/user/pushNotificationService.js";

const getFieldById = async (fieldId) => {
  const [rows] = await sequelize.query(
    "SELECT field_id, manager_id FROM fields WHERE field_id = ? LIMIT 1",
    { replacements: [fieldId] },
  );
  return rows?.[0] || null;
};

const getOwnedBookingById = async (bookingId, userId) => {
  const [rows] = await sequelize.query(
    "SELECT booking_id, field_id FROM bookings WHERE booking_id = ? AND customer_id = ? LIMIT 1",
    { replacements: [bookingId, userId] },
  );
  return rows?.[0] || null;
};

const getMatchedPeerContext = async ({ bookingId, currentUserId, peerUserId }) => {
  const [rows] = await sequelize.query(
    `SELECT
      mp.booking_id,
      mp.owner_user_id,
      mp.matched_user_id
     FROM match_posts mp
     WHERE mp.booking_id = ?
       AND mp.status = 'MATCHED'
       AND mp.matched_user_id IS NOT NULL
       AND (
         (mp.owner_user_id = ? AND mp.matched_user_id = ?)
         OR (mp.owner_user_id = ? AND mp.matched_user_id = ?)
       )
     LIMIT 1`,
    {
      replacements: [
        bookingId,
        currentUserId,
        peerUserId,
        peerUserId,
        currentUserId,
      ],
    },
  );
  return rows?.[0] || null;
};

const findAnyOwnerConversation = async ({ userId, managerId }) => {
  const [rows] = await sequelize.query(
    `SELECT chat_id, user_id, manager_id, field_id, booking_id
     FROM chats
     WHERE user_id = ?
        AND manager_id = ?
        AND field_id IS NOT NULL
      ORDER BY COALESCE(last_message_at, updated_at, created_at) DESC, chat_id DESC
      LIMIT 1`,
    { replacements: [userId, managerId] },
  );
  return rows?.[0] || null;
};

const findOwnerConversationChatIds = async ({ userId, managerId }) => {
  const [rows] = await sequelize.query(
    `SELECT chat_id
     FROM chats
     WHERE user_id = ?
       AND manager_id = ?
       AND field_id IS NOT NULL
     ORDER BY created_at ASC, chat_id ASC`,
     {
       replacements: [userId, managerId],
     },
   );
  return rows.map((row) => row.chat_id);
};

const findAnyPeerConversationBetweenUsers = async ({ participantA, participantB }) => {
  const [rows] = await sequelize.query(
    `SELECT chat_id, user_id, manager_id, field_id, booking_id
     FROM chats
     WHERE field_id IS NULL
       AND booking_id IS NOT NULL
       AND (
         (user_id = ? AND manager_id = ?)
         OR (user_id = ? AND manager_id = ?)
       )
     ORDER BY COALESCE(last_message_at, updated_at, created_at) DESC, chat_id DESC
     LIMIT 1`,
    {
      replacements: [
        participantA,
        participantB,
        participantB,
        participantA,
      ],
    },
  );
  return rows?.[0] || null;
};

const mapConversationRowForList = (row) => {
  const title = (row.display_title || "").trim();
  const ownerName = (row.counterpart_username || row.counterpart_name || "").trim();
  const phone =
    row.is_peer_chat
      ? row.counterpart_phone || null
      : row.field_phone || row.counterpart_phone || null;

  return {
    conversationId: row.chat_id,
    fieldId: row.field_id,
    fieldName: title || ownerName || null,
    fieldAvatar: row.card_image_url || row.avatar_image_url || null,
    ownerName: ownerName || null,
    ownerPhone: phone,
    isOnline: false,
    lastMessage: row.last_message || row.last_message_text || null,
    lastMessageTime:
      row.last_message_at || row.last_message_time || row.updated_at,
    unreadCount: Number(row.unread_count || 0),
    updatedAt: row.updated_at,
  };
};

// POST /api/user/conversations
export const createConversation = async (req, res) => {
  try {
    const userId = req.user?.id;
    const { fieldId, bookingId, peerUserId } = req.body || {};

    if (!userId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    const resolvedPeerUserId = peerUserId ? Number(peerUserId) : null;
    let resolvedFieldId = fieldId ? Number(fieldId) : null;
    let resolvedBookingId = bookingId ? Number(bookingId) : null;

    if (resolvedPeerUserId) {
      if (!resolvedBookingId) {
        return res.status(400).json({
          success: false,
          message: "bookingId is required for peer conversation",
        });
      }

      const matchedContext = await getMatchedPeerContext({
        bookingId: resolvedBookingId,
        currentUserId: Number(userId),
        peerUserId: resolvedPeerUserId,
      });

      if (!matchedContext) {
        return res.status(403).json({
          success: false,
          message: "You cannot create a peer conversation for this match",
        });
      }

      const existingPeerConversation = await findAnyPeerConversationBetweenUsers({
        participantA: Number(userId),
        participantB: resolvedPeerUserId,
      });

      if (existingPeerConversation) {
        return res.json({
          success: true,
          data: {
            conversationId: existingPeerConversation.chat_id,
            fieldId: existingPeerConversation.field_id,
            bookingId: existingPeerConversation.booking_id,
          },
        });
      }

      const [result] = await sequelize.query(
        `INSERT INTO chats
          (user_id, manager_id, field_id, booking_id, created_at, updated_at)
         VALUES (?, ?, NULL, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)`,
        {
          replacements: [Number(userId), resolvedPeerUserId, resolvedBookingId],
        },
      );

      const conversationId = result?.insertId || result;

      return res.json({
        success: true,
        data: {
          conversationId,
          fieldId: null,
          bookingId: resolvedBookingId,
        },
      });
    }

    if (!fieldId && !bookingId) {
      return res.status(400).json({
        success: false,
        message: "fieldId or bookingId is required",
      });
    }

    if (resolvedBookingId) {
      const booking = await getOwnedBookingById(resolvedBookingId, userId);
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

    const existingOwnerConversation = await findAnyOwnerConversation({
      userId: Number(userId),
      managerId,
    });

    if (existingOwnerConversation) {
      return res.json({
        success: true,
        data: {
          conversationId: existingOwnerConversation.chat_id,
          fieldId: existingOwnerConversation.field_id,
          bookingId: existingOwnerConversation.booking_id,
        },
      });
    }

    const [result] = await sequelize.query(
      `INSERT INTO chats
        (user_id, manager_id, field_id, booking_id, created_at, updated_at)
       VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)`,
      { replacements: [Number(userId), managerId, resolvedFieldId, resolvedBookingId] },
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
        cp.person_name AS counterpart_name,
        cp.username AS counterpart_username,
        cp.phone AS counterpart_phone,
        (c.field_id IS NULL AND c.booking_id IS NOT NULL) AS is_peer_chat,
        CASE
          WHEN c.field_id IS NULL AND c.booking_id IS NOT NULL THEN COALESCE(cp.username, cp.person_name)
          ELSE COALESCE(cp.username, cp.person_name, f.field_name)
        END AS display_title,
        (
          SELECT message_text
          FROM messages m2
          INNER JOIN chats c3 ON m2.chat_id = c3.chat_id
          WHERE (
              c.field_id IS NULL
              AND c3.field_id IS NULL
              AND c3.booking_id <=> c.booking_id
              AND (
                (c3.user_id = c.user_id AND c3.manager_id = c.manager_id)
                OR (c3.user_id = c.manager_id AND c3.manager_id = c.user_id)
              )
            ) OR (
              c.field_id IS NOT NULL
              AND c3.field_id IS NOT NULL
              AND c3.user_id = c.user_id
              AND c3.manager_id = c.manager_id
            )
          ORDER BY m2.created_at DESC
          LIMIT 1
        ) AS last_message_text,
        (
          SELECT m2.created_at
          FROM messages m2
          INNER JOIN chats c3 ON m2.chat_id = c3.chat_id
          WHERE (
              c.field_id IS NULL
              AND c3.field_id IS NULL
              AND c3.booking_id <=> c.booking_id
              AND (
                (c3.user_id = c.user_id AND c3.manager_id = c.manager_id)
                OR (c3.user_id = c.manager_id AND c3.manager_id = c.user_id)
              )
            ) OR (
              c.field_id IS NOT NULL
              AND c3.field_id IS NOT NULL
              AND c3.user_id = c.user_id
              AND c3.manager_id = c.manager_id
            )
          ORDER BY m2.created_at DESC
          LIMIT 1
        ) AS last_message_time,
        (
          SELECT COUNT(*)
          FROM messages m2
          INNER JOIN chats c3 ON m2.chat_id = c3.chat_id
          WHERE (
            (
                c.field_id IS NULL
                AND c3.field_id IS NULL
                AND c3.booking_id <=> c.booking_id
                AND (
                  (c3.user_id = c.user_id AND c3.manager_id = c.manager_id)
                  OR (c3.user_id = c.manager_id AND c3.manager_id = c.user_id)
                )
              ) OR (
                c.field_id IS NOT NULL
                AND c3.field_id IS NOT NULL
                AND c3.user_id = c.user_id
                AND c3.manager_id = c.manager_id
              )
            )
            AND m2.sender_id != ?
            AND m2.is_read = 0
        ) AS unread_count
      FROM chats c
      LEFT JOIN fields f ON c.field_id = f.field_id
      LEFT JOIN person cp
        ON cp.person_id = CASE
          WHEN c.user_id = ? THEN c.manager_id
          ELSE c.user_id
        END
      WHERE ? IN (c.user_id, c.manager_id)
        AND c.chat_id = (
          SELECT c2.chat_id
          FROM chats c2
          WHERE ? IN (c2.user_id, c2.manager_id)
             AND (
               (c2.user_id = c.user_id AND c2.manager_id = c.manager_id)
               OR (c2.user_id = c.manager_id AND c2.manager_id = c.user_id)
             )
            AND (
              (
                c.field_id IS NULL
                AND c2.field_id IS NULL
                AND c2.booking_id <=> c.booking_id
              ) OR (
                c.field_id IS NOT NULL
                AND c2.field_id IS NOT NULL
                AND c2.user_id = c.user_id
                AND c2.manager_id = c.manager_id
              )
            )
          ORDER BY COALESCE(c2.last_message_at, c2.updated_at, c2.created_at) DESC, c2.chat_id DESC
          LIMIT 1
        )
      ORDER BY COALESCE(c.last_message_at, c.updated_at) DESC`,
      {
        replacements: [
          Number(userId),
          Number(userId),
          Number(userId),
          Number(userId),
        ],
      },
    );

    return res.json({
      success: true,
      data: rows.map(mapConversationRowForList),
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
        c.user_id,
        c.manager_id,
        c.field_id,
        c.booking_id,
        f.field_name,
        f.avatar_image_url,
        f.card_image_url,
        f.phone AS field_phone,
        cp.person_name AS counterpart_name,
        cp.username AS counterpart_username,
        cp.phone AS counterpart_phone,
        (c.field_id IS NULL AND c.booking_id IS NOT NULL) AS is_peer_chat
      FROM chats c
      LEFT JOIN fields f ON c.field_id = f.field_id
      LEFT JOIN person cp
        ON cp.person_id = CASE
          WHEN c.user_id = ? THEN c.manager_id
          ELSE c.user_id
        END
      WHERE c.chat_id = ?
        AND ? IN (c.user_id, c.manager_id)
      LIMIT 1`,
      {
        replacements: [Number(userId), conversationId, Number(userId)],
      },
    );

    const conversation = conversations?.[0];
    if (!conversation) {
      return res.status(404).json({
        success: false,
        message: "Conversation not found",
      });
    }

    let chatIdsToFetch = [Number(conversationId)];
    if (conversation.field_id === null) {
      const [peerChats] = await sequelize.query(
        `SELECT chat_id 
         FROM chats 
         WHERE field_id IS NULL 
           AND (
             (user_id = ? AND manager_id = ?)
             OR (user_id = ? AND manager_id = ?)
           )`,
        {
          replacements: [
            conversation.user_id,
            conversation.manager_id,
            conversation.manager_id,
            conversation.user_id,
          ],
        }
      );
      if (peerChats && peerChats.length > 0) {
        chatIdsToFetch = peerChats.map(r => r.chat_id);
      }
    } else {
      const ownerChatIds = await findOwnerConversationChatIds({
        userId: conversation.user_id,
        managerId: conversation.manager_id,
      });
      if (ownerChatIds.length > 0) {
        chatIdsToFetch = ownerChatIds;
      }
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
      WHERE m.chat_id IN (?)
      ORDER BY m.created_at ASC`,
      { replacements: [chatIdsToFetch] },
    );

    await sequelize.query(
      `UPDATE messages
       SET is_read = 1, updated_at = CURRENT_TIMESTAMP
       WHERE chat_id IN (?) AND sender_id != ? AND is_read = 0`,
      { replacements: [chatIdsToFetch, Number(userId)] },
    );

    const title = conversation.is_peer_chat
      ? conversation.counterpart_username || conversation.counterpart_name || "Hội thoại"
      : conversation.field_name || conversation.counterpart_name || "Hội thoại";
    const phone = conversation.is_peer_chat
      ? conversation.counterpart_phone || null
      : conversation.field_phone || conversation.counterpart_phone || null;

    return res.json({
      success: true,
      data: {
        conversation: {
          conversationId: conversation.chat_id,
          fieldName: title,
          fieldAvatar:
            conversation.card_image_url || conversation.avatar_image_url || null,
          ownerName: conversation.counterpart_username || conversation.counterpart_name || null,
          ownerPhone: phone,
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
          isMine: Number(row.sender_id) === Number(userId),
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
      `SELECT chat_id, user_id, manager_id
       FROM chats
       WHERE chat_id = ?
         AND ? IN (user_id, manager_id)
       LIMIT 1`,
      { replacements: [conversationId, Number(userId)] },
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
          Number(userId),
          "user",
          resolvedType,
          trimmedContent || null,
          trimmedContent || null,
          imageUrl || null,
          metadata ? JSON.stringify(metadata) : null,
        ],
      },
    );

    const isPrimaryParticipant = Number(conversation.user_id) === Number(userId);

    await sequelize.query(
      `UPDATE chats
       SET last_message = ?, last_message_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP,
           user_unread_count = user_unread_count + ?,
           owner_unread_count = owner_unread_count + ?
       WHERE chat_id = ?`,
      {
        replacements: [
          trimmedContent || "",
          isPrimaryParticipant ? 0 : 1,
          isPrimaryParticipant ? 1 : 0,
          conversationId,
        ],
      },
    );

    const messageId = insertResult?.insertId || insertResult;
    const recipientUserId = isPrimaryParticipant
      ? Number(conversation.manager_id)
      : Number(conversation.user_id);

    if (Number.isInteger(recipientUserId) && recipientUserId !== Number(userId)) {
      const pushBody = trimmedContent || (imageUrl ? "Bạn nhận được hình ảnh mới" : "Bạn có tin nhắn mới");
      await sendPushToUser({
        userId: recipientUserId,
        title: "Tin nhắn mới",
        body: pushBody.slice(0, 160),
        data: {
          type: "message",
          targetType: "conversation",
          targetId: Number(conversationId),
          conversationId: Number(conversationId),
          messageId,
        },
      }).catch((error) => {
        console.error("send message push error:", error.message);
      });
    }

    return res.json({
      success: true,
      data: {
        messageId,
        senderId: Number(userId),
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
      `SELECT chat_id, user_id, manager_id, field_id
       FROM chats
       WHERE chat_id = ?
         AND ? IN (user_id, manager_id)
       LIMIT 1`,
      { replacements: [conversationId, Number(userId)] },
    );

    const conversation = conversations?.[0];
    if (!conversation) {
      return res.status(404).json({
        success: false,
        message: "Conversation not found",
      });
    }

    let chatIdsToUpdate = [Number(conversationId)];
    if (conversation.field_id === null) {
      const [peerChats] = await sequelize.query(
        `SELECT chat_id 
         FROM chats 
         WHERE field_id IS NULL 
           AND (
             (user_id = ? AND manager_id = ?)
             OR (user_id = ? AND manager_id = ?)
           )`,
        {
          replacements: [
            conversation.user_id,
            conversation.manager_id,
            conversation.manager_id,
            conversation.user_id,
          ],
        }
      );
      if (peerChats && peerChats.length > 0) {
        chatIdsToUpdate = peerChats.map(r => r.chat_id);
      }
    } else {
      const ownerChatIds = await findOwnerConversationChatIds({
        userId: conversation.user_id,
        managerId: conversation.manager_id,
      });
      if (ownerChatIds.length > 0) {
        chatIdsToUpdate = ownerChatIds;
      }
    }

    await sequelize.query(
      `UPDATE messages
       SET is_read = 1, updated_at = CURRENT_TIMESTAMP
       WHERE chat_id IN (?) AND sender_id != ? AND is_read = 0`,
      { replacements: [chatIdsToUpdate, Number(userId)] },
    );

    const unreadColumn =
      Number(conversation.user_id) === Number(userId)
        ? "user_unread_count"
        : "owner_unread_count";

    await sequelize.query(
      `UPDATE chats
       SET ${unreadColumn} = 0, updated_at = CURRENT_TIMESTAMP
       WHERE chat_id IN (?)`,
      { replacements: [chatIdsToUpdate] },
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
