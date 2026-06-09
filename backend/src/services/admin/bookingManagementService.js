import sequelize from "../../config/database.js";

const adminBookingSelect = `
      b.booking_id,
      CONCAT('B', LPAD(b.booking_id, 6, '0')) AS bookingCode,
      b.customer_id,
      b.field_id,
      b.start_time,
      b.end_time,
      DATE_FORMAT(b.start_time, '%Y-%m-%d') AS bookingDate,
      DATE_FORMAT(b.start_time, '%H:%i') AS startTime,
      DATE_FORMAT(b.end_time, '%H:%i') AS endTime,
      b.status,
      b.price,
      b.price AS totalPrice,
      b.note,
      COALESCE(b.customer_name, p.person_name, p.username, '-') AS customer_name,
      COALESCE(b.customer_phone, p.phone, '-') AS customer_phone,
      p.email AS customer_email,
      p.address AS customer_address,
      f.field_name,
      f.location,
      f.location AS fieldAddress,
      f.phone AS fieldPhone,
      pay.payment_id,
      pay.amount AS paymentAmount,
      pay.payment_method AS paymentMethod,
      COALESCE(pay.payment_status, pay.status) AS paymentStatus,
      pay.transaction_id AS transactionId,
      pay.transaction_code AS transactionCode,
      DATE_FORMAT(COALESCE(pay.paid_at, pay.payment_date, pay.created_at), '%Y-%m-%d %H:%i') AS paymentCreatedAt`;

const latestPaymentJoin = `
     LEFT JOIN payments pay ON pay.payment_id = (
       SELECT p2.payment_id
       FROM payments p2
       WHERE p2.booking_id = b.booking_id
          OR (
            p2.booking_ids_json IS NOT NULL
            AND JSON_VALID(p2.booking_ids_json)
            AND JSON_CONTAINS(p2.booking_ids_json, CAST(b.booking_id AS CHAR), '$')
          )
       ORDER BY p2.payment_id DESC
       LIMIT 1
     )`;

/**
 * Get all bookings with filters and pagination
 */
export const getAllBookingsService = async (filters = {}, pagination = {}) => {
  const {
    page = 1,
    limit = 10,
    status = "",
    field_id = "",
    startDate = "",
    endDate = "",
  } = { ...filters, ...pagination };
  const offset = (parseInt(page) - 1) * parseInt(limit);

  let whereConditions = [];
  let queryParams = [];

  if (status) {
    whereConditions.push("b.status = ?");
    queryParams.push(status);
  }
  if (field_id) {
    whereConditions.push("b.field_id = ?");
    queryParams.push(field_id);
  }

  if (startDate && endDate) {
    whereConditions.push("b.start_time BETWEEN ? AND ?");
    queryParams.push(startDate, endDate);
  } else if (startDate) {
    whereConditions.push("b.start_time >= ?");
    queryParams.push(startDate);
  } else if (endDate) {
    whereConditions.push("b.start_time <= ?");
    queryParams.push(endDate);
  }

  const whereClause =
    whereConditions.length > 0 ? "WHERE " + whereConditions.join(" AND ") : "";

  // Get total count
  const [[{ total }]] = await sequelize.query(
    `SELECT COUNT(*) as total FROM bookings b ${whereClause}`,
    { replacements: queryParams },
  );

  // Get bookings
  const [bookings] = await sequelize.query(
    `SELECT 
      ${adminBookingSelect}
     FROM bookings b
     LEFT JOIN person p ON b.customer_id = p.person_id
     LEFT JOIN fields f ON b.field_id = f.field_id
     ${latestPaymentJoin}
     ${whereClause}
     ORDER BY b.start_time DESC
     LIMIT ? OFFSET ?`,
    { replacements: [...queryParams, parseInt(limit), offset] },
  );

  return {
    bookings,
    total: parseInt(total),
    page: parseInt(page),
    totalPages: Math.ceil(total / limit),
  };
};

/**
 * Get booking by ID
 */
