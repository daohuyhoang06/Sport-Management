import {
  getDashboardStatsService,
  getRevenueByDateRangeService,
  getMonthlyRevenueStatsService
} from '../../services/manager/dashboardService.js';

/**
 * Get dashboard statistics for manager
 * GET /api/manager/dashboard/stats
 */
export const getDashboardStats = async (req, res) => {
  try {
    const managerId = req.user.id;
    
    const stats = await getDashboardStatsService(managerId);
    
    res.json(stats);
  } catch (error) {
    console.error('Error in getDashboardStats:', error);
    res.status(500).json({ 
      message: 'Server error when fetching dashboard stats',
      error: error.message 
    });
  }
};

/**
 * Get revenue by date range
 * GET /api/manager/dashboard/revenue?startDate=2024-01-01&endDate=2024-12-31
 */
export const getRevenueByDateRange = async (req, res) => {
  try {
    const managerId = req.user.id;
    const { startDate, endDate } = req.query;
    
    if (!startDate || !endDate) {
      return res.status(400).json({ 
        message: 'startDate and endDate are required' 
      });
    }
    
    const bookings = await getRevenueByDateRangeService(managerId, startDate, endDate);
    
    res.json(bookings);
  } catch (error) {
    console.error('Error in getRevenueByDateRange:', error);
    res.status(500).json({ 
      message: 'Server error when fetching revenue data',
      error: error.message 
    });
  }
};

/**
 * Get monthly revenue statistics
 * GET /api/manager/dashboard/monthly-revenue?year=2024
 */
export const getMonthlyRevenue = async (req, res) => {
  try {
    const managerId = req.user.id;
    const year = req.query.year || new Date().getFullYear();
    
    const monthlyStats = await getMonthlyRevenueStatsService(managerId, year);
    
    res.json(monthlyStats);
  } catch (error) {
    console.error('Error in getMonthlyRevenue:', error);
    res.status(500).json({ 
      message: 'Server error when fetching monthly revenue',
      error: error.message 
    });
  }
};
