import {
  getManagerBookingsService,
  getManagerBookingByIdService,
  updateBookingStatusService
} from '../../services/manager/bookingService.js';

/**
 * Get all bookings for manager's fields
 * GET /api/manager/bookings?status=pending&fieldId=1
 */
export const listBookings = async (req, res) => {
  try {
    const managerId = req.user.id;
    const { status, fieldId, startDate, endDate } = req.query;
    
    const bookings = await getManagerBookingsService(managerId, {
      status,
      fieldId,
      startDate,
      endDate
    });

    res.json(bookings);
  } catch (err) {
    console.error('Error fetching bookings:', err);
    res.status(500).json({ message: 'Server error', error: err.message });
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
      return res.status(404).json({ message: 'Booking not found or unauthorized' });
    }
    
    res.json(booking);
  } catch (err) {
    console.error('Error fetching booking:', err);
    res.status(500).json({ message: 'Server error', error: err.message });
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
    
    await updateBookingStatusService(managerId, id, 'confirmed');

    res.json({ message: 'Booking approved successfully' });
  } catch (err) {
    console.error('Error approving booking:', err);
    res.status(500).json({ message: 'Server error', error: err.message });
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
    
    const note = reason ? `Rejected: ${reason}` : 'Rejected by manager';
    await updateBookingStatusService(managerId, id, 'rejected', note);

    res.json({ message: 'Booking rejected successfully' });
  } catch (err) {
    console.error('Error rejecting booking:', err);
    res.status(500).json({ message: 'Server error', error: err.message });
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
    
    await updateBookingStatusService(managerId, id, 'completed');

    res.json({ message: 'Booking marked as completed' });
  } catch (err) {
    console.error('Error completing booking:', err);
    res.status(500).json({ message: 'Server error', error: err.message });
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
    
    const note = reason ? `Cancelled: ${reason}` : 'Cancelled by manager';
    await updateBookingStatusService(managerId, id, 'cancelled', note);

    res.json({ message: 'Booking cancelled successfully' });
  } catch (err) {
    console.error('Error cancelling booking:', err);
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};
