import crypto from "crypto";
import sequelize from "../config/database.js";

const CHECKIN_CODE_LENGTH = 8;
const CHECKIN_CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

const PUBLIC_STATUS_LABELS = {
  paid: "Đã thanh toán",
  checked_in: "Đã check-in",
  expired: "Hết hạn",
  cancelled: "Đã hủy",
  pending: "Chờ thanh toán",
};

const createShareToken = () => crypto.randomBytes(18).toString("base64url");

const createCheckInCode = () => {
  const bytes = crypto.randomBytes(CHECKIN_CODE_LENGTH);
  let result = "";
  for (let index = 0; index < CHECKIN_CODE_LENGTH; index += 1) {
    result += CHECKIN_CHARSET[bytes[index] % CHECKIN_CHARSET.length];
  }
  return result;
};

const buildInClause = (items) => items.map(() => "?").join(", ");

const formatMoneyLabel = (value) => {
  const amount = Number(value || 0);
  return `${amount.toLocaleString("vi-VN")} VND`;
};

const normalizePaymentMethodLabel = (value) => {
  const normalized = String(value || "").trim().toLowerCase();
  if (!normalized) return "";
  if (normalized === "momo") return "MoMo";
  if (normalized === "bank_transfer") return "Chuyển khoản";
  if (normalized === "cash") return "Tiền mặt";
  if (normalized === "zalopay") return "ZaloPay";
  if (normalized === "vnpay") return "VNPay";
  if (normalized === "credit_card") return "Thẻ tín dụng";
  return value;
};

export const derivePublicBookingStatusCode = (booking) => {
  const bookingStatus = String(
    booking.booking_status || booking.status || "",
  ).toLowerCase();
  const paymentStatus = String(booking.payment_status || "").toLowerCase();

  if (bookingStatus === "cancelled" || bookingStatus === "rejected") {
    return "cancelled";
  }

  if (booking.checked_in_at) {
    return "checked_in";
  }

  if (booking.end_time) {
    const endTime = new Date(booking.end_time).getTime();
    if (Number.isFinite(endTime) && endTime < Date.now()) {
      return "expired";
    }
  }

  if (
    paymentStatus === "completed" ||
    ["confirmed", "approved", "completed"].includes(bookingStatus)
  ) {
    return "paid";
  }

  return "pending";
};

export const derivePublicBookingStatusLabel = (booking) =>
  PUBLIC_STATUS_LABELS[derivePublicBookingStatusCode(booking)] ||
  PUBLIC_STATUS_LABELS.pending;

const shouldHaveShareAccess = (booking) =>
  ["confirmed", "approved", "completed"].includes(
    String(booking.booking_status || booking.status || "").toLowerCase(),
  ) || String(booking.payment_status || "").toLowerCase() === "completed";

const ensureUniqueShareToken = async (transaction) => {
  for (let attempt = 0; attempt < 8; attempt += 1) {
    const value = createShareToken();
    const [rows] = await sequelize.query(
      "SELECT booking_id FROM bookings WHERE share_token = ? LIMIT 1",
      { replacements: [value], transaction },
    );
    if (!rows?.[0]) {
      return value;
    }
  }
  throw new Error("Unable to generate unique booking share token");
};

const ensureUniqueCheckInCode = async (transaction) => {
  for (let attempt = 0; attempt < 8; attempt += 1) {
    const value = createCheckInCode();
    const [rows] = await sequelize.query(
      "SELECT booking_id FROM bookings WHERE checkin_code = ? LIMIT 1",
      { replacements: [value], transaction },
    );
    if (!rows?.[0]) {
      return value;
    }
  }
  throw new Error("Unable to generate unique booking check-in code");
};

export const ensureBookingShareAccess = async (
  bookingId,
  options = {},
) => {
  const { transaction = null, force = false } = options;
  const [rows] = await sequelize.query(
    `SELECT booking_id, share_token, checkin_code, status AS booking_status
     FROM bookings
     WHERE booking_id = ?
     LIMIT 1`,
    { replacements: [bookingId], transaction },
  );

  const booking = rows?.[0];
  if (!booking) {
    return null;
  }

  if (!force && !shouldHaveShareAccess(booking)) {
    return {
      shareToken: booking.share_token || "",
      checkInCode: booking.checkin_code || "",
    };
  }

  let shareToken = booking.share_token || "";
  let checkInCode = booking.checkin_code || "";
  if (shareToken && checkInCode) {
    return { shareToken, checkInCode };
  }

  shareToken = shareToken || (await ensureUniqueShareToken(transaction));
  checkInCode = checkInCode || (await ensureUniqueCheckInCode(transaction));

  await sequelize.query(
    `UPDATE bookings
     SET share_token = ?, checkin_code = ?
     WHERE booking_id = ?`,
    {
      replacements: [shareToken, checkInCode, bookingId],
      transaction,
    },
  );

  return { shareToken, checkInCode };
};

