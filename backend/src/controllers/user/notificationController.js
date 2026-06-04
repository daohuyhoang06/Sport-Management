import sequelize from "../../config/database.js";

const clampNumber = (value, fallback, min, max) => {
  const parsed = Number.parseInt(value, 10);
  if (Number.isNaN(parsed)) return fallback;
  return Math.min(Math.max(parsed, min), max);
};

const mapNotificationRow = (row) => ({
  id: row.id,
  type: row.type,
  section: row.section,
  title: row.title,
  subtitle: row.subtitle,
  content: row.content,
  time: row.created_at,
  isRead: Boolean(row.is_read),
  targetType: row.target_type,
  targetId: row.target_id,
  bookingId: row.booking_id,
  fieldId: row.field_id,
  metadata: row.metadata || null,
});

const mapNotificationDetail = (row) => ({
  id: row.id,
  type: row.type,
  section: row.section,
  title: row.title,
  subtitle: row.subtitle,
  content: row.content,
  time: row.created_at,
  isRead: Boolean(row.is_read),
  targetType: row.target_type,
  targetId: row.target_id,
  bookingId: row.booking_id,
  fieldId: row.field_id,
  metadata: row.metadata || null,
});

// GET /api/user/notifications
export const listNotifications = async (req, res) => {
  try {
    const userId = req.user?.id;
    const { section, type, page, limit } = req.query;

    if (!userId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    const safePage = clampNumber(page, 1, 1, 1000000);
    const safeLimit = clampNumber(limit, 20, 1, 100);
    const offset = (safePage - 1) * safeLimit;

    const whereClauses = ["n.user_id = ?"];
    const replacements = [userId];

    if (section) {
      whereClauses.push("n.section = ?");
      replacements.push(section);
    }

    if (type) {
      whereClauses.push("n.type = ?");
      replacements.push(type);
    }

    const whereSql = whereClauses.join(" AND ");

    const [countRows] = await sequelize.query(
      `SELECT COUNT(*) AS total
       FROM notifications n
       LEFT JOIN bookings b ON n.booking_id = b.booking_id
       WHERE ${whereSql}
        AND (
          n.type <> 'booking_success'
          OR (
            n.booking_id IS NOT NULL
            AND b.booking_id IS NOT NULL
            AND b.customer_id = n.user_id
            AND b.status IN ('confirmed', 'completed')
          )
        )`,
      { replacements },
    );
    const total = Number(countRows?.[0]?.total || 0);

    const [rows] = await sequelize.query(
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
        n.updated_at
      FROM notifications n
      LEFT JOIN bookings b ON n.booking_id = b.booking_id
      WHERE ${whereSql}
        AND (
          n.type <> 'booking_success'
          OR (
            n.booking_id IS NOT NULL
            AND b.booking_id IS NOT NULL
            AND b.customer_id = n.user_id
            AND b.status IN ('confirmed', 'completed')
          )
        )
      ORDER BY n.created_at DESC
      LIMIT ? OFFSET ?`,
      { replacements: [...replacements, safeLimit, offset] },
    );

    res.json({
      success: true,
      data: {
        items: rows.map(mapNotificationRow),
        page: safePage,
        limit: safeLimit,
        total,
      },
    });
  } catch (error) {
    console.error("listNotifications error:", error);
    res.status(500).json({
      success: false,
      message: "Loi server khi lay danh sach thong bao",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

// GET /api/user/notifications/:id
export const getNotificationDetail = async (req, res) => {
  try {
    const userId = req.user?.id;
    const { id } = req.params;

    if (!userId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    if (!id) {
      return res.status(400).json({
        success: false,
        message: "Notification ID is required",
      });
    }

    const [rows] = await sequelize.query(
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
        n.updated_at
      FROM notifications n
      LEFT JOIN bookings b ON n.booking_id = b.booking_id
      WHERE n.id = ? AND n.user_id = ?
        AND (
          n.type <> 'booking_success'
          OR (
            n.booking_id IS NOT NULL
            AND b.booking_id IS NOT NULL
            AND b.customer_id = n.user_id
            AND b.status IN ('confirmed', 'completed')
          )
        )
      LIMIT 1`,
      { replacements: [id, userId] },
    );

    const notification = rows?.[0];
    if (!notification) {
      return res.status(404).json({
        success: false,
        message: "Notification not found",
      });
    }

    res.json({
      success: true,
      data: mapNotificationDetail(notification),
    });
  } catch (error) {
    console.error("getNotificationDetail error:", error);
    res.status(500).json({
      success: false,
      message: "Loi server khi lay chi tiet thong bao",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

// PATCH /api/user/notifications/:id/read
export const markNotificationRead = async (req, res) => {
  try {
    const userId = req.user?.id;
    const { id } = req.params;

    if (!userId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    if (!id) {
      return res.status(400).json({
        success: false,
        message: "Notification ID is required",
      });
    }

    const [rows] = await sequelize.query(
      `SELECT id, is_read
       FROM notifications
       WHERE id = ? AND user_id = ?
       LIMIT 1`,
      { replacements: [id, userId] },
    );

    const notification = rows?.[0];
    if (!notification) {
      return res.status(404).json({
        success: false,
        message: "Notification not found",
      });
    }

    if (!notification.is_read) {
      await sequelize.query(
        `UPDATE notifications
         SET is_read = 1, updated_at = CURRENT_TIMESTAMP
         WHERE id = ? AND user_id = ?`,
        { replacements: [id, userId] },
      );
    }

    res.json({
      success: true,
      data: {
        id: Number(id),
        isRead: true,
      },
    });
  } catch (error) {
    console.error("markNotificationRead error:", error);
    res.status(500).json({
      success: false,
      message: "Loi server khi cap nhat thong bao",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};

// PATCH /api/user/notifications/read-all
export const markAllNotificationsRead = async (req, res) => {
  try {
    const userId = req.user?.id;

    if (!userId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    await sequelize.query(
      `UPDATE notifications
       SET is_read = 1, updated_at = CURRENT_TIMESTAMP
       WHERE user_id = ? AND is_read = 0`,
      { replacements: [userId] },
    );

    res.json({
      success: true,
      data: {
        isRead: true,
      },
    });
  } catch (error) {
    console.error("markAllNotificationsRead error:", error);
    res.status(500).json({
      success: false,
      message: "Loi server khi cap nhat thong bao",
      error: process.env.NODE_ENV === "development" ? error.message : undefined,
    });
  }
};
