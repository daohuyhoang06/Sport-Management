import sequelize from '../../config/database.js';

/**
 * Get all bookings with filters and pagination
 */
export const getAllBookingsService = async (filters = {}, pagination = {}) => {
  const { page = 1, limit = 10, status = '', fieldId = '', startDate = '', endDate = '' } = { ...filters, ...pagination };
  const offset = (parseInt(page) - 1) * parseInt(limit);

  let whereConditions = [];
  let queryParams = [];
  
  if (status) {
    whereConditions.push('b.status = ?');
    queryParams.push(status);
  }
  if (fieldId) {
    whereConditions.push('b.field_id = ?');
    queryParams.push(fieldId);
  }
  
  if (startDate && endDate) {
    whereConditions.push('b.start_time BETWEEN ? AND ?');
    queryParams.push(startDate, endDate);
  } else if (startDate) {
    whereConditions.push('b.start_time >= ?');
    queryParams.push(startDate);
  } else if (endDate) {
    whereConditions.push('b.start_time <= ?');
    queryParams.push(endDate);
  }

  const whereClause = whereConditions.length > 0 ? 'WHERE ' + whereConditions.join(' AND ') : '';

  // Get total count
  const [[{ total }]] = await sequelize.query(
    `SELECT COUNT(*) as total FROM bookings b ${whereClause}`,
    { replacements: queryParams }
  );

  // Get bookings
  const [bookings] = await sequelize.query(
    `SELECT 
      b.booking_id, b.customer_id, b.field_id, b.start_time, b.end_time,
      b.status, b.price, b.note,
      p.person_name as customer_name, p.email as customer_email, p.phone as customer_phone,
      f.field_name, f.location
     FROM bookings b
     LEFT JOIN person p ON b.customer_id = p.person_id
     LEFT JOIN fields f ON b.field_id = f.field_id
     ${whereClause}
     ORDER BY b.start_time DESC
     LIMIT ? OFFSET ?`,
    { replacements: [...queryParams, parseInt(limit), offset] }
  );

  return {
    bookings,
    total: parseInt(total),
    page: parseInt(page),
    totalPages: Math.ceil(total / limit)
  };
};

/**
 * Get booking by ID
 */
export const getBookingByIdService = async (id) => {
  const [[booking]] = await sequelize.query(
    `SELECT 
      b.booking_id, b.customer_id, b.field_id, b.start_time, b.end_time,
      b.status, b.price, b.note,
      p.person_name as customer_name, p.email as customer_email, p.phone as customer_phone, p.address as customer_address,
      f.field_name, f.location
     FROM bookings b
     LEFT JOIN person p ON b.customer_id = p.person_id
     LEFT JOIN fields f ON b.field_id = f.field_id
     WHERE b.booking_id = ?`,
    { replacements: [id] }
  );

  return booking;
};

/**
 * Update booking status
 */
export const updateBookingStatusService = async (id, status, note = '') => {
  const [[booking]] = await sequelize.query(
    'SELECT booking_id, status, note FROM bookings WHERE booking_id = ?',
    { replacements: [id] }
  );
  
  if (!booking) {
    throw new Error('Booking not found');
  }

  let updatedNote = booking.note || '';
  if (note) {
    updatedNote = updatedNote ? `${updatedNote}\n${note}` : note;
  }

  await sequelize.query(
    'UPDATE bookings SET status = ?, note = ? WHERE booking_id = ?',
    { replacements: [status, updatedNote, id] }
  );

  const [[updatedBooking]] = await sequelize.query(
    'SELECT * FROM bookings WHERE booking_id = ?',
    { replacements: [id] }
  );

  return updatedBooking;
};

/**
 * Cancel booking
 */
export const cancelBookingService = async (id, reason) => {
  const [[booking]] = await sequelize.query(
    'SELECT booking_id, status, note FROM bookings WHERE booking_id = ?',
    { replacements: [id] }
  );
  
  if (!booking) {
    throw new Error('Booking not found');
  }

  if (booking.status === 'completed') {
    throw new Error('Cannot cancel completed booking');
  }

  const cancelNote = booking.note 
    ? `${booking.note}\nCancellation reason: ${reason}` 
    : `Cancellation reason: ${reason}`;

  await sequelize.query(
    "UPDATE bookings SET status = 'cancelled', note = ? WHERE booking_id = ?",
    { replacements: [cancelNote, id] }
  );

  const [[updatedBooking]] = await sequelize.query(
    'SELECT * FROM bookings WHERE booking_id = ?',
    { replacements: [id] }
  );

  return updatedBooking;
};

/**
 * Get booking statistics
 */
export const getBookingStatsService = async () => {
  const [[{ total }]] = await sequelize.query(
    'SELECT COUNT(*) as total FROM bookings'
  );
  
  const [[{ pending }]] = await sequelize.query(
    "SELECT COUNT(*) as pending FROM bookings WHERE status = 'pending'"
  );
  
  const [[{ confirmed }]] = await sequelize.query(
    "SELECT COUNT(*) as confirmed FROM bookings WHERE status = 'confirmed'"
  );
  
  const [[{ completed }]] = await sequelize.query(
    "SELECT COUNT(*) as completed FROM bookings WHERE status = 'completed'"
  );
  
  const [[{ cancelled }]] = await sequelize.query(
    "SELECT COUNT(*) as cancelled FROM bookings WHERE status = 'cancelled'"
  );

  // Today's bookings
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);

  const [[{ todayCount }]] = await sequelize.query(
    'SELECT COUNT(*) as todayCount FROM bookings WHERE start_time >= ? AND start_time < ?',
    { replacements: [today, tomorrow] }
  );

  return {
    total: parseInt(total),
    pending: parseInt(pending),
    confirmed: parseInt(confirmed),
    completed: parseInt(completed),
    cancelled: parseInt(cancelled),
    today: parseInt(todayCount)
  };
};

/**
 * Get bookings by date range
 */
export const getBookingsByDateRangeService = async (startDate, endDate) => {
  const [bookings] = await sequelize.query(
    `SELECT 
      b.booking_id, b.customer_id, b.field_id, b.start_time, b.end_time,
      b.status, b.price, b.note,
      p.person_name as customer_name, p.phone as customer_phone,
      f.field_name
     FROM bookings b
     LEFT JOIN person p ON b.customer_id = p.person_id
     LEFT JOIN fields f ON b.field_id = f.field_id
     WHERE b.start_time BETWEEN ? AND ?
     ORDER BY b.start_time ASC`,
    { replacements: [startDate, endDate] }
  );

  return bookings;
};