const fetchBookingShareRow = async ({
  whereClause,
  replacements,
  transaction = null,
}) => {
  const [rows] = await sequelize.query(
    `
      SELECT
        b.booking_id,
        b.customer_id,
        b.field_id,
        b.court_id,
        b.start_time,
        b.end_time,
        DATE_FORMAT(b.start_time, '%d/%m/%Y') AS booking_date_label,
        DATE_FORMAT(b.start_time, '%H:%i') AS booking_start_time,
        DATE_FORMAT(b.end_time, '%H:%i') AS booking_end_time,
        b.status AS booking_status,
        b.note,
        b.price,
        b.share_token,
        b.checkin_code,
        b.checked_in_at,
        p.person_name AS customer_name,
        p.phone AS customer_phone,
        f.field_name,
        f.location,
        f.phone AS field_phone,
        m.person_name AS owner_name,
        m.phone AS owner_phone,
        fc.court_name,
        pay.payment_method,
        pay.payment_status,
        pay.transaction_id,
        pay.order_id,
        pay.request_id,
        pay.amount AS payment_amount
      FROM bookings b
      LEFT JOIN person p ON b.customer_id = p.person_id
      LEFT JOIN fields f ON b.field_id = f.field_id
      LEFT JOIN person m ON f.manager_id = m.person_id
      LEFT JOIN field_courts fc ON b.court_id = fc.court_id
      LEFT JOIN payments pay ON pay.payment_id = (
        SELECT p2.payment_id
        FROM payments p2
        WHERE p2.booking_id = b.booking_id
        ORDER BY p2.payment_id DESC
        LIMIT 1
      )
      WHERE ${whereClause}
      LIMIT 1
    `,
    { replacements, transaction },
  );

  const row = rows?.[0];
  if (!row) {
    return null;
  }

  if (shouldHaveShareAccess(row) && (!row.share_token || !row.checkin_code)) {
    const access = await ensureBookingShareAccess(row.booking_id, { transaction });
    row.share_token = access?.shareToken || row.share_token || "";
    row.checkin_code = access?.checkInCode || row.checkin_code || "";
  }

  const statusCode = derivePublicBookingStatusCode(row);

  return {
    bookingId: row.booking_id,
    bookingCode: `#B${row.booking_id}`,
    customerId: row.customer_id,
    fieldId: row.field_id,
    fieldName: row.field_name || "",
    fieldAddress: row.location || "",
    date: row.booking_date_label || "",
    startTime: row.booking_start_time || "",
    endTime: row.booking_end_time || "",
    timeRange: [row.booking_start_time, row.booking_end_time]
      .filter(Boolean)
      .join(" - "),
    bookingStatus: row.booking_status || "",
    statusCode,
    statusLabel: PUBLIC_STATUS_LABELS[statusCode] || PUBLIC_STATUS_LABELS.pending,
    totalPriceValue: Number(row.payment_amount || row.price || 0),
    totalPrice: formatMoneyLabel(row.payment_amount || row.price || 0),
    paymentMethod: normalizePaymentMethodLabel(row.payment_method),
    paymentStatus: row.payment_status || "",
    transactionId: row.transaction_id || "",
    orderId: row.order_id || "",
    requestId: row.request_id || "",
    ownerNote: row.note || "",
    userName: row.customer_name || "",
    userPhone: row.customer_phone || "",
    fieldAvatar: null,
    ownerName: row.owner_name || "",
    ownerPhone: row.field_phone || row.owner_phone || "",
    courtName: row.court_name || "",
    shareToken: row.share_token || "",
    checkInCode: row.checkin_code || "",
    checkedInAt: row.checked_in_at || null,
  };
};

