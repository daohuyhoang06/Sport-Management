import sequelize from '../../config/database.js';

/**
 * Get dashboard statistics
 */
export const getDashboardStatsService = async () => {
  try {
    // User stats
    const [userStats] = await sequelize.query(
      `SELECT 
        COUNT(*) as totalUsers,
        SUM(CASE WHEN role = 'user' THEN 1 ELSE 0 END) as regularUsers,
        SUM(CASE WHEN role = 'manager' THEN 1 ELSE 0 END) as totalManagers,
        SUM(CASE WHEN role = 'admin' THEN 1 ELSE 0 END) as totalAdmins,
        SUM(CASE WHEN role = 'user' THEN 1 ELSE 0 END) as activeUsers
      FROM person
      WHERE role IN ('user', 'manager', 'admin')`
    );

    // Field stats with all statuses
    const [fieldStats] = await sequelize.query(
      `SELECT 
        COUNT(*) as totalFields,
        SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) as activeFields,
        SUM(CASE WHEN status = 'maintenance' THEN 1 ELSE 0 END) as maintenanceFields,
        SUM(CASE WHEN status = 'inactive' THEN 1 ELSE 0 END) as inactiveFields
      FROM fields`
    );

    // Booking stats by status
    const [bookingStats] = await sequelize.query(
      `SELECT 
        COUNT(*) as totalBookings,
        SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) as pendingBookings,
        SUM(CASE WHEN status = 'confirmed' THEN 1 ELSE 0 END) as confirmedBookings,
        SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completedBookings,
        SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END) as cancelledBookings,
        SUM(CASE WHEN status = 'rejected' THEN 1 ELSE 0 END) as rejectedBookings
      FROM bookings`
    );

    // Today's bookings
    const [todayStats] = await sequelize.query(
      `SELECT COUNT(*) as todayBookings
      FROM bookings
      WHERE DATE(start_time) = CURRENT_DATE`
    );

    // Revenue stats - only from confirmed and completed bookings
    const [revenueStats] = await sequelize.query(
      `SELECT 
        COALESCE(SUM(CASE WHEN status IN ('confirmed', 'completed') THEN price ELSE 0 END), 0) as totalRevenue,
        COALESCE(SUM(CASE WHEN status IN ('confirmed', 'completed') AND EXTRACT(MONTH FROM start_time) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM start_time) = EXTRACT(YEAR FROM CURRENT_DATE) THEN price ELSE 0 END), 0) as monthlyRevenue
      FROM bookings`
    );

    return {
      totalUsers: parseInt(userStats[0]?.totalusers || 0),
      regularUsers: parseInt(userStats[0]?.regularusers || 0),
      totalManagers: parseInt(userStats[0]?.totalmanagers || 0),
      totalAdmins: parseInt(userStats[0]?.totaladmins || 0),
      activeUsers: parseInt(userStats[0]?.activeusers || 0),
      totalFields: parseInt(fieldStats[0]?.totalfields || 0),
      activeFields: parseInt(fieldStats[0]?.activefields || 0),
      maintenanceFields: parseInt(fieldStats[0]?.maintenancefields || 0),
      inactiveFields: parseInt(fieldStats[0]?.inactivefields || 0),
      totalBookings: parseInt(bookingStats[0]?.totalbookings || 0),
      pendingBookings: parseInt(bookingStats[0]?.pendingbookings || 0),
      confirmedBookings: parseInt(bookingStats[0]?.confirmedbookings || 0),
      completedBookings: parseInt(bookingStats[0]?.completedbookings || 0),
      cancelledBookings: parseInt(bookingStats[0]?.cancelledbookings || 0),
      rejectedBookings: parseInt(bookingStats[0]?.rejectedbookings || 0),
      todayBookings: parseInt(todayStats[0]?.todaybookings || 0),
      totalRevenue: parseFloat(revenueStats[0]?.totalrevenue || 0),
      monthlyRevenue: parseFloat(revenueStats[0]?.monthlyrevenue || 0)
    };
  } catch (error) {
    console.error('Error in getDashboardStatsService:', error);
    throw error;
  }
};

/**
 * Get revenue by date range
 */
export const getRevenuByDateRangeService = async (startDate, endDate) => {
  try {
    const [bookings] = await sequelize.query(
      `SELECT 
        b.booking_id,
        b.start_time,
        b.end_time,
        b.price,
        b.status,
        f.field_name,
        p.person_name as customer_name
      FROM bookings b
      LEFT JOIN fields f ON b.field_id = f.field_id
      LEFT JOIN person p ON b.customer_id = p.person_id
      WHERE b.status IN ('confirmed', 'completed')
        AND DATE(b.start_time) BETWEEN ? AND ?
      ORDER BY b.start_time DESC`,
      { replacements: [startDate, endDate] }
    );

    const totalRevenue = bookings.reduce((sum, booking) => sum + parseFloat(booking.price || 0), 0);

    return {
      bookings: bookings,
      totalRevenue: totalRevenue,
      totalBookings: bookings.length
    };
  } catch (error) {
    console.error('Error in getRevenuByDateRangeService:', error);
    throw error;
  }
};

/**
 * Get revenue by field
 */
export const getRevenueByFieldService = async (fieldId, startDate, endDate) => {
  try {
    let query = `
      SELECT 
        b.booking_id,
        b.start_time,
        b.end_time,
        b.price,
        b.status,
        p.person_name as customer_name
      FROM bookings b
      LEFT JOIN person p ON b.customer_id = p.person_id
      WHERE b.field_id = ?
        AND b.status IN ('confirmed', 'completed')
    `;

    const replacements = [fieldId];

    if (startDate && endDate) {
      query += ' AND DATE(b.start_time) BETWEEN ? AND ?';
      replacements.push(startDate, endDate);
    }

    query += ' ORDER BY b.start_time DESC';

    const [bookings] = await sequelize.query(query, { replacements });

    const totalRevenue = bookings.reduce((sum, booking) => sum + parseFloat(booking.price || 0), 0);

    return {
      bookings: bookings,
      totalRevenue: totalRevenue,
      count: bookings.length
    };
  } catch (error) {
    console.error('Error in getRevenueByFieldService:', error);
    throw error;
  }
};

/**
 * Get monthly revenue statistics
 */
export const getMonthlyRevenueStatsService = async (year) => {
  try {
    const [results] = await sequelize.query(
      `SELECT 
        EXTRACT(MONTH FROM start_time) as month,
        SUM(price) as revenue,
        COUNT(*) as count
      FROM bookings
      WHERE status IN ('confirmed', 'completed')
        AND EXTRACT(YEAR FROM start_time) = ?
      GROUP BY EXTRACT(MONTH FROM start_time)
      ORDER BY EXTRACT(MONTH FROM start_time) ASC`,
      { replacements: [year] }
    );

    // Fill in missing months with 0
    const monthlyData = Array.from({ length: 12 }, (_, i) => ({
      month: i + 1,
      revenue: 0,
      count: 0
    }));

    results.forEach(result => {
      const monthIndex = result.month - 1;
      monthlyData[monthIndex] = {
        month: result.month,
        revenue: parseFloat(result.revenue || 0),
        count: parseInt(result.count || 0)
      };
    });

    return monthlyData;
  } catch (error) {
    console.error('Error in getMonthlyRevenueStatsService:', error);
    throw error;
  }
};
