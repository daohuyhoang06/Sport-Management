import sequelize from "../../config/database.js";
import bcrypt from "bcrypt";
import { ACTIVE_BOOKING_STATUS_CONDITION } from "../bookingSlotService.js";

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

const findBookingSlotConflict = async ({
  transaction = null,
  fieldId,
  courtId,
  startTime,
  endTime,
}) => {
  const [slotConflicts] = await sequelize.query(
    `SELECT bs.booking_slot_id
     FROM booking_slots bs
     INNER JOIN bookings b ON b.booking_id = bs.booking_id
     WHERE bs.field_id = ?
       AND (? IS NULL OR bs.court_id = ? OR bs.court_id IS NULL)
       AND bs.start_time < ?
       AND bs.end_time > ?
       AND ${ACTIVE_BOOKING_STATUS_CONDITION}
     LIMIT 1
     FOR UPDATE`,
    {
      replacements: [fieldId, courtId, courtId, endTime, startTime],
      transaction,
    }
  );
  if (slotConflicts.length > 0) return true;

  const [legacyConflicts] = await sequelize.query(
    `SELECT b.booking_id
     FROM bookings b
     WHERE b.field_id = ?
       AND (? IS NULL OR b.court_id = ? OR b.court_id IS NULL)
       AND b.start_time < ?
       AND b.end_time > ?
       AND ${ACTIVE_BOOKING_STATUS_CONDITION}
       AND NOT EXISTS (
         SELECT 1
         FROM booking_slots bs
         WHERE bs.booking_id = b.booking_id
       )
     LIMIT 1
     FOR UPDATE`,
    {
      replacements: [fieldId, courtId, courtId, endTime, startTime],
      transaction,
    }
  );

  return legacyConflicts.length > 0;
};

/**
 * Get all bookings for manager's fields
 */
export const getManagerBookingsService = async (managerId, filters = {}) => {
  try {
    const { status, field_id, startDate, endDate } = filters;

    let whereConditions = ["f.manager_id = ?"];
    let replacements = [managerId];

    if (status && status !== "all") {
      whereConditions.push("b.status = ?");
      replacements.push(status);
    }

    if (field_id) {
      whereConditions.push("b.field_id = ?");
      replacements.push(field_id);
    }

    if (startDate) {
      whereConditions.push("DATE(b.start_time) >= ?");
      replacements.push(startDate);
    }

    if (endDate) {
      whereConditions.push("DATE(b.start_time) <= ?");
      replacements.push(endDate);
    }

    const whereClause =
      whereConditions.length > 0
        ? `WHERE ${whereConditions.join(" AND ")}`
        : "";

    const [bookings] = await sequelize.query(
      `
      SELECT
        b.booking_id,
        b.field_id,
        b.customer_id,
        b.court_id,
        DATE_FORMAT(CONVERT_TZ(b.created_at, '+00:00', '+07:00'), '%Y-%m-%d %H:%i:%s') AS created_at,
        b.start_time,
        b.end_time,
        b.status,
        b.price,
        b.note,
        pay.payment_method,
        COALESCE(pay.payment_status, pay.status) AS payment_status,
        pay.amount AS payment_amount,
        f.field_name,
        f.location,
        p.person_name as customer_name,
        p.email as customer_email,
        p.phone as customer_phone,
        fc.court_code,
        fc.court_name,
        CASE WHEN EXISTS (
          SELECT 1 FROM booking_history bh
          WHERE bh.booking_id = b.booking_id
            AND bh.author = 'manager'
            AND bh.action = 'Tạo đặt sân'
        ) THEN 1 ELSE 0 END AS manager_created
      FROM bookings b
      INNER JOIN fields f ON b.field_id = f.field_id
      LEFT JOIN person p ON b.customer_id = p.person_id
      LEFT JOIN field_courts fc ON b.court_id = fc.court_id
      ${latestPaymentJoin}
      ${whereClause}
      ORDER BY b.booking_id DESC
    `,
      { replacements },
    );

    return bookings;
  } catch (error) {
    console.error("Error in getManagerBookingsService:", error);
    throw error;
  }
};

/**
 * Get booking by ID (only if belongs to manager's field)
 */
export const getManagerBookingByIdService = async (managerId, bookingId) => {
  try {
    const [bookings] = await sequelize.query(
      `
      SELECT
        b.booking_id,
        b.field_id,
        b.customer_id,
        b.court_id,
        DATE_FORMAT(CONVERT_TZ(b.created_at, '+00:00', '+07:00'), '%Y-%m-%d %H:%i:%s') AS created_at,
        b.start_time,
        b.end_time,
        b.status,
        b.price,
        b.note,
        pay.payment_method,
        COALESCE(pay.payment_status, pay.status) AS payment_status,
        pay.amount AS payment_amount,
        f.field_name,
        f.location,
        f.manager_id,
        p.person_name as customer_name,
        p.email as customer_email,
        p.phone as customer_phone,
        fc.court_code,
        fc.court_name,
        CASE WHEN EXISTS (
          SELECT 1 FROM booking_history bh
          WHERE bh.booking_id = b.booking_id
            AND bh.author = 'manager'
            AND bh.action = 'Tạo đặt sân'
        ) THEN 1 ELSE 0 END AS manager_created
      FROM bookings b
      INNER JOIN fields f ON b.field_id = f.field_id
      LEFT JOIN person p ON b.customer_id = p.person_id
      LEFT JOIN field_courts fc ON b.court_id = fc.court_id
      ${latestPaymentJoin}
      WHERE b.booking_id = ? AND f.manager_id = ?
    `,
      { replacements: [bookingId, managerId] },
    );

    return bookings[0] || null;
  } catch (error) {
    console.error("Error in getManagerBookingByIdService:", error);
    throw error;
  }
};

