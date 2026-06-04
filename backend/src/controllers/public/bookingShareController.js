import {
  buildBookingShareResponse,
  getBookingShareDetailByToken,
} from "../../services/bookingShareService.js";

const escapeHtml = (value) =>
  String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");

const statusTone = (statusCode) => {
  if (statusCode === "checked_in") {
    return {
      background: "#DCFCE7",
      color: "#166534",
    };
  }
  if (statusCode === "expired" || statusCode === "cancelled") {
    return {
      background: "#FEE2E2",
      color: "#991B1B",
    };
  }
  return {
    background: "#DBEAFE",
    color: "#1D4ED8",
  };
};

const renderSharePageHtml = (payload) => {
  const tone = statusTone(payload.statusCode);
  const statusStyle = `background:${tone.background};color:${tone.color};`;
  const paymentInfo = [payload.paymentMethod, payload.totalPrice]
    .filter(Boolean)
    .join(" • ");

  return `<!doctype html>
<html lang="vi">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Thông tin đặt sân</title>
    <style>
      :root {
        color-scheme: light;
      }
      body {
        margin: 0;
        background: #f3f6fb;
        color: #0f172a;
        font-family: Arial, sans-serif;
        padding: 24px;
      }
      .container {
        max-width: 720px;
        margin: 0 auto;
      }
      .card {
        background: #fff;
        border-radius: 20px;
        box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
        overflow: hidden;
      }
      .hero {
        background: linear-gradient(135deg, #1d4ed8, #2563eb);
        color: #fff;
        padding: 28px 24px;
      }
      .hero h1 {
        margin: 0 0 8px;
        font-size: 28px;
      }
      .hero p {
        margin: 0;
        line-height: 1.5;
        color: rgba(255, 255, 255, 0.88);
      }
      .body {
        padding: 24px;
      }
      .section {
        margin-bottom: 20px;
        padding-bottom: 20px;
        border-bottom: 1px solid #e2e8f0;
      }
      .section:last-child {
        border-bottom: none;
        margin-bottom: 0;
        padding-bottom: 0;
      }
      .section-title {
        font-size: 13px;
        font-weight: 700;
        color: #64748b;
        margin: 0 0 12px;
        text-transform: uppercase;
        letter-spacing: 0.04em;
      }
      .field-name {
        font-size: 24px;
        font-weight: 700;
        margin: 0 0 6px;
      }
      .muted {
        color: #475569;
        line-height: 1.5;
      }
      .grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
        gap: 14px;
      }
      .item {
        background: #f8fafc;
        border-radius: 14px;
        padding: 14px;
      }
      .item-label {
        font-size: 12px;
        color: #64748b;
        margin-bottom: 6px;
      }
      .item-value {
        font-size: 16px;
        font-weight: 700;
        color: #0f172a;
      }
      .status-chip {
        display: inline-flex;
        align-items: center;
        padding: 8px 12px;
        border-radius: 999px;
        font-size: 14px;
        font-weight: 700;
        ${statusStyle}
      }
      .code-box {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        border: 1px dashed #93c5fd;
        background: #eff6ff;
        border-radius: 16px;
        padding: 16px;
      }
      .code-value {
        font-size: 28px;
        font-weight: 800;
        letter-spacing: 0.16em;
      }
      .button {
        appearance: none;
        border: none;
        border-radius: 12px;
        background: #1d4ed8;
        color: #fff;
        padding: 12px 16px;
        font-size: 14px;
        font-weight: 700;
        cursor: pointer;
      }
      .caption {
        margin-top: 10px;
        color: #64748b;
        line-height: 1.5;
      }
      @media (max-width: 540px) {
        body {
          padding: 12px;
        }
        .body,
        .hero {
          padding: 18px;
        }
        .code-box {
          flex-direction: column;
          align-items: flex-start;
        }
      }
    </style>
  </head>
  <body>
    <div class="container">
      <div class="card">
        <div class="hero">
          <h1>Thông tin đặt sân</h1>
        </div>
        <div class="body">
          <div class="section">
            <div class="field-name">${escapeHtml(payload.field.fieldName)}</div>
            <div class="muted">${escapeHtml(payload.field.address)}</div>
          </div>

          <div class="section">
            <div class="section-title">Trạng thái đơn</div>
            <div class="status-chip">${escapeHtml(payload.status)}</div>
          </div>

          <div class="section">
            <div class="section-title">Thông tin đặt sân</div>
            <div class="grid">
              <div class="item">
                <div class="item-label">Mã đơn</div>
                <div class="item-value">${escapeHtml(payload.bookingCode)}</div>
              </div>
              <div class="item">
                <div class="item-label">Ngày đặt</div>
                <div class="item-value">${escapeHtml(payload.date)}</div>
              </div>
              <div class="item">
                <div class="item-label">Khung giờ</div>
                <div class="item-value">${escapeHtml(payload.timeRange)}</div>
              </div>
              <div class="item">
                <div class="item-label">Người đặt sân</div>
                <div class="item-value">${escapeHtml(payload.user.name || "Chưa cập nhật")}</div>
              </div>
              <div class="item">
                <div class="item-label">Số điện thoại</div>
                <div class="item-value">${escapeHtml(payload.user.phone || "Chưa cập nhật")}</div>
              </div>
            </div>
          </div>

          <div class="section">
            <div class="section-title">Thông tin thanh toán</div>
            <div class="grid">
              <div class="item">
                <div class="item-label">Thanh toán</div>
                <div class="item-value">${escapeHtml(paymentInfo || "Chưa cập nhật")}</div>
              </div>
              <div class="item">
                <div class="item-label">Mã giao dịch</div>
                <div class="item-value">${escapeHtml(payload.transactionId || payload.orderId || "Chưa cập nhật")}</div>
              </div>
            </div>
          </div>

          <div class="section">
            <div class="section-title">Liên hệ chủ sân</div>
            <div class="grid">
              <div class="item">
                <div class="item-label">Chủ sân</div>
                <div class="item-value">${escapeHtml(payload.field.ownerName || "Chủ sân")}</div>
              </div>
              <div class="item">
                <div class="item-label">Số điện thoại</div>
                <div class="item-value">${escapeHtml(payload.field.ownerPhone || "Chưa cập nhật")}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </body>
</html>`;
};

const resolvePublicBaseUrl = (req) =>
  process.env.PUBLIC_WEB_BASE_URL || `${req.protocol}://${req.get("host")}`;

export const getPublicBookingShare = async (req, res) => {
  try {
    const detail = await getBookingShareDetailByToken(req.params.shareToken);
    if (!detail) {
      return res.status(404).json({
        success: false,
        message: "Booking share not found",
      });
    }

    return res.json({
      success: true,
      data: buildBookingShareResponse(detail, resolvePublicBaseUrl(req)),
    });
  } catch (error) {
    console.error("getPublicBookingShare error:", error);
    return res.status(500).json({
      success: false,
      message: "Server error when fetching booking share",
    });
  }
};

export const renderPublicBookingSharePage = async (req, res) => {
  try {
    const detail = await getBookingShareDetailByToken(req.params.shareToken);
    if (!detail) {
      return res.status(404).send("Booking share not found");
    }

    const payload = buildBookingShareResponse(detail, resolvePublicBaseUrl(req));
    res.setHeader("Content-Type", "text/html; charset=utf-8");
    return res.send(renderSharePageHtml(payload));
  } catch (error) {
    console.error("renderPublicBookingSharePage error:", error);
    return res.status(500).send("Server error");
  }
};
