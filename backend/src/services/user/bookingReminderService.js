import sequelize from "../../config/database.js";
import {
  buildBookingSlotSummary,
  formatSlotTimeLabel,
  listBookingSlotsByBookingIds,
  mapBookingSlotsByBookingId,
} from "../bookingSlotService.js";

const REMINDER_TYPE = "booking_reminder";
const URGENT_REMINDER_TYPE = "booking_reminder_urgent";
const REMINDER_TITLE = "Sắp đến giờ thi đấu";
const URGENT_REMINDER_TITLE = "Sắp đến giờ thi đấu!";
const DEFAULT_FIELD_NAME = "Sân thể thao";
const VN_TIME_ZONE = "Asia/Ho_Chi_Minh";
const LOOKAHEAD_MINUTES = 15;
const VALID_REMINDER_STATUSES = ["confirmed", "approved", "completed", "paid"];
const REMINDER_METADATA = JSON.stringify({ icon: REMINDER_TYPE });
const URGENT_REMINDER_METADATA = JSON.stringify({ icon: URGENT_REMINDER_TYPE });

const buildInClause = (items) => items.map(() => "?").join(", ");

const addMinutes = (date, minutes) =>
  new Date(date.getTime() + minutes * 60 * 1000);

const getLocalDateTimeParts = (value) => {
  if (!value) return null;

  if (value instanceof Date) {
    const parts = new Intl.DateTimeFormat("en-GB", {
      timeZone: VN_TIME_ZONE,
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
      hourCycle: "h23",
    }).formatToParts(value);

    return {
      year: Number(parts.find((item) => item.type === "year")?.value || 0),
      month: Number(parts.find((item) => item.type === "month")?.value || 0),
      day: Number(parts.find((item) => item.type === "day")?.value || 0),
      hour: Number(parts.find((item) => item.type === "hour")?.value || 0),
      minute: Number(parts.find((item) => item.type === "minute")?.value || 0),
      second: Number(parts.find((item) => item.type === "second")?.value || 0),
    };
  }

  const raw = String(value).trim();
  const match = raw.match(
    /^(\d{4})-(\d{2})-(\d{2})(?:[ T](\d{2}):(\d{2})(?::(\d{2}))?)?/,
  );
  if (!match) return null;

  return {
    year: Number(match[1]),
    month: Number(match[2]),
    day: Number(match[3]),
    hour: Number(match[4] || 0),
    minute: Number(match[5] || 0),
    second: Number(match[6] || 0),
  };
};

const getDbDateTimeParts = (value) => {
  if (!value) return null;

  if (value instanceof Date) {
    return {
      year: value.getUTCFullYear(),
      month: value.getUTCMonth() + 1,
      day: value.getUTCDate(),
      hour: value.getUTCHours(),
      minute: value.getUTCMinutes(),
      second: value.getUTCSeconds(),
    };
  }

  return getLocalDateTimeParts(value);
};

const padDatePart = (value) => String(value).padStart(2, "0");

const formatDateTimeForDb = (value) => {
  const parts = getLocalDateTimeParts(value);
  return `${parts.year}-${padDatePart(parts.month)}-${padDatePart(parts.day)} ${padDatePart(parts.hour)}:${padDatePart(parts.minute)}:${padDatePart(parts.second)}`;
};

const toComparableTime = (parts) =>
  Date.UTC(
    parts.year,
    parts.month - 1,
    parts.day,
    parts.hour,
    parts.minute,
    parts.second || 0,
  );

const normalizeBookingIds = (items) => [
  ...new Set(
    (Array.isArray(items) ? items : [])
      .map((item) => Number.parseInt(item, 10))
      .filter((item) => Number.isInteger(item) && item > 0),
  ),
];

const diffMinutes = (startTime, now) =>
  Math.ceil(
    (toComparableTime(getDbDateTimeParts(startTime)) -
      toComparableTime(getLocalDateTimeParts(now))) /
      60000,
  );

const resolveReminderType = (booking, now) => {
  const remainingMinutes = diffMinutes(booking.reminder_start_time, now);
  if (remainingMinutes > 10 && remainingMinutes <= 15) {
    return REMINDER_TYPE;
  }
  if (remainingMinutes > 0 && remainingMinutes <= 10) {
    return URGENT_REMINDER_TYPE;
  }
  return null;
};

const buildReminderSubtitle = (booking) => {
  const fieldName = booking.field_name || DEFAULT_FIELD_NAME;
  const startTime = formatSlotTimeLabel(booking.reminder_start_time);
  return `Còn khoảng 15 phút nữa đến lịch đặt sân ${fieldName} lúc ${startTime}.`;
};

const buildUrgentReminderSubtitle = (booking, now) => {
  const remainingMinutes = Math.max(1, diffMinutes(booking.reminder_start_time, now));
  return `Chỉ còn ${remainingMinutes} phút nữa đến lịch đặt sân của bạn.`;
};

const buildReminderContent = (booking, slots) => {
  const fieldName = booking.field_name || DEFAULT_FIELD_NAME;
  const slotSummary = buildBookingSlotSummary(slots, {
    fallback: `${formatSlotTimeLabel(booking.reminder_start_time)} - ${formatSlotTimeLabel(booking.reminder_end_time)}`,
  });

  return `Sân: ${fieldName}. Khung giờ:\n${slotSummary}`;
};

