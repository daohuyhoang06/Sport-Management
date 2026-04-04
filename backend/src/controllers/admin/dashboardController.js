import {
  getDashboardStatsService,
  getRevenuByDateRangeService,
  getRevenueByFieldService,
  getMonthlyRevenueStatsService
} from '../../services/admin/dashboardService.js';

/**
 * Get dashboard statistics
 */
export const getDashboard = async (req, res) => {
  try {
    const stats = await getDashboardStatsService();

    res.json({
      success: true,
      data: stats
    });
  } catch (error) {
    console.error('Error in getDashboard:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy thống kê dashboard',
      error: error.message
    });
  }
};

/**
 * Get revenue by date range
 */
export const getRevenueByDateRange = async (req, res) => {
  try {
    const { startDate, endDate } = req.query;

    if (!startDate || !endDate) {
      return res.status(400).json({
        success: false,
        message: 'Vui lòng cung cấp ngày bắt đầu và ngày kết thúc'
      });
    }

    const result = await getRevenuByDateRangeService(startDate, endDate);

    res.json({
      success: true,
      data: result
    });
  } catch (error) {
    console.error('Error in getRevenueByDateRange:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy doanh thu theo khoảng thời gian',
      error: error.message
    });
  }
};

/**
 * Get revenue by field
 */
export const getRevenueByField = async (req, res) => {
  try {
    const { fieldId } = req.params;
    const { startDate, endDate } = req.query;

    const result = await getRevenueByFieldService(fieldId, startDate, endDate);

    res.json({
      success: true,
      data: result
    });
  } catch (error) {
    console.error('Error in getRevenueByField:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy doanh thu theo sân',
      error: error.message
    });
  }
};

/**
 * Get monthly revenue statistics
 */
export const getMonthlyRevenueStats = async (req, res) => {
  try {
    const { year } = req.query;
    const currentYear = year || new Date().getFullYear();

    const result = await getMonthlyRevenueStatsService(currentYear);

    res.json({
      success: true,
      data: result
    });
  } catch (error) {
    console.error('Error in getMonthlyRevenueStats:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy thống kê doanh thu theo tháng',
      error: error.message
    });
  }
};
