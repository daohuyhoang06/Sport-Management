import {
  getAllBookingsService,
  getBookingByIdService,
  updateBookingStatusService,
  cancelBookingService,
  getBookingStatsService,
  getBookingsByDateRangeService
} from '../../services/admin/bookingManagementService.js';

/**
 * Get all bookings
 */
export const getAllBookings = async (req, res) => {
  try {
    const { page, limit, status, fieldId, startDate, endDate } = req.query;
    const result = await getAllBookingsService(
      { status, fieldId, startDate, endDate },
      { page, limit }
    );

    res.json({
      success: true,
      data: result
    });
  } catch (error) {
    console.error('Error in getAllBookings:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy danh sách đặt sân',
      error: error.message
    });
  }
};

/**
 * Get booking by ID
 */
export const getBookingById = async (req, res) => {
  try {
    const { id } = req.params;
    const booking = await getBookingByIdService(id);

    if (!booking) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy đặt sân'
      });
    }

    res.json({
      success: true,
      data: booking
    });
  } catch (error) {
    console.error('Error in getBookingById:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy thông tin đặt sân',
      error: error.message
    });
  }
};

/**
 * Update booking status
 */
export const updateBookingStatus = async (req, res) => {
  try {
    const { id } = req.params;
    const { status, note } = req.body;

    if (!status) {
      return res.status(400).json({
        success: false,
        message: 'Vui lòng cung cấp trạng thái'
      });
    }

    const booking = await updateBookingStatusService(id, status, note);

    res.json({
      success: true,
      message: 'Cập nhật trạng thái đặt sân thành công',
      data: booking
    });
  } catch (error) {
    console.error('Error in updateBookingStatus:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi cập nhật trạng thái đặt sân',
      error: error.message
    });
  }
};

/**
 * Cancel booking
 */
export const cancelBooking = async (req, res) => {
  try {
    const { id } = req.params;
    const { reason } = req.body;

    if (!reason) {
      return res.status(400).json({
        success: false,
        message: 'Vui lòng cung cấp lý do hủy'
      });
    }

    const booking = await cancelBookingService(id, reason);

    res.json({
      success: true,
      message: 'Hủy đặt sân thành công',
      data: booking
    });
  } catch (error) {
    console.error('Error in cancelBooking:', error);
    res.status(400).json({
      success: false,
      message: 'Lỗi khi hủy đặt sân',
      error: error.message
    });
  }
};

/**
 * Get booking statistics
 */
export const getBookingStats = async (req, res) => {
  try {
    const stats = await getBookingStatsService();

    res.json({
      success: true,
      data: stats
    });
  } catch (error) {
    console.error('Error in getBookingStats:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy thống kê đặt sân',
      error: error.message
    });
  }
};

/**
 * Get bookings by date range
 */
export const getBookingsByDateRange = async (req, res) => {
  try {
    const { startDate, endDate } = req.query;

    if (!startDate || !endDate) {
      return res.status(400).json({
        success: false,
        message: 'Vui lòng cung cấp ngày bắt đầu và ngày kết thúc'
      });
    }

    const bookings = await getBookingsByDateRangeService(startDate, endDate);

    res.json({
      success: true,
      data: bookings
    });
  } catch (error) {
    console.error('Error in getBookingsByDateRange:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi khi lấy đặt sân theo khoảng thời gian',
      error: error.message
    });
  }
};
