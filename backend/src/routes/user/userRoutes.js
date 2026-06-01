import { Router } from "express";
import { ping } from "../../controllers/user/userController.js";
import {
  listFields,
  listNearbyFields,
  searchFields,
  getField,
  createBooking,
  getBooking,
  updateBooking,
  getBookingHistory,
  getFieldBookings,
  getFieldGrid,
} from "../../controllers/user/fieldController.js";
import {
  getReviews,
  createReview,
  getReviewStats,
  uploadImages,
} from "../../controllers/user/reviewController.js";
import {
  uploadReviewImages,
  handleUploadErrors,
} from "../../middleware/upload.js";
import { requireAuth } from "../../middleware/authMiddleware.js";
import {
  listNotifications,
  getNotificationDetail,
  markNotificationRead,
  markAllNotificationsRead,
} from "../../controllers/user/notificationController.js";
import { getInbox } from "../../controllers/user/inboxController.js";
import {
  createConversation,
  listConversations,
  getConversationMessages,
  sendConversationMessage,
} from "../../controllers/user/conversationController.js";

const r = Router();

r.get("/ping", ping);

r.get("/fields", listFields);
r.get("/fields/nearby", listNearbyFields);
r.get("/fields/search", searchFields);
r.get("/fields/:id", getField);
r.get("/fields/:id/bookings", getFieldBookings);
r.get("/fields/:id/grid", getFieldGrid);

r.get("/bookings/history", getBookingHistory);
r.post("/bookings", requireAuth, createBooking);
r.get("/bookings/:id", requireAuth, getBooking);
r.put("/bookings/:id", updateBooking);

r.post(
  "/reviews/upload",
  requireAuth,
  uploadReviewImages,
  handleUploadErrors,
  uploadImages,
);
r.get("/reviews", getReviews);
r.post("/reviews", requireAuth, createReview);
r.get("/reviews/stats/:field_id", getReviewStats);

r.get("/notifications", requireAuth, listNotifications);
r.get("/notifications/:id", requireAuth, getNotificationDetail);
r.patch("/notifications/:id/read", requireAuth, markNotificationRead);
r.patch("/notifications/read-all", requireAuth, markAllNotificationsRead);

r.get("/inbox", requireAuth, getInbox);

r.post("/conversations", requireAuth, createConversation);
r.get("/conversations", requireAuth, listConversations);
r.get(
  "/conversations/:conversationId/messages",
  requireAuth,
  getConversationMessages,
);
r.post(
  "/conversations/:conversationId/messages",
  requireAuth,
  sendConversationMessage,
);

export default r;
