
import { Router } from "express";
import { ping } from "../../controllers/user/userController.js";
import { listFields, getField, createBooking, getBooking, updateBooking, getBookingHistory, getFieldBookings } from "../../controllers/user/fieldController.js";
import { getReviews, createReview, getReviewStats, uploadImages } from "../../controllers/user/reviewController.js";
import { uploadReviewImages, handleUploadErrors } from "../../middleware/upload.js";
import { requireAuth } from "../../middleware/authMiddleware.js";

const r = Router();

r.get("/ping", ping);

r.get('/fields', listFields);
r.get('/fields/:id', getField);
r.get('/fields/:id/bookings', getFieldBookings);

r.get('/bookings/history', getBookingHistory);
r.post('/bookings', requireAuth, createBooking);
r.get('/bookings/:id', getBooking);
r.put('/bookings/:id', updateBooking);

r.post('/reviews/upload', requireAuth, uploadReviewImages, handleUploadErrors, uploadImages);
r.get('/reviews', getReviews);
r.post('/reviews', requireAuth, createReview);
r.get('/reviews/stats/:fieldId', getReviewStats);

export default r;