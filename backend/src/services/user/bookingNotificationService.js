import sequelize from "../../config/database.js";

const BOOKING_SUCCESS_TITLE = "\u0110\u1eb7t s\u00e2n th\u00e0nh c\u00f4ng";
const DEFAULT_FIELD_NAME = "S\u00e2n th\u1ec3 thao";
const BOOKING_SUCCESS_METADATA = JSON.stringify({ icon: "booking_success" });

const buildInClause = (items) => items.map(() => "?").join(", ");

export const ensureBookingSuccessNotifications = async (bookingIds) => {
  const normalizedIds = [...new Set(
    (Array.isArray(bookingIds) ? bookingIds : [])
      .map((item) => Number.parseInt(item, 10))
      .filter((item) => Number.isInteger(item) && item > 0),
  )];

  if (normalizedIds.length === 0) {
    return;
  }

  await sequelize.query(
    `INSERT IGNORE INTO notifications
      (user_id, type, section, title, subtitle, content, target_type, target_id, booking_id, field_id, is_read, metadata, created_at, updated_at)
     SELECT
       b.customer_id,
       'booking_success',
       'priority',
       CONCAT(?, ' - ', COALESCE(f.field_name, ?)),
       CONCAT('Mã đặt sân #B', b.booking_id),
       CONCAT('Sân: ', COALESCE(f.field_name, ?), '. Thời gian: ', DATE_FORMAT(b.start_time, '%Y-%m-%d %H:%i'), ' - ', DATE_FORMAT(b.end_time, '%H:%i')),
       'booking',
       b.booking_id,
       b.booking_id,
       b.field_id,
       0,
       ?,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
     FROM bookings b
     LEFT JOIN fields f ON b.field_id = f.field_id
     WHERE b.booking_id IN (${buildInClause(normalizedIds)})
       AND NOT EXISTS (
         SELECT 1
         FROM notifications n
         WHERE n.user_id = b.customer_id
           AND n.type = 'booking_success'
           AND n.booking_id = b.booking_id
       )`,
    {
      replacements: [
        BOOKING_SUCCESS_TITLE,
        DEFAULT_FIELD_NAME,
        DEFAULT_FIELD_NAME,
        BOOKING_SUCCESS_METADATA,
        ...normalizedIds,
      ],
    },
  );
};