/**
 * Update booking status + ghi lịch sử
 */
export const updateBookingStatusService = async (
  managerId,
  bookingId,
  status,
  note = null,
) => {
  try {
    const booking = await getManagerBookingByIdService(managerId, bookingId);
    if (!booking) throw new Error("Booking not found or unauthorized");

    const fromStatus = booking.status;

    if (note) {
      await sequelize.query(
        `UPDATE bookings b INNER JOIN fields f ON b.field_id = f.field_id
         SET b.status = ?, b.note = CONCAT(COALESCE(b.note, ''), ?)
         WHERE b.booking_id = ? AND f.manager_id = ?`,
        { replacements: [status, ` | ${note}`, bookingId, managerId] }
      );
    } else {
      await sequelize.query(
        `UPDATE bookings b INNER JOIN fields f ON b.field_id = f.field_id
         SET b.status = ?
         WHERE b.booking_id = ? AND f.manager_id = ?`,
        { replacements: [status, bookingId, managerId] }
      );
    }

    // Ghi booking history
    const actionMap = {
      confirmed: 'Xác nhận đặt sân',
      rejected:  'Từ chối đặt sân',
      cancelled: 'Hủy đặt sân',
      completed: 'Hoàn thành',
    };
    await sequelize.query(
      `INSERT INTO booking_history (booking_id, action, from_status, to_status, note, author)
       VALUES (?, ?, ?, ?, ?, 'manager')`,
      { replacements: [bookingId, actionMap[status] || status, fromStatus, status, note || null] }
    );

    return { success: true };
  } catch (error) {
    console.error("Error in updateBookingStatusService:", error);
    throw error;
  }
};

/**
 * Manager tạo booking mới (walk-in hoặc thay mặt khách hàng)
 */
export const createBookingService = async (managerId, data) => {
  const { field_id, court_id, customer_phone, customer_name, note, price } = data;

  // Chuẩn hoá datetime: ISO 8601 → MySQL DATETIME
  const toMysqlDatetime = (v) => v ? v.replace('T', ' ').replace('Z', '').split('.')[0] : null;
  const start_time = toMysqlDatetime(data.start_time);
  const end_time   = toMysqlDatetime(data.end_time);

  if (!start_time || !end_time || new Date(end_time) <= new Date(start_time)) {
    throw new Error('Khung giờ đặt sân không hợp lệ');
  }

  let newBookingId = null;
  let finalPrice = price;

  await sequelize.transaction(async (transaction) => {
    const [[field]] = await sequelize.query(
      `SELECT field_id, slot_price, slot_minutes
       FROM fields
       WHERE field_id = ? AND manager_id = ?
       FOR UPDATE`,
      {
        replacements: [field_id, managerId],
        transaction,
      }
    );
    if (!field) throw new Error('Field không tồn tại hoặc không có quyền');

    const [[blockedConflict]] = await sequelize.query(
      `SELECT slot_id
       FROM field_blocked_slots
       WHERE field_id = ?
         AND block_date = DATE(?)
         AND start_time < TIME(?)
         AND end_time > TIME(?)
       LIMIT 1
       FOR UPDATE`,
      {
        replacements: [field_id, start_time, end_time, start_time],
        transaction,
      }
    );
    if (blockedConflict) {
      throw new Error('Khung giờ này đã bị khóa bởi quản lý');
    }

    const hasBookedConflict = await findBookingSlotConflict({
      transaction,
      fieldId: field_id,
      courtId: court_id ?? null,
      startTime: start_time,
      endTime: end_time,
    });
    if (hasBookedConflict) {
      throw new Error('Khung giờ này đã có người đặt');
    }

    let customerId = null;
    if (customer_phone) {
      const [[existing]] = await sequelize.query(
        'SELECT person_id FROM person WHERE phone = ? LIMIT 1',
        {
          replacements: [customer_phone],
          transaction,
        }
      );
      if (existing) {
        customerId = existing.person_id;
      } else if (customer_name) {
        // Tạo walk-in customer — username unique theo phone, password random (không dùng để login)
        const fakePassword = await bcrypt.hash(`walkin_${customer_phone}_${Date.now()}`, 10);
        const username = `walkin_${customer_phone}`;
        const [insertPerson] = await sequelize.query(
          `INSERT INTO person (person_name, phone, role, username, password) VALUES (?, ?, 'user', ?, ?)`,
          {
            replacements: [customer_name, customer_phone, username, fakePassword],
            transaction,
          }
        );
        customerId = insertPerson?.insertId || insertPerson;
      }
    }

    if (!finalPrice && field.slot_price && start_time && end_time) {
      const start = new Date(start_time);
      const end = new Date(end_time);
      const minutes = (end - start) / 60000;
      const slots = Math.ceil(minutes / (field.slot_minutes || 60));
      finalPrice = slots * parseFloat(field.slot_price);
    }

    const [result] = await sequelize.query(
      `INSERT INTO bookings (field_id, court_id, customer_id, manager_id, start_time, end_time, status, price, note)
       VALUES (?, ?, ?, ?, ?, ?, 'confirmed', ?, ?)`,
      {
        replacements: [
          field_id,
          court_id ?? null,
          customerId,
          managerId,
          start_time,
          end_time,
          finalPrice ?? null,
          note ?? null,
        ],
        transaction,
      }
    );

    newBookingId = result?.insertId || result;

    await sequelize.query(
      `INSERT INTO booking_slots (booking_id, field_id, court_id, start_time, end_time, price)
       VALUES (?, ?, ?, ?, ?, ?)`,
      {
        replacements: [
          newBookingId,
          field_id,
          court_id ?? null,
          start_time,
          end_time,
          finalPrice ?? 0,
        ],
        transaction,
      }
    );

    await sequelize.query(
      `INSERT INTO booking_history (booking_id, action, from_status, to_status, note, author)
       VALUES (?, 'Tạo đặt sân', null, 'confirmed', ?, 'manager')`,
      {
        replacements: [newBookingId, note || null],
        transaction,
      }
    );
  });

  const [[booking]] = await sequelize.query(
    `SELECT b.*, f.field_name, f.location,
            p.person_name as customer_name, p.email as customer_email, p.phone as customer_phone,
            fc.court_code, fc.court_name,
            1 AS manager_created
     FROM bookings b
     INNER JOIN fields f ON b.field_id = f.field_id
     LEFT JOIN person p ON b.customer_id = p.person_id
     LEFT JOIN field_courts fc ON b.court_id = fc.court_id
     WHERE b.booking_id = ?`,
    { replacements: [newBookingId] }
  );

  return booking;
};