export const getBookingByIdService = async (id) => {
  const [[booking]] = await sequelize.query(
    `SELECT 
      ${adminBookingSelect}
     FROM bookings b
     LEFT JOIN person p ON b.customer_id = p.person_id
     LEFT JOIN fields f ON b.field_id = f.field_id
     ${latestPaymentJoin}
     WHERE b.booking_id = ?`,
    { replacements: [id] },
  );

  return booking;
};

/**
 * Update booking status
 */
export const updateBookingStatusService = async (id, status, note = "") => {
  const [[booking]] = await sequelize.query(
    "SELECT booking_id, status, note FROM bookings WHERE booking_id = ?",
    { replacements: [id] },
  );

  if (!booking) {
    throw new Error("Booking not found");
  }

  let updatedNote = booking.note || "";
  if (note) {
    updatedNote = updatedNote ? `${updatedNote}\n${note}` : note;
  }

  await sequelize.query(
    "UPDATE bookings SET status = ?, note = ? WHERE booking_id = ?",
    { replacements: [status, updatedNote, id] },
  );

  const [[updatedBooking]] = await sequelize.query(
    "SELECT * FROM bookings WHERE booking_id = ?",
    { replacements: [id] },
  );

  return updatedBooking;
};

/**
 * Cancel booking
 */
export const cancelBookingService = async (id, reason) => {
  const [[booking]] = await sequelize.query(
    "SELECT booking_id, status, note FROM bookings WHERE booking_id = ?",
    { replacements: [id] },
  );

  if (!booking) {
    throw new Error("Booking not found");
  }

  if (booking.status === "completed") {
    throw new Error("Cannot cancel completed booking");
  }

  const cancelNote = booking.note
    ? `${booking.note}\nCancellation reason: ${reason}`
    : `Cancellation reason: ${reason}`;

  await sequelize.query(
    "UPDATE bookings SET status = 'cancelled', note = ? WHERE booking_id = ?",
    { replacements: [cancelNote, id] },
  );

  const [[updatedBooking]] = await sequelize.query(
    "SELECT * FROM bookings WHERE booking_id = ?",
    { replacements: [id] },
  );

  return updatedBooking;
};

/**
 * Get booking statistics
 */
export const getBookingStatsService = async () => {
  const [[{ total }]] = await sequelize.query(
    "SELECT COUNT(*) as total FROM bookings",
  );

  const [[{ pending }]] = await sequelize.query(
    "SELECT COUNT(*) as pending FROM bookings WHERE status = 'pending'",
  );

  const [[{ confirmed }]] = await sequelize.query(
    "SELECT COUNT(*) as confirmed FROM bookings WHERE status = 'confirmed'",
  );

  const [[{ completed }]] = await sequelize.query(
    "SELECT COUNT(*) as completed FROM bookings WHERE status = 'completed'",
  );

  const [[{ cancelled }]] = await sequelize.query(
    "SELECT COUNT(*) as cancelled FROM bookings WHERE status = 'cancelled'",
  );

  const [[{ todayCount }]] = await sequelize.query(
    "SELECT COUNT(*) as todayCount FROM bookings WHERE DATE(start_time) = CURRENT_DATE",
  );

  return {
    total: parseInt(total),
    pending: parseInt(pending),
    confirmed: parseInt(confirmed),
    completed: parseInt(completed),
    cancelled: parseInt(cancelled),
    today: parseInt(todayCount),
  };
};

/**
 * Get bookings by date range
 */
export const getBookingsByDateRangeService = async (startDate, endDate) => {
  const [bookings] = await sequelize.query(
    `SELECT 
      ${adminBookingSelect}
     FROM bookings b
     LEFT JOIN person p ON b.customer_id = p.person_id
     LEFT JOIN fields f ON b.field_id = f.field_id
     ${latestPaymentJoin}
     WHERE b.start_time BETWEEN ? AND ?
     ORDER BY b.start_time ASC`,
    { replacements: [startDate, endDate] },
  );

  return bookings;
};