export const getBookingShareDetailByBookingId = async (
  bookingId,
  options = {},
) =>
  fetchBookingShareRow({
    whereClause: options.userId
      ? "b.booking_id = ? AND b.customer_id = ?"
      : "b.booking_id = ?",
    replacements: options.userId ? [bookingId, options.userId] : [bookingId],
    transaction: options.transaction,
  });

export const getBookingShareDetailByToken = async (
  shareToken,
  options = {},
) =>
  fetchBookingShareRow({
    whereClause: "b.share_token = ?",
    replacements: [shareToken],
    transaction: options.transaction,
  });

export const markBookingCheckedInByCode = async (managerId, checkInCode) => {
  const normalizedCode = String(checkInCode || "").trim().toUpperCase();
  if (!normalizedCode) {
    return null;
  }

  const [rows] = await sequelize.query(
    `
      SELECT
        b.booking_id
      FROM bookings b
      INNER JOIN fields f ON b.field_id = f.field_id
      WHERE f.manager_id = ?
        AND b.checkin_code = ?
      LIMIT 1
    `,
    { replacements: [managerId, normalizedCode] },
  );

  const booking = rows?.[0];
  if (!booking) {
    return null;
  }

  await sequelize.query(
    `UPDATE bookings
     SET checked_in_at = COALESCE(checked_in_at, NOW())
     WHERE booking_id = ?`,
    { replacements: [booking.booking_id] },
  );

  return getBookingShareDetailByBookingId(booking.booking_id);
};

export const getBookingShareDetailByCheckInCode = async (
  managerId,
  checkInCode,
) => {
  const normalizedCode = String(checkInCode || "").trim().toUpperCase();
  if (!normalizedCode) {
    return null;
  }

  const [rows] = await sequelize.query(
    `
      SELECT
        b.booking_id
      FROM bookings b
      INNER JOIN fields f ON b.field_id = f.field_id
      WHERE f.manager_id = ?
        AND b.checkin_code = ?
      LIMIT 1
    `,
    { replacements: [managerId, normalizedCode] },
  );

  const booking = rows?.[0];
  if (!booking) {
    return null;
  }

  return getBookingShareDetailByBookingId(booking.booking_id);
};

export const buildBookingShareUrl = (baseUrl, shareToken) => {
  if (!baseUrl || !shareToken) return "";
  return `${String(baseUrl).replace(/\/+$/, "")}/l/booking/${shareToken}`;
};

export const buildBookingShareResponse = (detail, baseUrl) => {
  if (!detail) return null;
  return {
    bookingId: detail.bookingId,
    bookingCode: detail.bookingCode,
    status: detail.statusLabel,
    statusCode: detail.statusCode,
    date: detail.date,
    startTime: detail.startTime,
    endTime: detail.endTime,
    timeRange: detail.timeRange,
    totalPrice: detail.totalPrice,
    paymentMethod: detail.paymentMethod,
    transactionId: detail.transactionId,
    orderId: detail.orderId,
    ownerNote: detail.ownerNote,
    checkInCode: detail.checkInCode,
    shareUrl: buildBookingShareUrl(baseUrl, detail.shareToken),
    user: {
      name: detail.userName,
      phone: detail.userPhone,
    },
    field: {
      fieldId: detail.fieldId,
      fieldName: detail.fieldName,
      address: detail.fieldAddress,
      avatar: detail.fieldAvatar,
      ownerName: detail.ownerName,
      ownerPhone: detail.ownerPhone,
    },
  };
};

export const bulkEnsureBookingShareAccess = async (
  bookingIds,
  options = {},
) => {
  const normalizedIds = Array.isArray(bookingIds)
    ? [...new Set(
        bookingIds
          .map((item) => Number.parseInt(item, 10))
          .filter((item) => Number.isInteger(item) && item > 0),
      )]
    : [];

  if (normalizedIds.length === 0) {
    return [];
  }

  const { transaction = null } = options;
  const [rows] = await sequelize.query(
    `SELECT booking_id, status AS booking_status
     FROM bookings
     WHERE booking_id IN (${buildInClause(normalizedIds)})`,
    { replacements: normalizedIds, transaction },
  );

  for (const row of rows) {
    if (shouldHaveShareAccess(row)) {
      await ensureBookingShareAccess(row.booking_id, { transaction });
    }
  }

  return rows;
};
