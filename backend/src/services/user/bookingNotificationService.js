import sequelize from "../../config/database.js";
import {
  buildBookingSlotSummary,
  listBookingSlotsByBookingIds,
  mapBookingSlotsByBookingId,
} from "../bookingSlotService.js";

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

  const [bookings] = await sequelize.query(
    `SELECT
      b.booking_id,
      b.customer_id,
      b.field_id,
      f.field_name
     FROM bookings b
     LEFT JOIN fields f ON b.field_id = f.field_id
     WHERE b.booking_id IN (${buildInClause(normalizedIds)})`,
    { replacements: normalizedIds },
  );
  const slotRows = await listBookingSlotsByBookingIds(normalizedIds);
  const slotMap = mapBookingSlotsByBookingId(slotRows);

  for (const booking of bookings) {
    const bookingId = Number.parseInt(booking.booking_id, 10);
    const slotSummary = buildBookingSlotSummary(slotMap.get(bookingId) || [], {
      fallback: booking.field_name || DEFAULT_FIELD_NAME,
    });

    await sequelize.query(
      `INSERT IGNORE INTO notifications
        (user_id, type, section, title, subtitle, content, target_type, target_id, booking_id, field_id, is_read, metadata, created_at, updated_at)
       VALUES (?, 'booking_success', 'priority', ?, ?, ?, 'booking', ?, ?, ?, 0, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)`,
      {
        replacements: [
          booking.customer_id,
          `${BOOKING_SUCCESS_TITLE} - ${booking.field_name || DEFAULT_FIELD_NAME}`,
          `Mã đặt sân #B${bookingId}`,
          `Sân: ${booking.field_name || DEFAULT_FIELD_NAME}. Khung giờ:\n${slotSummary}`,
          bookingId,
          bookingId,
          booking.field_id,
          BOOKING_SUCCESS_METADATA,
        ],
      },
    );
  }
};
