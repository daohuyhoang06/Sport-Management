import sequelize from '../../config/database.js';

const ACTIVE_DASHBOARD_BOOKING_CONDITION = `
  (
    b.status IN ('confirmed', 'approved', 'completed')
    OR (
      b.status = 'pending'
      AND (b.pending_expires_at IS NULL OR b.pending_expires_at > NOW())
    )
  )
`;

const REVENUE_BOOKING_STATUS_CONDITION = `
  b.status IN ('confirmed', 'approved', 'completed')
`;

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

    // Get today's active bookings
    const [todayStats] = await sequelize.query(`
      SELECT COUNT(*) as todayBookings
      FROM bookings b
      INNER JOIN fields f ON b.field_id = f.field_id
      WHERE f.manager_id = ?
        AND DATE(b.start_time) = CURRENT_DATE
        AND ${ACTIVE_DASHBOARD_BOOKING_CONDITION}
    `, { replacements: [managerId] });

    // Count active courts and sum the actual operating minutes for each court.
    const [courtStats] = await sequelize.query(`
      SELECT
        COUNT(*) as activeCourts,
        COALESCE(SUM(
          CASE
            WHEN COALESCE(f.open_time, '06:00:00') = COALESCE(f.close_time, '22:00:00') THEN 0
            WHEN TIME(COALESCE(f.close_time, '22:00:00')) > TIME(COALESCE(f.open_time, '06:00:00'))
              THEN TIMESTAMPDIFF(
                MINUTE,
                TIMESTAMP(CURDATE(), TIME(COALESCE(f.open_time, '06:00:00'))),
                TIMESTAMP(CURDATE(), TIME(COALESCE(f.close_time, '22:00:00')))
              )
            ELSE TIMESTAMPDIFF(
              MINUTE,
              TIMESTAMP(CURDATE(), TIME(COALESCE(f.open_time, '06:00:00'))),
              DATE_ADD(
                TIMESTAMP(CURDATE(), TIME(COALESCE(f.close_time, '22:00:00'))),
                INTERVAL 1 DAY
              )
            )
          END
        ), 0) as totalAvailableMinutes
      FROM field_courts fc
      INNER JOIN fields f ON fc.field_id = f.field_id
      WHERE f.manager_id = ?
        AND f.status = 'active'
        AND fc.status = 'active'
    `, { replacements: [managerId] });

    // Sum booked minutes today from active bookings only.
    const [bookedStats] = await sequelize.query(`
      SELECT COALESCE(SUM(TIMESTAMPDIFF(MINUTE, b.start_time, b.end_time)), 0) as bookedMinutes
      FROM bookings b
      INNER JOIN fields f ON b.field_id = f.field_id
      WHERE f.manager_id = ?
        AND DATE(b.start_time) = CURDATE()
        AND ${ACTIVE_DASHBOARD_BOOKING_CONDITION}
    `, { replacements: [managerId] });

    const totalAvailableMinutes = Number(courtStats[0].totalAvailableMinutes) || 0;
    const bookedMinutes = Number(bookedStats[0].bookedMinutes) || 0;
    const occupancyPercent = totalAvailableMinutes > 0
      ? Math.min(100, Math.round(bookedMinutes / totalAvailableMinutes * 100))
      : 0;

    // Revenue on the overview is based on bookings scheduled for the period.
    const [revenueStats] = await sequelize.query(`
      SELECT
        COALESCE(SUM(b.price), 0) as totalRevenue,
        COALESCE(SUM(CASE WHEN MONTH(b.start_time) = MONTH(CURRENT_DATE)
                          AND YEAR(b.start_time) = YEAR(CURRENT_DATE)
                          THEN b.price ELSE 0 END), 0) as monthlyRevenue,
        COALESCE(SUM(CASE WHEN DATE(b.start_time) = CURRENT_DATE
                          THEN b.price ELSE 0 END), 0) as todayRevenue,
        COALESCE(SUM(CASE WHEN DATE(b.start_time) = DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)
                          THEN b.price ELSE 0 END), 0) as yesterdayRevenue
      FROM bookings b
      INNER JOIN fields f ON b.field_id = f.field_id
      WHERE f.manager_id = ?
      AND ${REVENUE_BOOKING_STATUS_CONDITION}
    `, { replacements: [managerId] });

    // Get top performing field by revenue
    const [topFieldData] = await sequelize.query(`
      SELECT f.field_name, COALESCE(SUM(b.price), 0) as revenue
      FROM fields f
      LEFT JOIN bookings b ON b.field_id = f.field_id
        AND ${REVENUE_BOOKING_STATUS_CONDITION}
      WHERE f.manager_id = ?
      GROUP BY f.field_id, f.field_name
      ORDER BY revenue DESC
      LIMIT 1
    `, { replacements: [managerId] });

    return {
      totalFields: Number(fieldStats[0].totalFields) || 0,
      activeFields: Number(fieldStats[0].activeFields) || 0,
      totalBookings: Number(bookingStats[0].totalBookings) || 0,
      pendingBookings: Number(bookingStats[0].pendingBookings) || 0,
      confirmedBookings: Number(bookingStats[0].confirmedBookings) || 0,
      completedBookings: Number(bookingStats[0].completedBookings) || 0,
      cancelledBookings: Number(bookingStats[0].cancelledBookings) || 0,
      rejectedBookings: Number(bookingStats[0].rejectedBookings) || 0,
      todayBookings: Number(todayStats[0].todayBookings) || 0,
      todayOccupancyPercent: occupancyPercent,
      totalRevenue: parseFloat(revenueStats[0].totalRevenue) || 0,
      monthlyRevenue: parseFloat(revenueStats[0].monthlyRevenue) || 0,
      todayRevenue: parseFloat(revenueStats[0].todayRevenue) || 0,
      yesterdayRevenue: parseFloat(revenueStats[0].yesterdayRevenue) || 0,
      topFieldName: topFieldData[0]?.field_name || null,
      topFieldRevenue: parseFloat(topFieldData[0]?.revenue) || 0
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
 * Get upcoming bookings for manager's fields (pending/confirmed, future start_time)
 */
export const getUpcomingBookingsService = async (managerId, limit = 5) => {
  try {
    const [bookings] = await sequelize.query(`
      SELECT
        b.booking_id,
        b.start_time,
        b.end_time,
        b.price,
        b.status,
        b.note,
        p.person_name as customer_name,
        p.phone as customer_phone,
        f.field_name,
        fc.court_code,
        fc.court_name
      FROM bookings b
      INNER JOIN fields f ON b.field_id = f.field_id
      LEFT JOIN field_courts fc ON b.court_id = fc.court_id
      LEFT JOIN person p ON b.customer_id = p.person_id
      WHERE f.manager_id = ?
        AND b.status IN ('pending', 'confirmed', 'approved')
        AND b.start_time > NOW()
      ORDER BY b.start_time ASC
      LIMIT ?
    `, { replacements: [managerId, limit] });
    return bookings;
  } catch (error) {
    console.error('Error in getUpcomingBookingsService:', error);
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

/**
 * Get revenue trend data points for a specific period
 * period: 'day' | 'week' | 'month' | 'year'
 */
export const getRevenueTrendService = async (managerId, period) => {
  try {
    let result = [];

    if (period === 'day') {
      // 5 time bands: 6-9h, 9-12h, 12-15h, 15-18h, 18-22h
      const bands = [
        { label: '6-9h',   minH: 6,  maxH: 9  },
        { label: '9-12h',  minH: 9,  maxH: 12 },
        { label: '12-15h', minH: 12, maxH: 15 },
        { label: '15-18h', minH: 15, maxH: 18 },
        { label: '18-22h', minH: 18, maxH: 22 },
      ];
      const [rows] = await sequelize.query(`
        SELECT
          CASE
            WHEN HOUR(b.start_time) < 9  THEN '6-9h'
            WHEN HOUR(b.start_time) < 12 THEN '9-12h'
            WHEN HOUR(b.start_time) < 15 THEN '12-15h'
            WHEN HOUR(b.start_time) < 18 THEN '15-18h'
            ELSE '18-22h'
          END AS label,
          COALESCE(SUM(b.price), 0) AS revenue
        FROM bookings b
        INNER JOIN fields f ON b.field_id = f.field_id
        WHERE f.manager_id = ?
          AND DATE(b.start_time) = CURRENT_DATE
          AND b.status IN ('confirmed', 'completed')
          AND HOUR(b.start_time) BETWEEN 6 AND 21
        GROUP BY label
      `, { replacements: [managerId] });
      result = bands.map(b => {
        const found = rows.find(r => r.label === b.label);
        return { label: b.label, revenue: found ? parseFloat(found.revenue) : 0 };
      });

    } else if (period === 'week') {
      // Mon-Sun of the current week
      const [rows] = await sequelize.query(`
        SELECT
          DAYOFWEEK(DATE(b.start_time)) AS dow,
          COALESCE(SUM(b.price), 0) AS revenue
        FROM bookings b
        INNER JOIN fields f ON b.field_id = f.field_id
        WHERE f.manager_id = ?
          AND DATE(b.start_time) >= DATE_SUB(CURRENT_DATE, INTERVAL WEEKDAY(CURRENT_DATE) DAY)
          AND DATE(b.start_time) <= DATE_ADD(DATE_SUB(CURRENT_DATE, INTERVAL WEEKDAY(CURRENT_DATE) DAY), INTERVAL 6 DAY)
          AND b.status IN ('confirmed', 'completed')
        GROUP BY DAYOFWEEK(DATE(b.start_time))
      `, { replacements: [managerId] });
      // MySQL DAYOFWEEK: 1=Sun,2=Mon,3=Tue,4=Wed,5=Thu,6=Fri,7=Sat
      const weekOrder = [
        { dow: 2, label: 'T2' },
        { dow: 3, label: 'T3' },
        { dow: 4, label: 'T4' },
        { dow: 5, label: 'T5' },
        { dow: 6, label: 'T6' },
        { dow: 7, label: 'T7' },
        { dow: 1, label: 'CN' },
      ];
      result = weekOrder.map(d => {
        const found = rows.find(r => Number(r.dow) === d.dow);
        return { label: d.label, revenue: found ? parseFloat(found.revenue) : 0 };
      });

    } else if (period === 'month') {
      // 5 weeks of the current month
      const [rows] = await sequelize.query(`
        SELECT
          LEAST(CEIL(DAY(b.start_time) / 7), 5) AS week_num,
          COALESCE(SUM(b.price), 0) AS revenue
        FROM bookings b
        INNER JOIN fields f ON b.field_id = f.field_id
        WHERE f.manager_id = ?
          AND MONTH(b.start_time) = MONTH(CURRENT_DATE)
          AND YEAR(b.start_time) = YEAR(CURRENT_DATE)
          AND b.status IN ('confirmed', 'completed')
        GROUP BY week_num
        ORDER BY week_num
      `, { replacements: [managerId] });
      result = [1, 2, 3, 4, 5].map(w => {
        const found = rows.find(r => Number(r.week_num) === w);
        return { label: `Tuần ${w}`, revenue: found ? parseFloat(found.revenue) : 0 };
      });

    } else {
      // year - 12 months
      const year = new Date().getFullYear();
      const [rows] = await sequelize.query(`
        SELECT
          MONTH(b.start_time) AS month,
          COALESCE(SUM(b.price), 0) AS revenue
        FROM bookings b
        INNER JOIN fields f ON b.field_id = f.field_id
        WHERE f.manager_id = ?
          AND YEAR(b.start_time) = ?
          AND b.status IN ('confirmed', 'completed')
        GROUP BY MONTH(b.start_time)
        ORDER BY month
      `, { replacements: [managerId, year] });
      result = Array.from({ length: 12 }, (_, i) => {
        const found = rows.find(r => Number(r.month) === i + 1);
        return { label: `T${i + 1}`, revenue: found ? parseFloat(found.revenue) : 0 };
      });
    }

    return result;
  } catch (error) {
    console.error('Error in getRevenueTrendService:', error);
    throw error;
  }
};
