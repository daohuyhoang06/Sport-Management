import sequelize from "../config/database.js";

const VN_TIME_ZONE = "Asia/Ho_Chi_Minh";

export const ACTIVE_BOOKING_STATUS_CONDITION = `
  (
    b.status = 'confirmed'
    OR (
      b.status = 'pending'
      AND (b.pending_expires_at IS NULL OR b.pending_expires_at > NOW())
    )
  )
`;

export const buildInClause = (items) => items.map(() => "?").join(", ");

const formatVnDatePart = (value, options) =>
  new Intl.DateTimeFormat("en-GB", {
    timeZone: VN_TIME_ZONE,
    ...options,
  }).format(new Date(value));

export const formatSlotTimeLabel = (value) =>
  formatVnDatePart(value, {
    hour: "2-digit",
    minute: "2-digit",
    hourCycle: "h23",
  });

export const formatSlotDateLabel = (value) =>
  formatVnDatePart(value, {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });

export const deriveAggregateCourtId = (slots) => {
  const normalizedCourtIds = [...new Set(
    (Array.isArray(slots) ? slots : [])
      .map((slot) => Number.parseInt(slot?.court_id ?? slot?.courtId, 10))
      .filter((slot) => Number.isInteger(slot)),
  )];

  return normalizedCourtIds.length === 1 ? normalizedCourtIds[0] : null;
};

export const deriveAggregateBookingValues = (slots) => {
  const normalizedSlots = Array.isArray(slots) ? slots : [];
  if (normalizedSlots.length === 0) {
    return {
      startTime: null,
      endTime: null,
      totalPrice: 0,
      courtId: null,
    };
  }

  const orderedSlots = [...normalizedSlots].sort((left, right) => {
    const leftStart = new Date(left.start_time).getTime();
    const rightStart = new Date(right.start_time).getTime();
    if (leftStart !== rightStart) return leftStart - rightStart;
    return new Date(left.end_time).getTime() - new Date(right.end_time).getTime();
  });

  return {
    startTime: orderedSlots[0].start_time,
    endTime: orderedSlots.reduce((latest, slot) => {
      if (!latest) return slot.end_time;
      return new Date(slot.end_time).getTime() > new Date(latest).getTime()
        ? slot.end_time
        : latest;
    }, null),
    totalPrice: orderedSlots.reduce(
      (sum, slot) => sum + Number(slot.price || 0),
      0,
    ),
    courtId: deriveAggregateCourtId(orderedSlots),
  };
};

export const listBookingSlotsByBookingIds = async (
  bookingIds,
  options = {},
) => {
  const normalizedIds = [...new Set(
    (Array.isArray(bookingIds) ? bookingIds : [])
      .map((item) => Number.parseInt(item, 10))
      .filter((item) => Number.isInteger(item) && item > 0),
  )];

  if (normalizedIds.length === 0) {
    return [];
  }

  const { transaction = null } = options;

  const [rows] = await sequelize.query(
    `SELECT
      bs.booking_slot_id,
      bs.booking_id,
      bs.field_id,
      bs.court_id,
      bs.start_time,
      bs.end_time,
      bs.price,
      fc.court_name
     FROM booking_slots bs
     LEFT JOIN field_courts fc ON bs.court_id = fc.court_id
     WHERE bs.booking_id IN (${buildInClause(normalizedIds)})
     ORDER BY bs.booking_id ASC, bs.start_time ASC, bs.end_time ASC, bs.booking_slot_id ASC`,
    transaction ? { replacements: normalizedIds, transaction } : { replacements: normalizedIds },
  );

  return rows;
};

export const mapBookingSlotsByBookingId = (slots) => {
  const result = new Map();

  (Array.isArray(slots) ? slots : []).forEach((slot) => {
    const bookingId = Number.parseInt(slot.booking_id, 10);
    if (!Number.isInteger(bookingId)) {
      return;
    }
    if (!result.has(bookingId)) {
      result.set(bookingId, []);
    }
    result.get(bookingId).push(slot);
  });

  return result;
};

export const buildBookingSlotSummary = (
  slots,
  options = {},
) => {
  const {
    lineSeparator = "\n",
    fallback = "",
  } = options;

  const orderedSlots = Array.isArray(slots)
    ? [...slots].sort((left, right) => {
        const leftStart = new Date(left.start_time).getTime();
        const rightStart = new Date(right.start_time).getTime();
        if (leftStart !== rightStart) return leftStart - rightStart;
        return new Date(left.end_time).getTime() - new Date(right.end_time).getTime();
      })
    : [];

  if (orderedSlots.length === 0) {
    return fallback;
  }

  return orderedSlots
    .map((slot) => {
      const timeLabel = `${formatSlotTimeLabel(slot.start_time)} - ${formatSlotTimeLabel(slot.end_time)}`;
      const courtLabel = String(slot.court_name || "").trim();
      return courtLabel ? `${courtLabel}: ${timeLabel}` : timeLabel;
    })
    .join(lineSeparator);
};
