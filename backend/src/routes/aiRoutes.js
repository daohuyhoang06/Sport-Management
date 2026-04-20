import { Router } from "express";
import {
  aiChat,
  recommendFields,
  getWeather,
  suggestTimeSlots,
  detectFraud,
} from "../controllers/aiController.js";
import { requireAuth } from "../middleware/authMiddleware.js";

const router = Router();

// Public routes
router.post("/chat", aiChat);
router.post("/recommend-fields", recommendFields);
router.get("/weather", getWeather);
router.get("/suggest-timeslots/:field_id", suggestTimeSlots);

// Protected routes
router.post("/detect-fraud", requireAuth, detectFraud);

export default router;
