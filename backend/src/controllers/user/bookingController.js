import sequelize from '../../config/database.js';

export const listBookings = async (req, res) => {
  try {
    const { status } = req.query;
    
    let whereClause = {};
    if (status && status !== 'all') {
      whereClause.status = status;
    }

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
      LEFT JOIN fields f ON b.field_id = f.field_id
      LEFT JOIN person p ON b.customer_id = p.person_id
      ${status && status !== 'all' ? `WHERE b.status = '${status}'` : ''}
      ORDER BY b.booking_id DESC
    `);

    res.json(bookings);
  } catch (err) {
    console.error('Error fetching bookings:', err);
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};

export const approveBooking = async (req, res) => {
  try {
    const { id } = req.params;
    
    await sequelize.query(`
      UPDATE bookings 
      SET status = 'confirmed'
      WHERE booking_id = ?
    `, { replacements: [id] });

    res.json({ message: 'Booking approved successfully' });
  } catch (err) {
    console.error('Error approving booking:', err);
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};

export const rejectBooking = async (req, res) => {
  try {
    const { id } = req.params;
    const { reason } = req.body;
    
    const noteAppend = reason ? ` | Rejected reason: ${reason}` : '';
    
    await sequelize.query(`
      UPDATE bookings 
      SET status = 'rejected', 
          note = COALESCE(note, '') || ?
      WHERE booking_id = ?
    `, { replacements: [noteAppend, id] });

    res.json({ message: 'Booking rejected successfully' });
  } catch (err) {
    console.error('Error rejecting booking:', err);
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};

export const getBookings = async (req, res) => {
  res.json({ message: "Danh sách yêu cầu đặt sân (manager)" });
};