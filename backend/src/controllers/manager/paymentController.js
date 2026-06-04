import sequelize from "../../config/database.js";
import Payment from "../../models/Payment.js";
import { bulkEnsureBookingShareAccess } from "../../services/bookingShareService.js";
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

const parseBookingIds = (bookingIds, bookingId) => {
  const normalized = [];
  const source = Array.isArray(bookingIds) ? bookingIds : [];
  source.forEach((value) => {
    const parsed = Number.parseInt(value, 10);
    if (Number.isInteger(parsed) && parsed > 0 && !normalized.includes(parsed)) {
      normalized.push(parsed);
    }
  });

  const singleId = Number.parseInt(bookingId, 10);
  if (Number.isInteger(singleId) && singleId > 0 && !normalized.includes(singleId)) {
    normalized.unshift(singleId);
  }

  return normalized;
};

const buildInClause = (items) => items.map(() => "?").join(", ");

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

const getBookingPaymentContexts = async (bookingIds) => {
  if (!Array.isArray(bookingIds) || bookingIds.length == 0) {
    return [];
  }

  const [rows] = await sequelize.query(
    `
      SELECT
        b.booking_id,
        b.customer_id,
        b.field_id,
        b.court_id,
        b.manager_id as booking_manager_id,
        b.price,
        b.status,
        b.pending_expires_at,
        f.manager_id as field_manager_id,
        f.field_name
      FROM bookings b
      LEFT JOIN fields f ON b.field_id = f.field_id
      WHERE b.booking_id IN (${buildInClause(bookingIds)})
      ORDER BY b.booking_id ASC
    `,
    { replacements: bookingIds },
  );

  return rows;
};

