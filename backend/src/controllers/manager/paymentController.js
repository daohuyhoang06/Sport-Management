import sequelize from "../../config/database.js";
import Payment from "../../models/Payment.js";
import {
  assertMomoConfigured,
  createMomoPaymentRequest,
  verifyMomoResultSignature,
} from "../../services/momoPaymentService.js";

const generateProviderId = (prefix) =>
  `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`;

const normalizeAmount = (amount) => {
  const value = Number(amount);
  if (!Number.isFinite(value) || value < 1000) {
    return null;
  }
  return Math.round(value);
};

const getBookingPaymentContext = async (bookingId) => {
  const [rows] = await sequelize.query(
    `
      SELECT
        b.booking_id,
        b.customer_id,
        b.field_id,
        b.manager_id as booking_manager_id,
        b.price,
        b.status,
        f.manager_id as field_manager_id,
        f.field_name
      FROM bookings b
      LEFT JOIN fields f ON b.field_id = f.field_id
      WHERE b.booking_id = ?
      LIMIT 1
    `,
    { replacements: [bookingId] },
  );

  return rows[0] || null;
};

export const createMomoPayment = async (req, res) => {
  try {
    const {
      booking_id,
      amount,
      orderInfo,
      extraData = "",
      redirectUrl,
      ipnUrl,
      demo = false,
    } = req.body || {};

    let paymentContext = null;
    let finalAmount = normalizeAmount(amount);
    let finalOrderInfo = orderInfo || "Thanh toan dat san Sport Management";

    if (booking_id) {
      paymentContext = await getBookingPaymentContext(booking_id);
      if (!paymentContext) {
        return res.status(404).json({ message: "Booking not found" });
      }

      finalAmount = normalizeAmount(paymentContext.price);
      finalOrderInfo =
        orderInfo ||
        `Thanh toan dat san #${paymentContext.booking_id} - ${paymentContext.field_name || "Sport Management"}`;
    } else if (!demo) {
      return res.status(400).json({
        message: "booking_id is required unless demo=true is provided",
      });
    }

    if (!finalAmount) {
      return res.status(400).json({
        message: "Invalid amount. MoMo sandbox amount must be at least 1000 VND",
      });
    }

    assertMomoConfigured();

    const orderId = generateProviderId("SM");
    const requestId = generateProviderId("REQ");

    const payment = paymentContext
      ? await Payment.create({
          booking_id: paymentContext.booking_id,
          customer_id: paymentContext.customer_id,
          field_id: paymentContext.field_id,
          amount: finalAmount,
          payment_method: "momo",
          payment_status: "pending",
          order_id: orderId,
          request_id: requestId,
          provider: "momo",
        })
      : null;

    const momoResult = await createMomoPaymentRequest({
      amount: finalAmount,
      orderId,
      orderInfo: finalOrderInfo,
      requestId,
      extraData,
      redirectUrl,
      ipnUrl,
    });

    if (payment) {
      await payment.update({
        pay_url: momoResult.response.payUrl || null,
        deeplink: momoResult.response.deeplink || null,
        qr_code_url: momoResult.response.qrCodeUrl || null,
        raw_response: JSON.stringify(momoResult.response),
        failure_reason:
          momoResult.response.resultCode === 0
            ? null
            : momoResult.response.message || "MoMo create payment failed",
      });
    }

    res.status(201).json({
      payment_id: payment?.payment_id || null,
      order_id: orderId,
      request_id: requestId,
      amount: finalAmount,
      resultCode: momoResult.response.resultCode,
      message: momoResult.response.message,
      payUrl: momoResult.response.payUrl,
      deeplink: momoResult.response.deeplink,
      qrCodeUrl: momoResult.response.qrCodeUrl,
    });
  } catch (error) {
    if (error.message?.startsWith("Missing MoMo sandbox config")) {
      return res.status(503).json({
        message: "MoMo sandbox is not configured",
        error: error.message,
      });
    }

    console.error("createMomoPayment error:", error);
    res.status(500).json({
      message: "Server error when creating MoMo payment",
      error: error.message,
    });
  }
};

const updatePaymentFromMomoResult = async (payload) => {
  const payment = await Payment.findOne({
    where: { order_id: payload.orderId, request_id: payload.requestId },
  });

  if (!payment) {
    return null;
  }

  const completed = Number(payload.resultCode) === 0;

  await payment.update({
    payment_status: completed ? "completed" : "failed",
    transaction_id: payload.transId ? String(payload.transId) : null,
    paid_at: completed ? new Date() : null,
    raw_response: JSON.stringify(payload),
    failure_reason: completed ? null : payload.message || "MoMo payment failed",
  });

  if (completed && payment.booking_id) {
    await sequelize.query(
      `UPDATE bookings
       SET status = 'confirmed', pending_expires_at = NULL
       WHERE booking_id = ?
         AND (
           status = 'confirmed'
           OR (
             status = 'pending'
             AND (pending_expires_at IS NULL OR pending_expires_at > NOW())
           )
         )`,
      { replacements: [payment.booking_id] },
    );
  }

  return payment;
};

export const momoIpn = async (req, res) => {
  try {
    const payload = req.body || {};

    if (!verifyMomoResultSignature(payload)) {
      return res.status(400).json({ message: "Invalid MoMo signature" });
    }

    const payment = await updatePaymentFromMomoResult(payload);
    if (!payment) {
      return res.json({
        resultCode: 0,
        message: "Payment result accepted but no local payment row was found",
      });
    }

    res.json({ resultCode: 0, message: "Success" });
  } catch (error) {
    console.error("momoIpn error:", error);
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

export const momoReturn = async (req, res) => {
  try {
    const payload = { ...req.query, ...req.body };

    if (payload.signature && verifyMomoResultSignature(payload)) {
      await updatePaymentFromMomoResult(payload);
    }

    res.json({
      message: "MoMo payment return received",
      order_id: payload.orderId,
      resultCode: payload.resultCode,
      momoMessage: payload.message,
    });
  } catch (error) {
    console.error("momoReturn error:", error);
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

export const getPaymentById = async (req, res) => {
  try {
    const payment = await Payment.findByPk(req.params.id);
    if (!payment) {
      return res.status(404).json({ message: "Payment not found" });
    }

    res.json(payment);
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

export const getPaymentByOrderId = async (req, res) => {
  try {
    const payment = await Payment.findOne({
      where: { order_id: req.params.orderId },
    });
    if (!payment) {
      return res.status(404).json({ message: "Payment not found" });
    }

    res.json(payment);
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

export const getPayments = async (req, res) => {
  try {
    const { status, provider = "momo", limit = 50 } = req.query;
    const where = {};

    if (status) where.payment_status = status;
    if (provider) where.provider = provider;

    const payments = await Payment.findAll({
      where,
      order: [["payment_id", "DESC"]],
      limit: Number(limit),
    });

    res.json(payments);
  } catch (error) {
    res.status(500).json({ message: "Server error", error: error.message });
  }
};
