import express from "express";
import { getPayments } from "../../controllers/manager/paymentController.js";

const router = express.Router();

router.get("/", getPayments);

export default router;