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
        m.person_name AS owner_name,
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
