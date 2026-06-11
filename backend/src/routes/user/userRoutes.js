import { Router } from "express";
import { ping } from "../../controllers/user/userController.js";
import {
  listFields,
  listNearbyFields,
  searchFields,
  getField,
  createBooking,
  createBatchBookings,
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
  addFavoriteField,
  listFavoriteFields,
  removeFavoriteField,
} from "../../controllers/user/favoriteController.js";
import {
  uploadReviewImages,
  handleUploadErrors,
} from "../../middleware/upload.js";
import { requireAuth } from "../../middleware/authMiddleware.js";
import {
  listNotifications,
  getNotificationDetail,
  markNotificationRead,
  markBookingNotificationsRead,
  markAllNotificationsRead,
} from "../../controllers/user/notificationController.js";
import { getInbox, markInboxReadAll } from "../../controllers/user/inboxController.js";
import {
  createConversation,
  listConversations,
  getConversationMessages,
  sendConversationMessage,
  markConversationRead,
} from "../../controllers/user/conversationController.js";
import {
  acceptMatchRequestHandler,
  createMatchRequest,
  rejectMatchRequestHandler,
} from "../../controllers/user/matchmakingController.js";
import {
  registerMyDeviceToken,
  unregisterMyDeviceToken,
} from "../../controllers/user/deviceTokenController.js";

const r = Router();

r.get("/ping", ping);

r.get("/fields", listFields);
r.get("/fields/nearby", listNearbyFields);
r.get("/fields/search", searchFields);
r.get("/fields/:id", getField);
r.get("/fields/:id/bookings", getFieldBookings);
r.get("/fields/:id/grid", getFieldGrid);

r.get("/favorites", requireAuth, listFavoriteFields);
r.post("/favorites/:fieldId", requireAuth, addFavoriteField);
r.delete("/favorites/:fieldId", requireAuth, removeFavoriteField);

r.get("/bookings/history", getBookingHistory);
r.post("/bookings/batch", requireAuth, createBatchBookings);
r.post("/bookings", requireAuth, createBooking);
r.get("/bookings/:id", requireAuth, getBooking);
r.put("/bookings/:id", updateBooking);
r.post("/match-posts/:id/requests", requireAuth, createMatchRequest);
r.post("/match-requests/:id/accept", requireAuth, acceptMatchRequestHandler);
r.post("/match-requests/:id/reject", requireAuth, rejectMatchRequestHandler);

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
r.post("/device-tokens", requireAuth, registerMyDeviceToken);
r.delete("/device-tokens", requireAuth, unregisterMyDeviceToken);
r.get("/notifications/:id", requireAuth, getNotificationDetail);
r.patch("/notifications/read-all", requireAuth, markAllNotificationsRead);
r.post("/notifications/read-all", requireAuth, markAllNotificationsRead);
r.patch("/notifications/booking/:bookingId/read", requireAuth, markBookingNotificationsRead);
r.post("/notifications/booking/:bookingId/read", requireAuth, markBookingNotificationsRead);
r.patch("/notifications/:id/read", requireAuth, markNotificationRead);
r.post("/notifications/:id/read", requireAuth, markNotificationRead);

r.get("/inbox", requireAuth, getInbox);
r.patch("/inbox/read-all", requireAuth, markInboxReadAll);
r.post("/inbox/read-all", requireAuth, markInboxReadAll);

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
r.patch(
  "/conversations/:conversationId/read",
  requireAuth,
  markConversationRead,
);
r.post(
  "/conversations/:conversationId/read",
  requireAuth,
  markConversationRead,
);

export default r;
