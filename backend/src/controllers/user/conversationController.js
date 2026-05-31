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