export const createMomoPayment = async (req, res) => {
  try {
    const {
      booking_id,
      booking_ids,
      amount,
      orderInfo,
      extraData = "",
      redirectUrl,
      ipnUrl,
      demo = false,
    } = req.body || {};

    let paymentContext = null;
    let paymentContexts = [];
    let finalAmount = normalizeAmount(amount);
    let finalOrderInfo = orderInfo || "Thanh toan dat san Sport Management";
    const normalizedBookingIds = parseBookingIds(booking_ids, booking_id);
    const currentUserId = Number.parseInt(req.user?.id, 10);

    if (normalizedBookingIds.length > 0) {
      paymentContexts = await getBookingPaymentContexts(normalizedBookingIds);
      if (paymentContexts.length !== normalizedBookingIds.length) {
        return res.status(404).json({ message: "One or more bookings were not found" });
      }

      const bookingIdsSet = new Set(paymentContexts.map((item) => item.booking_id));
      const missingIds = normalizedBookingIds.filter((item) => !bookingIdsSet.has(item));
      if (missingIds.length > 0) {
        return res.status(404).json({ message: "One or more bookings were not found" });
      }

      if (Number.isInteger(currentUserId)) {
        const foreignBooking = paymentContexts.find(
          (item) => Number.parseInt(item.customer_id, 10) !== currentUserId,
        );
        if (foreignBooking) {
          return res.status(403).json({ message: "Booking does not belong to current user" });
        }
      }

      const inactiveBooking = paymentContexts.find((item) => {
        const status = String(item.status || "").toLowerCase();
        if (status === "confirmed") return false;
        if (status !== "pending") return true;
        if (!item.pending_expires_at) return false;
        return new Date(item.pending_expires_at).getTime() <= Date.now();
      });
      if (inactiveBooking) {
        return res.status(409).json({
          message: "One or more bookings are no longer pending for payment",
        });
      }

      const firstContext = paymentContexts[0];
      paymentContext = firstContext;
      finalAmount = normalizeAmount(
        paymentContexts.reduce((sum, item) => sum + Number(item.price || 0), 0),
      );
      finalOrderInfo =
        orderInfo ||
        `Thanh toan dat san ${paymentContexts.length} khung gio - ${firstContext.field_name || "Sport Management"}`;
    } else if (!demo) {
      return res.status(400).json({
        message: "booking_id or booking_ids is required unless demo=true is provided",
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
          booking_ids_json: JSON.stringify(
            paymentContexts.length > 0
              ? paymentContexts.map((item) => item.booking_id)
              : [paymentContext.booking_id],
          ),
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
      booking_ids: paymentContexts.map((item) => item.booking_id),
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
    where: payload.requestId
      ? { order_id: payload.orderId, request_id: payload.requestId }
      : { order_id: payload.orderId },
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

  const bookingIds = (() => {
    const raw = payment.booking_ids_json;
    const parsed = (() => {
      if (!raw) return [];
      try {
        return JSON.parse(raw);
      } catch (_error) {
        return [];
      }
    })();
    const normalized = Array.isArray(parsed)
      ? parsed.map((item) => Number.parseInt(item, 10)).filter((item) => Number.isInteger(item) && item > 0)
      : [];
    if (normalized.length > 0) {
      return [...new Set(normalized)];
    }
    const fallback = Number.parseInt(payment.booking_id, 10);
    return Number.isInteger(fallback) && fallback > 0 ? [fallback] : [];
  })();

  if (completed && bookingIds.length > 0) {
    await sequelize.query(
      `UPDATE bookings
       SET status = 'confirmed', pending_expires_at = NULL
       WHERE booking_id IN (${buildInClause(bookingIds)})
         AND (
           status = 'confirmed'
           OR (
             status = 'pending'
             AND (pending_expires_at IS NULL OR pending_expires_at > NOW())
           )
         )`,
      { replacements: bookingIds },
    );

    await bulkEnsureBookingShareAccess(bookingIds);
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

    const appDeepLink = new URL("sportmanagement://payment/momo");
    const query = new URLSearchParams();
    for (const [key, value] of Object.entries({
      orderId: payload.orderId || payload.order_id || "",
      requestId: payload.requestId || payload.request_id || "",
      resultCode: payload.resultCode ?? payload.result_code ?? "",
      message: payload.message || payload.momoMessage || "",
    })) {
      if (value !== "") {
        query.set(key, String(value));
      }
    }
    appDeepLink.search = query.toString();

    const html = `<!doctype html>
<html lang="vi">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Returning to app</title>
    <style>
      body {
        font-family: Arial, sans-serif;
        background: #f7f9fc;
        color: #0f172a;
        display: grid;
        place-items: center;
        min-height: 100vh;
        margin: 0;
        padding: 24px;
      }
      .card {
        width: min(100%, 520px);
        background: #fff;
        border-radius: 16px;
        padding: 24px;
        box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
        text-align: center;
      }
      .btn {
        display: inline-block;
        margin-top: 16px;
        padding: 12px 16px;
        border-radius: 12px;
        text-decoration: none;
        background: #1d4ed8;
        color: #fff;
      }
      p { line-height: 1.5; }
    </style>
    <script>
      (function () {
        var deepLink = ${JSON.stringify(appDeepLink.toString())};
        setTimeout(function () {
          window.location.href = deepLink;
        }, 150);
      })();
    </script>
  </head>
  <body>
    <div class="card">
      <h2>Đang quay lại ứng dụng</h2>
      <p>MoMo đã hoàn tất luồng thanh toán. Nếu ứng dụng không mở tự động, hãy bấm nút bên dưới.</p>
      <a class="btn" href="${appDeepLink.toString()}">Quay lại app</a>
    </div>
  </body>
</html>`;

    res.setHeader("Content-Type", "text/html; charset=utf-8");
    res.send(html);
  } catch (error) {
    console.error("momoReturn error:", error);
    res.status(500).json({ message: "Server error", error: error.message });
  }
};

export const confirmMomoClientResult = async (req, res) => {
  try {
    const payload = req.body || {};
    const orderId = payload.orderId || payload.order_id;
    const requestId = payload.requestId || payload.request_id;
    const resultCode =
      payload.resultCode !== undefined ? payload.resultCode : payload.result_code;

    if (!orderId || resultCode === undefined || resultCode === null) {
      return res.status(400).json({
        message: "orderId and resultCode are required",
      });
    }

    const payment = await Payment.findOne({
      where: requestId
        ? { order_id: orderId, request_id: requestId }
        : { order_id: orderId },
    });

    if (!payment) {
      return res.status(404).json({ message: "Payment not found" });
    }

    const currentUserId = Number.parseInt(req.user?.id, 10);
    if (
      Number.isInteger(currentUserId) &&
      Number.parseInt(payment.customer_id, 10) !== currentUserId
    ) {
      return res.status(403).json({ message: "Payment does not belong to current user" });
    }

    const updatedPayment = await updatePaymentFromMomoResult({
      orderId,
      requestId,
      resultCode,
      message: payload.message || payload.momoMessage || null,
      transId: payload.transId || payload.transactionId || null,
    });

    res.json({
      message: "Client payment result confirmed",
      payment_status: updatedPayment?.payment_status || payment.payment_status,
      order_id: orderId,
      request_id: requestId,
    });
  } catch (error) {
    console.error("confirmMomoClientResult error:", error);
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