const findUpcomingReminderBookings = async (now, lookaheadMinutes) => {
  const windowEnd = addMinutes(now, lookaheadMinutes);
  const windowStartLabel = formatDateTimeForDb(now);
  const windowEndLabel = formatDateTimeForDb(windowEnd);

  const [rows] = await sequelize.query(
    `SELECT
      b.booking_id,
      b.customer_id,
      b.field_id,
      f.field_name,
      nr.id AS regular_notification_id,
      nu.id AS urgent_notification_id,
      COALESCE(MIN(bs.start_time), b.start_time) AS reminder_start_time,
      COALESCE(MAX(bs.end_time), b.end_time) AS reminder_end_time
     FROM bookings b
     LEFT JOIN booking_slots bs ON bs.booking_id = b.booking_id
     LEFT JOIN fields f ON f.field_id = b.field_id
     LEFT JOIN notifications nr
       ON nr.user_id = b.customer_id
      AND nr.booking_id = b.booking_id
      AND nr.type = ?
     LEFT JOIN notifications nu
       ON nu.user_id = b.customer_id
      AND nu.booking_id = b.booking_id
      AND nu.type = ?
     WHERE b.customer_id IS NOT NULL
       AND b.status IN (${buildInClause(VALID_REMINDER_STATUSES)})
     GROUP BY
       b.booking_id,
       b.customer_id,
       b.field_id,
       f.field_name,
       b.start_time,
       b.end_time,
       nr.id,
       nu.id
     HAVING reminder_start_time > ?
        AND reminder_start_time <= ?
     ORDER BY reminder_start_time ASC`,
    {
      replacements: [
        REMINDER_TYPE,
        URGENT_REMINDER_TYPE,
        ...VALID_REMINDER_STATUSES,
        windowStartLabel,
        windowEndLabel,
      ],
    },
  );

  return rows;
};

export const createUpcomingBookingReminderNotifications = async (
  options = {},
) => {
  const {
    now = new Date(),
    lookaheadMinutes = LOOKAHEAD_MINUTES,
  } = options;

  const bookings = await findUpcomingReminderBookings(now, lookaheadMinutes);
  const bookingIds = normalizeBookingIds(bookings.map((booking) => booking.booking_id));

  if (bookingIds.length === 0) {
    return { created: 0, checked: 0 };
  }

  const slotRows = await listBookingSlotsByBookingIds(bookingIds);
  const slotMap = mapBookingSlotsByBookingId(slotRows);
  let created = 0;

  for (const booking of bookings) {
    const bookingId = Number.parseInt(booking.booking_id, 10);
    const userId = Number.parseInt(booking.customer_id, 10);
    const fieldId = Number.parseInt(booking.field_id, 10);
    const reminderType = resolveReminderType(booking, now);
    const alreadyCreated =
      (reminderType === REMINDER_TYPE && booking.regular_notification_id) ||
      (reminderType === URGENT_REMINDER_TYPE && booking.urgent_notification_id);

    if (!Number.isInteger(bookingId) || !Number.isInteger(userId) || !reminderType || alreadyCreated) {
      continue;
    }

    const slots = slotMap.get(bookingId) || [];
    const isUrgent = reminderType === URGENT_REMINDER_TYPE;
    const [result] = await sequelize.query(
      `INSERT IGNORE INTO notifications
        (user_id, type, section, title, subtitle, content, target_type, target_id, booking_id, field_id, is_read, metadata, created_at, updated_at)
       VALUES (?, ?, 'activity', ?, ?, ?, 'booking', ?, ?, ?, 0, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)`,
      {
        replacements: [
          userId,
          reminderType,
          isUrgent ? URGENT_REMINDER_TITLE : REMINDER_TITLE,
          isUrgent ? buildUrgentReminderSubtitle(booking, now) : buildReminderSubtitle(booking),
          isUrgent ? buildUrgentReminderSubtitle(booking, now) : buildReminderContent(booking, slots),
          bookingId,
          bookingId,
          Number.isInteger(fieldId) ? fieldId : null,
          isUrgent ? URGENT_REMINDER_METADATA : REMINDER_METADATA,
        ],
      },
    );

    created += Number(result?.affectedRows || 0);
  }

  return { created, checked: bookings.length };
};

export const startUpcomingBookingReminderJob = (options = {}) => {
  const {
    intervalMs = 60 * 1000,
    runImmediately = true,
  } = options;

  let isRunning = false;

  const run = async () => {
    if (isRunning) return;
    isRunning = true;
    try {
      const result = await createUpcomingBookingReminderNotifications();
      if (result.created > 0) {
        console.log(`booking reminder job created ${result.created} notification(s)`);
      }
    } catch (error) {
      console.error("booking reminder job error:", error.message);
    } finally {
      isRunning = false;
    }
  };

  if (runImmediately) {
    run();
  }

  return setInterval(run, intervalMs);
};

export default {
  createUpcomingBookingReminderNotifications,
  startUpcomingBookingReminderJob,
};
