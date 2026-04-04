import { Router } from "express";
import { 
  listBookings, 
  getBookingById,
  approveBooking, 
  rejectBooking,
  completeBooking,
  cancelBooking
} from "../../controllers/manager/bookingController.js";
import { 
  getAllFields, 
  getFieldById,
  createField,
  updateField,
  deleteField,
  updateFieldStatus,
  getFieldStats
} from "../../controllers/manager/fieldController.js";
import {
  getDashboardStats,
  getRevenueByDateRange,
  getMonthlyRevenue
} from "../../controllers/manager/dashboardController.js";
import { requireAuth } from "../../middleware/authMiddleware.js";
import { requireRole } from "../../middleware/roleMiddleware.js";

const r = Router();

// Apply auth middleware to all manager routes
r.use(requireAuth);
r.use(requireRole('manager'));

// Dashboard routes
r.get('/dashboard/stats', getDashboardStats);
r.get('/dashboard/revenue', getRevenueByDateRange);
r.get('/dashboard/monthly-revenue', getMonthlyRevenue);

// Booking management
r.get('/bookings', listBookings);
r.get('/bookings/:id', getBookingById);
r.put('/bookings/:id/approve', approveBooking);
r.put('/bookings/:id/reject', rejectBooking);
r.put('/bookings/:id/complete', completeBooking);
r.put('/bookings/:id/cancel', cancelBooking);

// Field management
r.get('/fields', getAllFields);
r.post('/fields', createField);
r.get('/fields/:id', getFieldById);
r.put('/fields/:id', updateField);
r.delete('/fields/:id', deleteField);
r.put('/fields/:id/status', updateFieldStatus);
r.get('/fields/:id/stats', getFieldStats);

export default r;