import {
  getManagerBookingsService,
  getManagerBookingByIdService,
  updateBookingStatusService,
  createBookingService,
  getBookingHistoryService,
  getCourtAvailabilityService,
} from "../../services/manager/bookingService.js";
import {
  buildBookingShareResponse,
  getBookingShareDetailByCheckInCode,
  markBookingCheckedInByCode,
} from "../../services/bookingShareService.js";

/**
 * Manager tạo booking mới
 * POST /api/manager/bookings
 */
export const createBooking = async (req, res) => {
  try {
    const managerId = req.user.id;
    const booking = await createBookingService(managerId, req.body);
    res.status(201).json({ success: true, data: booking });
  } catch (err) {
    console.error("Error creating booking:", err);
    const status = err.message.includes('quyền') ? 403 : 400;
    res.status(status).json({ message: err.message });
  }
};

/**
 * Lấy lịch sử trạng thái của booking
 * GET /api/manager/bookings/:id/history
 */
export const getBookingHistory = async (req, res) => {
  try {
    const managerId = req.user.id;
    const { id } = req.params;
    const history = await getBookingHistoryService(managerId, id);
    res.json({ success: true, data: history });
  } catch (err) {
    console.error("Error fetching booking history:", err);
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};

/**
 * Get all bookings for manager's fields
 * GET /api/manager/bookings?status=pending&field_id=1
 */
export const listBookings = async (req, res) => {
  try {
    const managerId = req.user.id;
    const { status, field_id, startDate, endDate } = req.query;

    const bookings = await getManagerBookingsService(managerId, {
      status,
      field_id,
      startDate,
      endDate,
    });

    res.json(bookings);
  } catch (err) {
    console.error("Error fetching bookings:", err);
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

/**
 * Get booking by ID
 * GET /api/manager/bookings/:id
 */
export const getBookingById = async (req, res) => {
  try {
    const managerId = req.user.id;
    const { id } = req.params;

    const booking = await getManagerBookingByIdService(managerId, id);

    if (!booking) {
      return res
        .status(404)
        .json({ message: "Booking not found or unauthorized" });
    }

    res.json(booking);
  } catch (err) {
    console.error("Error fetching booking:", err);
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

/**
 * Approve booking
 * PUT /api/manager/bookings/:id/approve
 */
export const approveBooking = async (req, res) => {
  try {
    const managerId = req.user.id;
    const { id } = req.params;

    await updateBookingStatusService(managerId, id, "confirmed");

    res.json({ message: "Booking approved successfully" });
  } catch (err) {
    console.error("Error approving booking:", err);
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

/**
 * Reject booking
 * PUT /api/manager/bookings/:id/reject
 */
export const rejectBooking = async (req, res) => {
  try {
    const managerId = req.user.id;
    const { id } = req.params;
    const { reason } = req.body;

    const note = reason ? `Rejected: ${reason}` : "Rejected by manager";
    await updateBookingStatusService(managerId, id, "rejected", note);

    res.json({ message: "Booking rejected successfully" });
  } catch (err) {
    console.error("Error rejecting booking:", err);
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

/**
 * Complete booking
 * PUT /api/manager/bookings/:id/complete
 */
export const completeBooking = async (req, res) => {
  try {
    const managerId = req.user.id;
    const { id } = req.params;

    await updateBookingStatusService(managerId, id, "completed");

    res.json({ message: "Booking marked as completed" });
  } catch (err) {
    console.error("Error completing booking:", err);
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

/**
 * Cancel booking
 * PUT /api/manager/bookings/:id/cancel
 */
export const cancelBooking = async (req, res) => {
  try {
    const managerId = req.user.id;
    const { id } = req.params;
    const { reason } = req.body;

    const note = reason ? `Cancelled: ${reason}` : "Cancelled by manager";
    await updateBookingStatusService(managerId, id, "cancelled", note);

    res.json({ message: "Booking cancelled successfully" });
  } catch (err) {
    console.error("Error cancelling booking:", err);
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

/**
 * Get booking by check-in code
 * GET /api/manager/bookings/check-in/:code
 */
export const getBookingByCheckInCode = async (req, res) => {
  try {
    const managerId = req.user.id;
    const detail = await getBookingShareDetailByCheckInCode(
      managerId,
      req.params.code,
    );

    if (!detail) {
      return res.status(404).json({ message: "Check-in code not found" });
    }

    const publicBaseUrl =
      process.env.PUBLIC_WEB_BASE_URL ||
      `${req.protocol}://${req.get("host")}`;

    return res.json({
      success: true,
      data: buildBookingShareResponse(detail, publicBaseUrl),
    });
  } catch (err) {
    console.error("Error fetching booking by check-in code:", err);
    return res.status(500).json({ message: "Server error", error: err.message });
  }
};

/**
 * Lấy khung giờ đã đặt của sân con theo ngày
 * GET /api/manager/fields/:fieldId/courts/:courtId/availability?date=YYYY-MM-DD
 */
export const getCourtAvailability = async (req, res) => {
  try {
    const managerId = req.user.id;
    const { fieldId, courtId } = req.params;
    const { date } = req.query;
    if (!date) return res.status(400).json({ message: 'Thiếu tham số date' });
    const data = await getCourtAvailabilityService(managerId, fieldId, courtId, date);
    res.json({ success: true, data });
  } catch (err) {
    console.error('Error fetching availability:', err);
    res.status(500).json({ message: err.message });
  }
};

/**
 * Confirm booking check-in
 * PUT /api/manager/bookings/check-in/:code/confirm
 */
export const confirmBookingCheckIn = async (req, res) => {
  try {
    const managerId = req.user.id;
    const detail = await markBookingCheckedInByCode(managerId, req.params.code);

    if (!detail) {
      return res.status(404).json({ message: "Check-in code not found" });
    }

    const publicBaseUrl =
      process.env.PUBLIC_WEB_BASE_URL ||
      `${req.protocol}://${req.get("host")}`;

    return res.json({
      success: true,
      message: "Booking checked in successfully",
      data: buildBookingShareResponse(detail, publicBaseUrl),
    });
  } catch (err) {
    console.error("Error confirming booking check-in:", err);
    return res.status(500).json({ message: "Server error", error: err.message });
  }
};
