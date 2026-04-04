import express from "express";
import {
  getAllFields,
  getFieldById,
  updateField,
} from "../../controllers/manager/fieldController.js";

const router = express.Router();

// GET /api/manager/fields → Lấy danh sách sân
router.get("/", getAllFields);

// GET /api/manager/fields/:id → Xem chi tiết sân
router.get("/:id", getFieldById);

// PUT /api/manager/fields/:id - Cập nhật thông tin sân (Giá, vị trí, trạng thái)
router.put("/:id", updateField);

export default router;