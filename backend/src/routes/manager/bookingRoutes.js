import express from 'express';
import { requireAuth } from '../../middleware/authMiddleware.js';
import { listBookings, approveBooking, rejectBooking } from '../../controllers/manager/bookingController.js';

const router = express.Router();

// Booking routes here

export default router;