/**
 * Lấy khung giờ đã đặt của một sân con trong ngày — dùng cho form đặt sân
 * GET /api/manager/fields/:fieldId/courts/:courtId/availability?date=YYYY-MM-DD
 */
export const getCourtAvailabilityService = async (managerId, fieldId, courtId, date) => {
  const [[field]] = await sequelize.query(
    'SELECT field_id FROM fields WHERE field_id = ? AND manager_id = ?',
    { replacements: [fieldId, managerId] }
  );
  if (!field) throw new Error('Field không tồn tại hoặc không có quyền');

  const [booked] = await sequelize.query(
    `SELECT
       TIME_FORMAT(booked_ranges.start_time, '%H:%i') AS start_time,
       TIME_FORMAT(booked_ranges.end_time,   '%H:%i') AS end_time
     FROM (
       SELECT bs.start_time, bs.end_time
       FROM booking_slots bs
       INNER JOIN bookings b ON b.booking_id = bs.booking_id
       WHERE bs.field_id = ?
         AND (? IS NULL OR bs.court_id = ? OR bs.court_id IS NULL)
         AND DATE(bs.start_time) = ?
         AND ${ACTIVE_BOOKING_STATUS_CONDITION}

       UNION ALL

       SELECT b.start_time, b.end_time
       FROM bookings b
       WHERE b.field_id = ?
         AND (? IS NULL OR b.court_id = ? OR b.court_id IS NULL)
         AND DATE(b.start_time) = ?
         AND ${ACTIVE_BOOKING_STATUS_CONDITION}
         AND NOT EXISTS (
           SELECT 1
           FROM booking_slots bs
           WHERE bs.booking_id = b.booking_id
         )
     ) booked_ranges
     ORDER BY booked_ranges.start_time ASC`,
    {
      replacements: [
        fieldId,
        courtId,
        courtId,
        date,
        fieldId,
        courtId,
        courtId,
        date,
      ],
    }
  );
  return booked;
};

/**
 * Lấy lịch sử thay đổi trạng thái của một booking
 */
export const getBookingHistoryService = async (managerId, bookingId) => {
  // Xác nhận booking thuộc manager
  const booking = await getManagerBookingByIdService(managerId, bookingId);
  if (!booking) throw new Error('Booking not found or unauthorized');

  const [history] = await sequelize.query(
    `SELECT history_id, booking_id, action, from_status, to_status, note, author, created_at
     FROM booking_history
     WHERE booking_id = ?
     ORDER BY created_at ASC`,
    { replacements: [bookingId] }
  );
  return history;
};
