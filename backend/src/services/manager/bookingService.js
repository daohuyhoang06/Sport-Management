import sequelize from '../../config/database.js';

/**
 * Get all bookings for manager's fields
 */
export const getManagerBookingsService = async (managerId, filters = {}) => {
  try {
    const { status, fieldId, startDate, endDate } = filters;
    
    let whereConditions = ['f.manager_id = ?'];
    let replacements = [managerId];
    
    if (status && status !== 'all') {
      whereConditions.push('b.status = ?');
      replacements.push(status);
    }
    
    if (fieldId) {
      whereConditions.push('b.field_id = ?');
      replacements.push(fieldId);
    }
    
    if (startDate) {
      whereConditions.push('DATE(b.start_time) >= ?');
      replacements.push(startDate);
    }
    
    if (endDate) {
      whereConditions.push('DATE(b.start_time) <= ?');
      replacements.push(endDate);
    }
    
    const whereClause = whereConditions.length > 0 
      ? `WHERE ${whereConditions.join(' AND ')}` 
      : '';

    const [bookings] = await sequelize.query(`
      SELECT 
        b.booking_id,
        b.field_id,
        b.customer_id,
        b.start_time,
        b.end_time,
        b.status,
        b.price,
        b.note,
        f.field_name,
        f.location,
        p.person_name as customer_name,
        p.email as customer_email,
        p.phone as customer_phone
      FROM bookings b
      INNER JOIN fields f ON b.field_id = f.field_id
      LEFT JOIN person p ON b.customer_id = p.person_id
      ${whereClause}
      ORDER BY b.booking_id DESC
    `, { replacements });

    return bookings;
  } catch (error) {
    console.error('Error in getManagerBookingsService:', error);
    throw error;
  }
};

/**
 * Get booking by ID (only if belongs to manager's field)
 */
export const getManagerBookingByIdService = async (managerId, bookingId) => {
  try {
    const [bookings] = await sequelize.query(`
      SELECT 
        b.booking_id,
        b.field_id,
        b.customer_id,
        b.start_time,
        b.end_time,
        b.status,
        b.price,
        b.note,
        f.field_name,
        f.location,
        f.manager_id,
        p.person_name as customer_name,
        p.email as customer_email,
        p.phone as customer_phone
      FROM bookings b
      INNER JOIN fields f ON b.field_id = f.field_id
      LEFT JOIN person p ON b.customer_id = p.person_id
      WHERE b.booking_id = ? AND f.manager_id = ?
    `, { replacements: [bookingId, managerId] });

    return bookings[0] || null;
  } catch (error) {
    console.error('Error in getManagerBookingByIdService:', error);
    throw error;
  }
};

/**
 * Update booking status
 */
export const updateBookingStatusService = async (managerId, bookingId, status, note = null) => {
  try {
    // Verify booking belongs to manager's field
    const booking = await getManagerBookingByIdService(managerId, bookingId);
    if (!booking) {
      throw new Error('Booking not found or unauthorized');
    }

    let query = `
      UPDATE bookings
      SET status = ?
      FROM fields f
      WHERE bookings.field_id = f.field_id
    `;
    let replacements = [status];

    if (note) {
      query = `
        UPDATE bookings
        SET status = ?,
            note = COALESCE(note, '') || ?
        FROM fields f
        WHERE bookings.field_id = f.field_id
      `;
      replacements = [status, ` | ${note}`];
    }

    query += ` AND bookings.booking_id = ? AND f.manager_id = ?`;
    replacements.push(bookingId, managerId);

    await sequelize.query(query, { replacements });

    return { success: true };
  } catch (error) {
    console.error('Error in updateBookingStatusService:', error);
    throw error;
  }
};
