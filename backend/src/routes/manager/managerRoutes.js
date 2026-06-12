import { Router } from "express";
import {
  listBookings,
  createBooking,
  getBookingById,
  getBookingHistory,
  approveBooking,
  rejectBooking,
  completeBooking,
  cancelBooking,
  rescheduleBooking,
  getBookingByCheckInCode,
  confirmBookingCheckIn,
  getCourtAvailability,
} from "../../controllers/manager/bookingController.js";
import {
  listBlockedSlots,
  createBlockedSlot,
  deleteBlockedSlot,
} from "../../controllers/manager/blockedSlotController.js";
import {
  getAllFields,
  getFieldById,
  createField,
  updateField,
  deleteField,
  updateFieldStatus,
  getFieldStats,
  uploadFieldImageController
} from "../../controllers/manager/fieldController.js";
import {
  createFieldCourt,
  createFieldPolicy,
  createFieldServiceItem,
  deleteFieldCourt,
  deleteFieldPolicy,
  deleteFieldServiceItem,
  getFieldConfig,
  listFieldCourts,
  listFieldPolicies,
  listFieldServices,
  reorderFieldCourts,
  updateFieldCourt,
  updateFieldPolicy,
  updateFieldServiceItem,
} from "../../controllers/manager/fieldConfigController.js";
import {
  getDashboardStats,
  getRevenueByDateRange,
  getMonthlyRevenue,
  getUpcomingBookings,
  getRevenueTrend
} from "../../controllers/manager/dashboardController.js";
import { getProfile, updateProfile } from "../../controllers/manager/profileController.js";
import { managerStartChat } from "../../controllers/chatController.js";
import { requireAuth } from "../../middleware/authMiddleware.js";
import { requireRole } from "../../middleware/roleMiddleware.js";

const r = Router();

// Apply auth middleware to all manager routes
r.use(requireAuth);
r.use(requireRole('manager'));

// Profile
r.get('/profile', getProfile);
r.put('/profile', updateProfile);

// Dashboard routes
r.get('/dashboard/stats', getDashboardStats);
r.get('/dashboard/upcoming', getUpcomingBookings);
r.get('/dashboard/revenue', getRevenueByDateRange);
r.get('/dashboard/monthly-revenue', getMonthlyRevenue);
r.get('/dashboard/revenue-trend', getRevenueTrend);

// Booking management
r.get('/bookings', listBookings);
r.post('/bookings', createBooking);
r.get('/bookings/check-in/:code', getBookingByCheckInCode);
r.put('/bookings/check-in/:code/confirm', confirmBookingCheckIn);
r.get('/bookings/:id', getBookingById);
r.get('/bookings/:id/history', getBookingHistory);
r.put('/bookings/:id/approve', approveBooking);
r.put('/bookings/:id/reject', rejectBooking);
r.put('/bookings/:id/complete', completeBooking);
r.put('/bookings/:id/reschedule', rescheduleBooking);
r.put('/bookings/:id/cancel', cancelBooking);

// Field image upload
r.post('/upload/field-image', uploadFieldImageController);

// Field management
r.get('/fields', getAllFields);
r.post('/fields', createField);
r.get('/fields/:id', getFieldById);
r.get('/fields/:id/config', getFieldConfig);
r.put('/fields/:id', updateField);
r.patch('/fields/:id', updateField);
r.delete('/fields/:id', deleteField);
r.put('/fields/:id/status', updateFieldStatus);
r.get('/fields/:id/stats', getFieldStats);
r.get('/fields/:id/courts', listFieldCourts);
r.get('/fields/:fieldId/courts/:courtId/availability', getCourtAvailability);
r.post('/fields/:id/courts', createFieldCourt);
r.put('/fields/:id/courts/:courtId', updateFieldCourt);
r.patch('/fields/:id/courts/reorder', reorderFieldCourts);
r.delete('/fields/:id/courts/:courtId', deleteFieldCourt);
r.get('/fields/:id/services', listFieldServices);
r.post('/fields/:id/services', createFieldServiceItem);
r.put('/fields/:id/services/:serviceId', updateFieldServiceItem);
r.delete('/fields/:id/services/:serviceId', deleteFieldServiceItem);
r.get('/fields/:id/policies', listFieldPolicies);
r.post('/fields/:id/policies', createFieldPolicy);
r.put('/fields/:id/policies/:policyId', updateFieldPolicy);
r.delete('/fields/:id/policies/:policyId', deleteFieldPolicy);

// Blocked slots
r.get('/fields/:id/blocked-slots', listBlockedSlots);
r.post('/fields/:id/blocked-slots', createBlockedSlot);
r.delete('/fields/:id/blocked-slots/:slotId', deleteBlockedSlot);

// Manager chat
r.post('/chat/start', managerStartChat);

export default r;
