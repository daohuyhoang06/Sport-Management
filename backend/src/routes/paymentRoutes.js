import { Router } from "express";
import {
  createMomoPayment,
  getPaymentById,
  getPaymentByOrderId,
  getPayments,
  momoIpn,
  momoReturn,
} from "../controllers/manager/paymentController.js";

const router = Router();

router.post("/momo/create", createMomoPayment);
router.post("/momo/ipn", momoIpn);
router.get("/momo/return", momoReturn);
router.post("/momo/return", momoReturn);
router.get("/order/:orderId", getPaymentByOrderId);
router.get("/:id", getPaymentById);
router.get("/", getPayments);

export default router;
