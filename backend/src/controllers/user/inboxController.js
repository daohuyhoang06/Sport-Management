import sequelize from "../../config/database.js";

const clampNumber = (value, fallback, min, max) => {
  const parsed = Number.parseInt(value, 10);
  if (Number.isNaN(parsed)) return fallback;
  return Math.min(Math.max(parsed, min), max);
};

const normalizeSection = (value) => {
  if (!value) return null;
  const normalized = String(value).trim().toLowerCase();
  if (
    normalized === "priority" ||
    normalized === "messages" ||
    normalized === "activity"
  ) {
    return normalized;
  }
  return null;
};

const resolveNotificationSection = (row) => {
  const explicit = normalizeSection(row.section);
  if (explicit) return explicit;
  if (row.type === "booking_success" || row.type === "upcoming_match") {
    return "priority";
  }
  return "activity";
};

const resolveFieldAvatar = (row) =>
  row.card_image_url || row.avatar_image_url || null;

const mapNotificationItem = (row) => ({
  id: row.id,
  type: row.type,
  title: row.title,
  subtitle: row.subtitle,
  detail: row.content || row.subtitle || "",
  time: row.created_at,
  isRead: Boolean(row.is_read),
  icon: row.metadata?.icon || null,
  avatar: resolveFieldAvatar(row),
  section: resolveNotificationSection(row),
  targetType: row.target_type,
  targetId: row.target_id,
  bookingId: row.booking_id,
  conversationId: null,
  fieldId: row.field_id,
});

const mapConversationItem = (row) => ({
  id: row.chat_id,
  type: "message",
  title: row.field_name || row.owner_name || "",
  subtitle: row.last_message || row.last_message_text || "",
  detail: row.last_message || row.last_message_text || "",
  time: row.last_message_at || row.last_message_time || row.updated_at,
  isRead: Number(row.unread_count || 0) === 0,
  icon: null,
  avatar: resolveFieldAvatar(row),
  section: "messages",
  targetType: "conversation",
  targetId: row.chat_id,
  bookingId: row.booking_id,
  conversationId: row.chat_id,
  fieldId: row.field_id,
});

// GET /api/user/inbox
export const getInbox = async (req, res) => {
  try {
    const userId = req.user?.id;
    const { page, limit } = req.query;

    if (!userId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    const safePage = clampNumber(page, 1, 1, 1000000);
    const safeLimit = clampNumber(limit, 50, 1, 100);
    const offset = (safePage - 1) * safeLimit;

    const [notificationRows] = await sequelize.query(
      `SELECT
        n.id,
        n.type,
        n.section,
        n.title,
        n.subtitle,
        n.content,
        n.target_type,
        n.target_id,
        n.booking_id,
        n.field_id,
        n.is_read,
        n.metadata,
        n.created_at,
        f.avatar_image_url,
        f.card_image_url
      FROM notifications n
      LEFT JOIN fields f ON n.field_id = f.field_id
      WHERE n.user_id = ?
      ORDER BY n.created_at DESC
      LIMIT ? OFFSET ?`,
      { replacements: [userId, safeLimit, offset] },
    );

    const [conversationRows] = await sequelize.query(
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
      ORDER BY COALESCE(c.last_message_at, c.updated_at) DESC
      LIMIT ? OFFSET ?`,
      { replacements: [userId, userId, safeLimit, offset] },
    );

    const sections = {
      priority: [],
      messages: [],
      activity: [],
    };

    notificationRows.forEach((row) => {
      const item = mapNotificationItem(row);
      sections[item.section].push(item);
    });

    conversationRows.forEach((row) => {
      sections.messages.push(mapConversationItem(row));
    });

    res.json({
      success: true,
      data: {
        page: safePage,
        limit: safeLimit,
        sections: [
          { section: "priority", items: sections.priority },
          { section: "messages", items: sections.messages },
          { section: "activity", items: sections.activity },
        ],
      },
    });
  } catch (error) {
    console.error("getInbox error:", error);
    res.status(500).json({
      success: false,
      message: "Loi server khi lay hop thu",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};
