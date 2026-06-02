import { Router } from "express";
import {
  confirmMomoClientResult,
  createMomoPayment,
  getPaymentById,
  getPaymentByOrderId,
  getPayments,
  momoIpn,
  momoReturn,
} from "../controllers/manager/paymentController.js";
import { requireAuth } from "../middleware/authMiddleware.js";

const router = Router();

router.post("/momo/create", requireAuth, createMomoPayment);
router.post("/momo/client-confirm", requireAuth, confirmMomoClientResult);
router.post("/momo/ipn", momoIpn);
router.get("/momo/return", momoReturn);
router.post("/momo/return", momoReturn);
router.get("/order/:orderId", getPaymentByOrderId);
router.get("/:id", getPaymentById);
router.get("/", getPayments);

export default router;
