import sequelize from '../../config/database.js';

/**
 * Get dashboard statistics for manager
 * Only shows data for fields managed by this manager
 */
export const getDashboardStatsService = async (managerId) => {
  try {
    // Get field stats for this manager
    const [fieldStats] = await sequelize.query(`
      SELECT 
        COUNT(*) as totalFields,
        SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) as activeFields
      FROM fields
      WHERE manager_id = ?
    `, { replacements: [managerId] });

    // Get booking stats for this manager's fields
    const [bookingStats] = await sequelize.query(`
      SELECT 
        COUNT(*) as totalBookings,
        SUM(CASE WHEN b.status = 'pending' THEN 1 ELSE 0 END) as pendingBookings,
        SUM(CASE WHEN b.status = 'confirmed' THEN 1 ELSE 0 END) as confirmedBookings,
        SUM(CASE WHEN b.status = 'completed' THEN 1 ELSE 0 END) as completedBookings,
        SUM(CASE WHEN b.status = 'cancelled' THEN 1 ELSE 0 END) as cancelledBookings,
        SUM(CASE WHEN b.status = 'rejected' THEN 1 ELSE 0 END) as rejectedBookings
      FROM bookings b
      INNER JOIN fields f ON b.field_id = f.field_id
      WHERE f.manager_id = ?
    `, { replacements: [managerId] });

    // Get today's bookings
    const [todayStats] = await sequelize.query(`
      SELECT COUNT(*) as todayBookings
      FROM bookings b
      INNER JOIN fields f ON b.field_id = f.field_id
      WHERE f.manager_id = ?
      AND DATE(b.start_time) = CURRENT_DATE
    `, { replacements: [managerId] });

    // Get revenue stats (only confirmed and completed)
    const [revenueStats] = await sequelize.query(`
      SELECT 
        COALESCE(SUM(b.price), 0) as totalRevenue,
        COALESCE(SUM(CASE WHEN EXTRACT(MONTH FROM b.start_time) = EXTRACT(MONTH FROM CURRENT_DATE) 
                          AND EXTRACT(YEAR FROM b.start_time) = EXTRACT(YEAR FROM CURRENT_DATE) 
                          THEN b.price ELSE 0 END), 0) as monthlyRevenue
      FROM bookings b
      INNER JOIN fields f ON b.field_id = f.field_id
      WHERE f.manager_id = ?
      AND b.status IN ('confirmed', 'completed')
    `, { replacements: [managerId] });

    return {
      totalFields: Number(fieldStats[0].totalfields) || 0,
      activeFields: Number(fieldStats[0].activefields) || 0,
      totalBookings: Number(bookingStats[0].totalbookings) || 0,
      pendingBookings: Number(bookingStats[0].pendingbookings) || 0,
      confirmedBookings: Number(bookingStats[0].confirmedbookings) || 0,
      completedBookings: Number(bookingStats[0].completedbookings) || 0,
      cancelledBookings: Number(bookingStats[0].cancelledbookings) || 0,
      rejectedBookings: Number(bookingStats[0].rejectedbookings) || 0,
      todayBookings: Number(todayStats[0].todaybookings) || 0,
      totalRevenue: parseFloat(revenueStats[0].totalrevenue) || 0,
      monthlyRevenue: parseFloat(revenueStats[0].monthlyrevenue) || 0
    };
  } catch (error) {
    console.error('Error in getDashboardStatsService:', error);
    throw error;
  }
};

/**
 * Get revenue by date range for manager's fields
 */
export const getRevenueByDateRangeService = async (managerId, startDate, endDate) => {
  try {
    const [bookings] = await sequelize.query(`
      SELECT 
        b.booking_id,
        b.start_time,
        b.end_time,
        b.price,
        b.status,
        f.field_name,
        f.location,
        p.person_name as customer_name
      FROM bookings b
      INNER JOIN fields f ON b.field_id = f.field_id
      LEFT JOIN person p ON b.customer_id = p.person_id
      WHERE f.manager_id = ?
      AND b.status IN ('confirmed', 'completed')
      AND DATE(b.start_time) >= ?
      AND DATE(b.start_time) <= ?
      ORDER BY b.start_time DESC
    `, { replacements: [managerId, startDate, endDate] });

    return bookings;
  } catch (error) {
    console.error('Error in getRevenueByDateRangeService:', error);
    throw error;
  }
};

/**
 * Get monthly revenue statistics for manager
 */
export const getMonthlyRevenueStatsService = async (managerId, year) => {
  try {
    const [monthlyData] = await sequelize.query(`
      SELECT 
        EXTRACT(MONTH FROM b.start_time) as month,
        COALESCE(SUM(b.price), 0) as revenue,
        COUNT(b.booking_id) as bookings
      FROM bookings b
      INNER JOIN fields f ON b.field_id = f.field_id
      WHERE f.manager_id = ?
      AND EXTRACT(YEAR FROM b.start_time) = ?
      AND b.status IN ('confirmed', 'completed')
      GROUP BY EXTRACT(MONTH FROM b.start_time)
      ORDER BY month
    `, { replacements: [managerId, year] });

    // Fill missing months with 0
    const result = [];
    for (let i = 1; i <= 12; i++) {
      const found = monthlyData.find(d => Number(d.month) === i);
      result.push({
        month: i,
        revenue: found ? parseFloat(found.revenue) : 0,
        bookings: found ? Number(found.bookings) : 0
      });
    }

    return result;
  } catch (error) {
    console.error('Error in getMonthlyRevenueStatsService:', error);
    throw error;
  }
};
